package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.repository.AccountRepository;
import ru.bendricks.piris.repository.AccountTypeRepository;
import ru.bendricks.piris.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Log
@Service
@RequiredArgsConstructor
public class AccountService {

    @Value("${sfrbUUIDByn}")
    private UUID sfrbUUIDByn;
    @Value("${sfrbUUIDUsd}")
    private UUID sfrbUUIDUsd;
    @Value("${sfrbUUIDEur}")
    private UUID sfrbUUIDEur;

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    private final Set<Integer> permittedSenderAccountTypes = Set.of(3014, 3470, 2400, 1010, 7327);

    @Transactional
    public void transferMoney(UUID senderId, UUID recipientId, long amount) throws Exception {
        Account senderAccount = accountRepository.findById(senderId).orElseThrow(() -> new Exception("Счёт отправителя не был найден"));
        Account recipientAccount = accountRepository.findById(recipientId).orElseThrow(() -> new Exception("Счёт получателя не был найден"));
        if (!permittedSenderAccountTypes.contains(senderAccount.getAccountType().getCode()))
            throw new Exception("С данного счёта нельзя перевести деньги");
        if (senderAccount.getCurrency() != recipientAccount.getCurrency())
            throw new Exception("Невозможно перевести деньги на счёт с другой валютой");
        if (senderAccount.getBalance() < amount) {
            throw new Exception("Недостаточно денег на балансе отправителя");
        }
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        recipientAccount.setBalance(recipientAccount.getBalance() + amount);
        transactionRepository.save(new Transaction(null, senderAccount, recipientAccount, amount, LocalDateTime.now(), senderAccount.getCurrency()));
    }

    @Transactional
    public void createDepositAccount(Obligation obligation, long openBalance) {
        UUID mainAccountUUID, percentAccountUUID;
        do { mainAccountUUID = UUID.randomUUID();} while (!accountRepository.existsById(mainAccountUUID));
        do { percentAccountUUID = UUID.randomUUID();} while (!accountRepository.existsById(percentAccountUUID));
        var mainAccount = new Account(mainAccountUUID, "Депозитный счёт", accountTypeRepository.getReferenceById(3404),
                RecordStatus.ACTIVE, obligation.getOwner(), openBalance, obligation.getCurrency(), obligation,
                null, null);
        var percentAccount = new Account(percentAccountUUID, "Процентный счёт по депозиту",
                accountTypeRepository.getReferenceById(3470), RecordStatus.ACTIVE, obligation.getOwner(),
                0, obligation.getCurrency(), obligation, null, null);
        var sfrbAcc = switch (obligation.getCurrency()) {
            case BYN -> accountRepository.findById(sfrbUUIDByn);
            case USD -> accountRepository.findById(sfrbUUIDUsd);
            case EUR -> accountRepository.findById(sfrbUUIDEur);
        };
        sfrbAcc.ifPresent(sfAcc -> sfAcc.setBalance(sfAcc.getBalance() + openBalance));
        obligation.setMainAccount(mainAccount);
        obligation.setPercentAccount(percentAccount);
    }

    @Transactional
    public void createPaymentAccount() {

    }

}
