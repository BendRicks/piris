package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.service.AccountService;
import ru.bendricks.piris.service.ObligationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/obligations")
public class ObligationController {

    private final ObligationService obligationService;
    private final AccountService accountService;

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<Obligation>> getCurrentUserObligations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var obligations = obligationService.getObligationsByUserId(userDetails.getId());
        return Map.of("deposit", obligations.stream().filter(obligation -> (obligation.getObligationType() == ObligationType.DEPOSIT || obligation.getObligationType() == ObligationType.DEPOSIT_UNTOUCH) && obligation.getStatus() != RecordStatus.CLOSED).toList(),
                "credit", obligations.stream().filter(obligation -> (obligation.getObligationType() == ObligationType.CREDIT || obligation.getObligationType() == ObligationType.CREDIT_ANUAL) && obligation.getStatus() != RecordStatus.CLOSED).toList());
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public Obligation createObligation(@RequestBody @Valid ObligationCreateDTO obligationCreateDTO, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        var obligationPlan = obligationService.getObligationTypeById(obligationCreateDTO.getObligation().getObligationPlan().getId());
        if (obligationPlan.getObligationType() == ObligationType.DEPOSIT || obligationPlan.getObligationType() == ObligationType.DEPOSIT_UNTOUCH)
            return obligationService.createDepositObligation(obligationCreateDTO, userDetails);
        else
            return obligationService.createCreditObligation(obligationCreateDTO, userDetails);
    }

    @GetMapping("/deposit/plans")
    public List<ObligationPlan> getDepositPlans() {
        return obligationService.getObligationPlansByObligationType(ObligationType.DEPOSIT, ObligationType.DEPOSIT_UNTOUCH);
    }

    @GetMapping("/credit/plans")
    public List<ObligationPlan> getCreditPlans() {
        return obligationService.getObligationPlansByObligationType(ObligationType.CREDIT, ObligationType.CREDIT_ANUAL);
    }

    @GetMapping("/user/{id}/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, List<Obligation>> getUserObligations(@PathVariable(name = "id") Long userId) {
        var obligations = obligationService.getObligationsByUserId(userId);
        return Map.of("deposit", obligations.stream().filter(obligation -> obligation.getObligationType() == ObligationType.DEPOSIT || obligation.getObligationType() == ObligationType.DEPOSIT_UNTOUCH).toList(),
                "credit", obligations.stream().filter(obligation -> obligation.getObligationType() == ObligationType.CREDIT || obligation.getObligationType() == ObligationType.CREDIT_ANUAL).toList());
    }

    @GetMapping("/{oblId}/payments")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView getPaymentsOnCredit(@PathVariable(name = "oblId") Long oblId) {
        var model = new HashMap<String, Object>();
//        mode
        return new ModelAndView("user/payments", model);
    }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Obligation getObligation(@PathVariable(name = "id") Long obligationId) {
        return obligationService.getObligationById(obligationId);
    }

}
