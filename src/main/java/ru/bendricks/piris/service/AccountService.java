package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.repository.AccountRepository;
import ru.bendricks.piris.repository.AccountTypeRepository;
import ru.bendricks.piris.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Log
@Service
@RequiredArgsConstructor
public class AccountService {

    //    @Value("${sfrbUUIDByn}")
//    private UUID sfrbUUIDByn;
//    @Value("${sfrbUUIDUsd}")
//    private UUID sfrbUUIDUsd;
//    @Value("${sfrbUUIDEur}")
//    private UUID sfrbUUIDEur;
    @Value("${sfrbIBANByn}")
    private String sfrbIBANByn;
    @Value("${sfrbIBANUsd}")
    private String sfrbIBANUsd;
    @Value("${sfrbIBANEur}")
    private String sfrbIBANEur;

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    private final Set<Integer> permittedSenderAccountTypes = Set.of(3014, 3470, 2400, 1010, 7327);

    @Transactional
//    public void transferMoney(UUID senderId, UUID recipientId, long amount) throws Exception {
    public Transaction transferMoney(Transaction transaction, CustomUserDetails userDetails) throws Exception {
        Account senderAccount = accountRepository.findById(transaction.getSender().getIban()).orElseThrow(() -> new Exception("Счёт отправителя не был найден"));
        Account recipientAccount = accountRepository.findById(transaction.getRecipient().getIban()).orElseThrow(() -> new Exception("Счёт получателя не был найден"));
        if (senderAccount.getOwner().getId() != userDetails.getId())
            throw new Exception("Ты хто такой?");
        if (senderAccount.getStatus() == RecordStatus.CLOSED)
            throw new Exception("С данного счёта нельзя перевести деньги так как он закрыт");
        if (!permittedSenderAccountTypes.contains(senderAccount.getAccountType().getCode()) && !(senderAccount.getAccountType().getCode() == 3404 && senderAccount.getStatus() == RecordStatus.END_OF_SERVICE))
            throw new Exception("С данного счёта нельзя перевести деньги");
        if (recipientAccount.getStatus() == RecordStatus.CLOSED || recipientAccount.getStatus() == RecordStatus.END_OF_SERVICE)
            throw new Exception("На данный счёт нельзя перевести деньги");
        if (senderAccount.getCurrency() != recipientAccount.getCurrency())
            throw new Exception("Невозможно перевести деньги на счёт с другой валютой");
        if (senderAccount.getBalance() < transaction.getAmount()) {
            throw new Exception("Недостаточно денег на балансе отправителя");
        }
        senderAccount.setBalance(senderAccount.getBalance() - transaction.getAmount());
        if (senderAccount.getBalance() == 0 && senderAccount.getStatus() == RecordStatus.END_OF_SERVICE) {
            senderAccount.setStatus(RecordStatus.CLOSED);
            if (senderAccount.getParentObligation().getMainAccount().getStatus() == RecordStatus.CLOSED
                    && senderAccount.getParentObligation().getPercentAccount().getStatus() == RecordStatus.CLOSED) {
                senderAccount.getParentObligation().setStatus(RecordStatus.CLOSED);
            }
        }
        recipientAccount.setBalance(recipientAccount.getBalance() + transaction.getAmount());
        transaction.setSender(senderAccount);
        transaction.setRecipient(recipientAccount);
        transaction.setTime(LocalDateTime.now());
        transaction.setCurrency(senderAccount.getCurrency());
        return transactionRepository.save(transaction);
    }

    //BY00MTBK

    private String generateIban() {
        StringBuilder sb;
        do {
            var rand = new Random();
            sb = new StringBuilder("BY");
            sb.append(rand.nextInt(10)).append(rand.nextInt(10)).append("MTBK")
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10))
                    .append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10)).append(rand.nextInt(10));
        } while (accountRepository.existsById(sb.toString()));
        return sb.toString();
    }

    public List<Account> getAccountsByUserId(Long id) {
        return accountRepository.findAllByOwnerId(id);
    }

    public Account getAccountById(String iban) {
        return accountRepository.findById(iban).orElse(null);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void createDepositAccount(Obligation obligation) {
        var mainIban = generateIban();
        var percentIban = generateIban();
        var mainAccount = new Account(mainIban, "Депозитный счёт", accountTypeRepository.getReferenceById(3404),
                RecordStatus.ACTIVE, obligation.getOwner(), obligation.getAmount(), obligation.getCurrency(), obligation,
                null, null);
        var percentAccount = new Account(percentIban, "Процентный счёт по депозиту " + mainIban,
                accountTypeRepository.getReferenceById(3470), RecordStatus.ACTIVE, obligation.getOwner(),
                0, obligation.getCurrency(), obligation, null, null);
        var sfrbAcc = switch (obligation.getCurrency()) {
            case BYN -> accountRepository.findById(sfrbIBANByn);
            case USD -> accountRepository.findById(sfrbIBANUsd);
            case EUR -> accountRepository.findById(sfrbIBANEur);
        };
        sfrbAcc.ifPresent(sfAcc -> sfAcc.setBalance(sfAcc.getBalance() + obligation.getAmount()));
        obligation.setMainAccount(mainAccount);
        obligation.setPercentAccount(percentAccount);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account createPaymentAccount(Account account) {
        account.setIban(generateIban());
        account.setAccountType(accountTypeRepository.getReferenceById(3014));
        account.setBalance(0);
        account.setStatus(RecordStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Account> getPaymentAccounts(Long id, Currency currency) {
        return accountRepository.findAllByOwnerIdAndAccountTypeCodeAndCurrency(id, 3014, currency);
    }

}
