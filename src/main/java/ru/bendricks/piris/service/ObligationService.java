package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.hibernate.query.sqm.TemporalUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.model.Account;
import ru.bendricks.piris.model.Obligation;
import ru.bendricks.piris.model.ObligationType;
import ru.bendricks.piris.model.RecordStatus;
import ru.bendricks.piris.repository.ObligationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Log
@RequiredArgsConstructor
public class ObligationService {

    private final ObligationRepository obligationRepository;
    private final AccountService accountService;
    private final UserService userService;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Obligation createDepositObligation(Obligation obligation, long openBalance) throws Exception {
        obligation.setStatus(RecordStatus.ACTIVE);
        if (obligation.getStartTime().until(obligation.getEndTime(), ChronoUnit.MONTHS) > obligation.getObligationPlan().getMonths())
            throw new Exception("Невозможно назначить депозит больше чем на " + obligation.getObligationPlan().getMonths() + " месяцев");
//        var now = LocalDateTime.now();
//        obligation.setStartTime(now);
//        obligation.setEndTime(now.plus(obligation.getObligationPlan().getMonths(), ChronoUnit.MONTHS));
        accountService.createDepositAccount(obligation, openBalance);
        return obligationRepository.save(obligation);
    }

    @Transactional
    public void createCreditObligation(Obligation obligation) {

    }

    //    @Scheduled(cron = "* * 12 * * MON-SUN")
    @Scheduled(cron = "30 * * * * MON-SUN")
    @Transactional
    public void bankDayClose() {
        log.info("Запущена процедура закрытия банковского дня");
        var obligations = obligationRepository.findAll();
        obligations.stream().filter(obligation -> obligation.getStatus().equals(RecordStatus.ACTIVE)
                && (obligation.getObligationPlan().getObligationType().equals(ObligationType.DEPOSIT) || obligation.getObligationPlan().getObligationType().equals(ObligationType.DEPOSIT_UNTOUCH)))
                .forEach(obligation -> {
                    if (obligation.getStartTime().until(LocalDateTime.now(), ChronoUnit.DAYS) % 30 == 0) {
                        log.info("Происходит расчет процентов для договора " + obligation.getContractNumber());
                        obligation.getPercentAccount().setBalance(
                                obligation.getPercentAccount().getBalance() + (long)(obligation.getMainAccount().getBalance() *
                                        obligation.getObligationPlan().getPlanPercent() / 100)
                        );
                        if (obligation.getEndTime().isBefore(LocalDateTime.now())) {
                            obligation.setStatus(RecordStatus.CLOSED);
                        }
                    }
                });
    }

}
