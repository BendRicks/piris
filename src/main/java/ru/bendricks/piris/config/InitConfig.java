package ru.bendricks.piris.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.repository.AccountRepository;
import ru.bendricks.piris.repository.AccountTypeRepository;
import ru.bendricks.piris.repository.ObligationPlanRepository;
import ru.bendricks.piris.repository.UserRepository;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Log
public class InitConfig {

    private final ObligationPlanRepository obligationPlanRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Value("${sfrbIBANByn}")
    private String sfrbIBANByn;
    @Value("${sfrbIBANUsd}")
    private String sfrbIBANUsd;
    @Value("${sfrbIBANEur}")
    private String sfrbIBANEur;
    @Value("${cashIBANByn}")
    private String cashIBANByn;
    @Value("${cashIBANUsd}")
    private String cashIBANUsd;
    @Value("${cashIBANEur}")
    private String cashIBANEur;

    @PostConstruct
    public void init() {
        User adminUser = userRepository.findUserByEmail("bank@gmail.com").orElse(null);
        if (adminUser == null) {
            log.info("Не был найден ни один аккаунт. Будет создана запись");
            adminUser = new User(null, RecordStatus.ACTIVE, UserRole.ROLE_ADMIN, "$2y$10$mJriDHm56IoyKmzZPmr/wue7ofdi6q5Wr8aTmlVQ3icua0/ybSs0y",
                    "Банк", "Банк", "Банк", LocalDate.now(), Sex.ATTACK_HELICOPTER,
                    "BY1234567", "bank", LocalDate.now(), "5111111A001PB0", "bank",
                    City.MINSK, "bank", "+375170000000", "+375290000000",
                    "bank@gmail.com", "bank", MaritalStatus.SINGLE,
                    Citizenship.REPUBLIC_OF_BELARUS, Disability.NO, false, 0L, null, null);
            userRepository.saveAndFlush(adminUser);
        }
        if (obligationPlanRepository.count() == 0) {
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.DEPOSIT_UNTOUCH, Currency.BYN, "Депозит МТБелки", 13, 36, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.DEPOSIT_UNTOUCH, Currency.USD, "Депозит Актуальный(USD)", 0.1, 24, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.DEPOSIT_UNTOUCH, Currency.USD, "Депозит Актуальный online(USD)", 0.1, 24, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.DEPOSIT_UNTOUCH, Currency.EUR, "Депозит Актуальный(EUR)", 0.1, 24, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.DEPOSIT_UNTOUCH, Currency.EUR, "Депозит Актуальный online(EUR)", 0.1, 24, null));

            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.DEPOSIT, Currency.BYN, "Депозит МТБелки online", 13.2, 36, null));

            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.CREDIT_ANUIT, Currency.BYN, "Кредит на готовое жилье от застройщика", 14.5, 240, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.CREDIT_ANUIT, Currency.BYN, "Кредит на недвижимость «Счастливы в месте»", 0.001, 360, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.CREDIT, Currency.BYN, "Потребительский кредит «Проще простого»", 9.99, 84, null));
            obligationPlanRepository.save(new ObligationPlan(null, RecordStatus.ACTIVE, ObligationType.CREDIT, Currency.BYN, "Потребительский кредит «Большие планы»", 6.99, 84, null));
        }
        if (accountTypeRepository.count() == 0) {
            accountTypeRepository.save(new AccountType(3014, "Текущий счёт физического лица", null));
            accountTypeRepository.save(new AccountType(3404, "Депозитный счёт физического лица", null));
            accountTypeRepository.save(new AccountType(3470, "Начисленные процентные расходы по вкладам", null));
            accountTypeRepository.save(new AccountType(2400, "Кредитный счёт физического лица", null));
            accountTypeRepository.save(new AccountType(2470, "Начисленные процентные доходы по займам физическим лицам", null));
            accountTypeRepository.save(new AccountType(1010, "Касса банка", null));
            accountTypeRepository.save(new AccountType(7327, "Фонд развития банка", null));
        }
        if (accountRepository.findById(sfrbIBANByn).isEmpty()) {
            log.info("Счет СФРБ(BYN) не был найден. Будет создана запись");
            accountRepository.saveAndFlush(new Account(sfrbIBANByn, "Счёт Фонда Развития Банка BYN", accountTypeRepository.findById(7327).orElse(null), RecordStatus.ACTIVE, adminUser, 10000000000000L, Currency.BYN, null, null, null, null, null));
        }
        if (accountRepository.findById(sfrbIBANUsd).isEmpty()) {
            log.info("Счет СФРБ(USD) не был найден. Будет создана запись");
            accountRepository.saveAndFlush(new Account(sfrbIBANUsd, "Счёт Фонда Развития Банка USD", accountTypeRepository.findById(7327).orElse(null), RecordStatus.ACTIVE, adminUser, 100000000L, Currency.USD, null, null, null, null, null));
        }
        if (accountRepository.findById(sfrbIBANEur).isEmpty()) {
            log.info("Счет СФРБ(EUR) не был найден. Будет создана запись");
            accountRepository.saveAndFlush(new Account(sfrbIBANEur, "Счёт Фонда Развития Банка EUR", accountTypeRepository.findById(7327).orElse(null), RecordStatus.ACTIVE, adminUser, 100000000L, Currency.EUR, null, null, null, null, null));
        }
        if (accountRepository.findById(cashIBANByn).isEmpty()) {
            log.info("Счет кассы (BYN) не был найден. Будет создана запись");
            accountRepository.saveAndFlush(new Account(cashIBANByn, "Касса Банка BYN", accountTypeRepository.findById(1010).orElse(null), RecordStatus.ACTIVE, adminUser, 0, Currency.BYN, null, null, null, null, null));
        }
        if (accountRepository.findById(cashIBANUsd).isEmpty()) {
            log.info("Счет кассы (USD) не был найден. Будет создана запись");
            accountRepository.saveAndFlush(new Account(cashIBANUsd, "Касса Банка USD", accountTypeRepository.findById(1010).orElse(null), RecordStatus.ACTIVE, adminUser, 0, Currency.USD, null, null, null, null, null));
        }
        if (accountRepository.findById(cashIBANEur).isEmpty()) {
            log.info("Счет кассы (EUR) не был найден. Будет создана запись");
            accountRepository.saveAndFlush(new Account(cashIBANEur, "Касса Банка EUR", accountTypeRepository.findById(1010).orElse(null), RecordStatus.ACTIVE, adminUser, 0, Currency.EUR, null, null, null, null, null));
        }
//        log.info("Курс доллара - " + Currency.USD.getRate());
    }

}