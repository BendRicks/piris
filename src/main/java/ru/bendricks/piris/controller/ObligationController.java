package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.*;
import ru.bendricks.piris.service.AccountService;
import ru.bendricks.piris.service.ObligationService;

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
        return Map.of("deposit", obligations.stream().filter(obligation -> obligation.getObligationType() == ObligationType.DEPOSIT || obligation.getObligationType() == ObligationType.DEPOSIT_UNTOUCH).toList(),
                "credit", obligations.stream().filter(obligation -> obligation.getObligationType() == ObligationType.CREDIT || obligation.getObligationType() == ObligationType.CREDIT_ANUAL).toList());
    }

    @PostMapping("/deposit/create")
    @ResponseStatus(HttpStatus.OK)
    public Obligation createDeposit(@RequestBody @Valid ObligationCreateDTO obligationCreateDTO) {
        return obligationService.createDepositObligation(obligationCreateDTO);
    }

//    @GetMapping("/deposit/create")
//    public String getObligationCreatePage(@RequestParam("userId") Long userId, Model model) {
//        model.addAttribute("obligation", ObligationCreateDTO.builder().obligation(
//                Obligation.builder().owner(User.builder().id(userId).build()).contractNumber(generateContractNumber()).build()
//        ).build());
//        model.addAttribute("url", "/obligation/deposit/create");
//        model.addAttribute("ibans", );
//        model.addAttribute("plans", );
//        return "obligation/obligation_create";
//    }

    @GetMapping("/deposit/plans")
    public List<ObligationPlan> getDepositPlans() {
        return obligationService.getObligationPlansByObligationType(ObligationType.DEPOSIT, ObligationType.DEPOSIT_UNTOUCH);
    }

    @GetMapping("/credit/plans")
    public List<ObligationPlan> getCreditPlans() {
        return obligationService.getObligationPlansByObligationType(ObligationType.CREDIT, ObligationType.CREDIT_ANUAL);
    }

//    @GetMapping("/user/{id}")
//    public String getUserObligationsPage(@PathVariable(name = "id") Long userId, Model model) {
//        model.addAttribute("userId", userId);
//        return "obligation/obligations";
//    }


    @GetMapping("/user/{id}/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Obligation> getUserObligations(@PathVariable(name = "id") Long userId) {
        return obligationService.getObligationsByUserId(userId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Obligation getObligation(@PathVariable(name = "id") Long obligationId) {
        return obligationService.getObligationById(obligationId);
    }

}
