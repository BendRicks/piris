package ru.bendricks.piris.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.repository.ObligationPlanRepository;
import ru.bendricks.piris.repository.ObligationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Log
@RequiredArgsConstructor
public class ObligationService {

    private final ObligationRepository obligationRepository;
    private final ObligationPlanRepository obligationPlanRepository;
    private final AccountService accountService;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Obligation createDepositObligation(ObligationCreateDTO createDTO) {
        var obligation = createDTO.getObligation();
        obligation.setContractNumber(generateContractNumber());
        obligation.setEndTime(obligation.getStartTime().plus(createDTO.getMonths(), ChronoUnit.MONTHS));
        var obligationPlan = obligationPlanRepository.getReferenceById(obligation.getObligationPlan().getId());
        obligation.setObligationType(obligationPlan.getObligationType());
        obligation.setAmount(createDTO.getStartBalance());
        obligation.setCurrency(obligationPlan.getCurrency());
        obligation.setStatus(RecordStatus.ACTIVE);
        accountService.createDepositAccount(obligation);
        return obligationRepository.save(obligation);
    }

    private String generateContractNumber() {
        var rand = new Random();
        StringBuilder sb;
        do {
            sb = new StringBuilder();
            sb.append(getInt(rand)).append(getInt(rand)).append(getInt(rand)).append(getInt(rand))
                    .append(getInt(rand)).append(getInt(rand)).append(getInt(rand)).append(getInt(rand))
                    .append(getInt(rand)).append(getInt(rand)).append(getInt(rand)).append(getInt(rand));
        } while (obligationRepository.existsByContractNumber(sb.toString()));
        return sb.toString();
    }

    private int getInt(Random rand) {
        return Math.abs(rand.nextInt() % 10);
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
        obligations.stream().filter(obligation -> obligation.getStatus().equals(RecordStatus.ACTIVE))
//                && (obligation.getObligationPlan().getObligationType().equals(ObligationType.DEPOSIT) || obligation.getObligationPlan().getObligationType().equals(ObligationType.DEPOSIT_UNTOUCH)))
                .forEach(obligation -> {
                    if (checkIfCalcDay(obligation.getStartTime(), obligation.getEndTime())) {
                        log.info("Происходит расчет процентов для договора " + obligation.getContractNumber());
                        obligation.getPercentAccount().setBalance(
                                obligation.getPercentAccount().getBalance() + (long)(obligation.getAmount() *
                                        obligation.getObligationPlan().getPlanPercent() / 100)
                        );
                        obligation.setAmount(obligation.getMainAccount().getBalance());
                        if (obligation.getEndTime().isEqual(LocalDate.now())) {
                            obligation.setStatus(RecordStatus.END_OF_SERVICE);
                            obligation.getMainAccount().setStatus(RecordStatus.END_OF_SERVICE);
                            obligation.getPercentAccount().setStatus(RecordStatus.END_OF_SERVICE);
                        }
                    }
                });
    }

    private boolean checkIfCalcDay(LocalDate startTime, LocalDate endTime) {
        var now = LocalDate.now();
        LocalDate iterator = startTime;
        while (iterator.isBefore(endTime)){
            iterator = iterator.plus(1, ChronoUnit.MONTHS);
            if (iterator.isEqual(now))
                return true;
        }
        return false;
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

}
