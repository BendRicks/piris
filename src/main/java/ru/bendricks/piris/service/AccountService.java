package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.repository.AccountRepository;
import ru.bendricks.piris.repository.AccountTypeRepository;
import ru.bendricks.piris.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            if (!senderAccount.getOwner().getId().equals(userDetails.getId()))
                throw new Exception("Ты хто такой?");
            if (senderAccount.getStatus() == RecordStatus.CLOSED)
                throw new Exception("С данного счёта нельзя перевести деньги так как он закрыт");
            if (!permittedSenderAccountTypes.contains(senderAccount.getAccountType().getCode()) && !(senderAccount.getAccountType().getCode() == 3404 && senderAccount.getStatus() == RecordStatus.END_OF_SERVICE))
                throw new Exception("С данного счёта нельзя перевести деньги");
            if (recipientAccount.getStatus() == RecordStatus.CLOSED || (recipientAccount.getStatus() == RecordStatus.END_OF_SERVICE && recipientAccount.getAccountType().getCode() != 2470))
                throw new Exception("На данный счёт нельзя перевести деньги");
            if (senderAccount.getBalance() < transaction.getAmount())
                throw new Exception("Недостаточно денег на балансе отправителя");
        }
        if (senderAccount.getCurrency() != recipientAccount.getCurrency())
            throw new Exception("Невозможно перевести деньги на счёт с другой валютой");
        senderAccount.setBalance(senderAccount.getBalance() - transaction.getAmount());
        if (senderAccount.getBalance() == 0 && senderAccount.getStatus() == RecordStatus.END_OF_SERVICE) {
            senderAccount.setStatus(RecordStatus.CLOSED);
            var parentObligation = Optional.of(senderAccount.getParentObligationAsMainAccount()).orElse(senderAccount.getParentObligationAsPercentAccount());
            if (parentObligation.getMainAccount().getStatus() == RecordStatus.CLOSED
                    && parentObligation.getPercentAccount().getStatus() == RecordStatus.CLOSED) {
                parentObligation.setStatus(RecordStatus.CLOSED);
            }
        }
        recipientAccount.setBalance(recipientAccount.getBalance() + transaction.getAmount());
        if (recipientAccount.getBalance() >= 0 && recipientAccount.getStatus() == RecordStatus.END_OF_SERVICE) {
            recipientAccount.setStatus(RecordStatus.CLOSED);
            var parentObligation = Optional.of(senderAccount.getParentObligationAsMainAccount()).orElse(senderAccount.getParentObligationAsPercentAccount());
            if (parentObligation.getMainAccount().getStatus() == RecordStatus.CLOSED
                    && parentObligation.getPercentAccount().getStatus() == RecordStatus.CLOSED) {
                parentObligation.setStatus(RecordStatus.CLOSED);
            }
        }
        transaction.setSender(senderAccount);
        transaction.setRecipient(recipientAccount);
        transaction.setTime(LocalDateTime.now());
        transaction.setCurrency(senderAccount.getCurrency());
        return transactionRepository.save(transaction);
    }

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
    public void createDepositAccounts(Obligation obligation) {
        var mainIban = generateIban();
        var percentIban = generateIban();
        var mainAccount = new Account(mainIban, "Депозитный счёт", accountTypeRepository.findById(3404).orElse(null),
                RecordStatus.ACTIVE, obligation.getOwner(), 0, obligation.getCurrency(), obligation,
                null, null, null);
        var percentAccount = new Account(percentIban, "Процентный счёт по депозиту " + mainIban,
                accountTypeRepository.findById(3470).orElse(null), RecordStatus.ACTIVE, obligation.getOwner(),
                0, obligation.getCurrency(), null, obligation, null, null);
        obligation.setMainAccount(mainAccount);
        obligation.setPercentAccount(percentAccount);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void createCreditAccounts(Obligation obligation) {
        var mainIban = generateIban();
        var percentIban = generateIban();
        var mainAccount = new Account(mainIban, "Кредитный счёт", accountTypeRepository.findById(2400).orElse(null),
                RecordStatus.ACTIVE, obligation.getOwner(), 0, obligation.getCurrency(), obligation,
                null, null, null);
        var percentAccount = new Account(percentIban, "Процентный счёт по кредиту " + mainIban,
                accountTypeRepository.findById(2470).orElse(null), RecordStatus.ACTIVE, obligation.getOwner(),
                0, obligation.getCurrency(), null, obligation, null, null);
        obligation.setMainAccount(mainAccount);
        obligation.setPercentAccount(percentAccount);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Account> getSfrbAccount(Currency currency) {
        return switch (currency) {
            case BYN -> accountRepository.findById(sfrbIBANByn);
            case USD -> accountRepository.findById(sfrbIBANUsd);
            case EUR -> accountRepository.findById(sfrbIBANEur);
        };
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Account createPaymentAccount(Account account) {
        account.setIban(generateIban());
        account.setAccountType(accountTypeRepository.findById(3014).orElse(null));
        account.setBalance(0);
        account.setStatus(RecordStatus.ACTIVE);
        return accountRepository.save(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Account> getPaymentAccounts(Long id, Currency currency) {
        return accountRepository.findAllByOwnerIdAndAccountTypeCodeAndCurrency(id, 3014, currency);
    }

}
