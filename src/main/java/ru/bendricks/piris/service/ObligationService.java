package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.hibernate.query.sqm.TemporalUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.repository.ObligationPlanRepository;
import ru.bendricks.piris.repository.ObligationRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Log
@RequiredArgsConstructor
public class ObligationService {

    private final ObligationRepository obligationRepository;
    private final ObligationPlanRepository obligationPlanRepository;
    private final AccountService accountService;
    private final UserService userService;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Obligation createDepositObligation(ObligationCreateDTO createDTO) throws Exception {
        var obligation = createDTO.getObligation();
        obligation.setEndTime(obligation.getStartTime().plus(createDTO.getMonths(), ChronoUnit.MONTHS));
        var obligationPlan = obligationPlanRepository.getReferenceById(obligation.getObligationPlan().getId());
        obligation.setObligationType(obligationPlan.getObligationType());
        obligation.setAmount(createDTO.getStartBalance());
        obligation.setCurrency(obligationPlan.getCurrency());
        obligation.setStatus(RecordStatus.ACTIVE);
//        if (obligation.getStartTime().until(obligation.getEndTime(), ChronoUnit.MONTHS) > obligation.getObligationPlan().getMonths())
//            throw new Exception("Невозможно назначить депозит больше чем на " + obligation.getObligationPlan().getMonths() + " месяцев");
//        var now = LocalDateTime.now();
//        obligation.setStartTime(now);
//        obligation.setEndTime(now.plus(obligation.getObligationPlan().getMonths(), ChronoUnit.MONTHS));
        accountService.createDepositAccount(obligation);
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

    public List<ObligationPlan> getObligationPlansByObligationType(ObligationType ... obligationTypes) {
        List<ObligationPlan> plans = new ArrayList<>();
        for (var type : obligationTypes) {
            plans.addAll(obligationPlanRepository.findAllByObligationType(type));
        }
        return plans;
    }

    public List<Obligation> getObligationsByUserId(Long userId) {
        return obligationRepository.findAllByOwnerId(userId);
    }

    public Obligation getObligationById(Long obligationId) {
        return obligationRepository.findById(obligationId).orElse(null);
    }

    public boolean existsByContractNumber(String contractNumber) {
        return obligationRepository.existsByContractNumber(contractNumber);
    }

}
