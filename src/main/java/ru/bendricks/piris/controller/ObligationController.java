package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.Obligation;
import ru.bendricks.piris.model.ObligationType;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.AccountService;
import ru.bendricks.piris.service.ObligationService;

import java.util.List;
import java.util.Random;

@Controller
@RequiredArgsConstructor
@RequestMapping("/obligation")
public class ObligationController {

    private final ObligationService obligationService;
    private final AccountService accountService;

    @PostMapping("/deposit/create")
    @ResponseBody
    public Obligation createDeposit(@RequestBody @Valid ObligationCreateDTO obligationCreateDTO) throws Exception {
        return obligationService.createDepositObligation(obligationCreateDTO);
    }

    @GetMapping("/deposit/create")
    public String getObligationCreatePage(@RequestParam("userId") Long userId, Model model) {
        model.addAttribute("obligation", ObligationCreateDTO.builder().obligation(
                Obligation.builder().owner(User.builder().id(userId).build()).contractNumber(generateContractNumber()).build()
        ).build());
        model.addAttribute("url", "/obligation/deposit/create");
        model.addAttribute("ibans", accountService.getPaymentIbans(userId));
        model.addAttribute("plans", obligationService.getObligationPlansByObligationType(ObligationType.DEPOSIT, ObligationType.DEPOSIT_UNTOUCH));
        return "obligation/obligation_create";
    }

    private String generateContractNumber() {
        var rand = new Random();
        StringBuilder sb;
        do {
            sb = new StringBuilder();
            sb.append(getInt(rand)).append(getInt(rand)).append(getInt(rand)).append(getInt(rand))
                    .append(getInt(rand)).append(getInt(rand)).append(getInt(rand)).append(getInt(rand))
                    .append(getInt(rand)).append(getInt(rand)).append(getInt(rand)).append(getInt(rand));
        } while (obligationService.existsByContractNumber(sb.toString()));
        return sb.toString();
    }

    private int getInt(Random rand) {
        return Math.abs(rand.nextInt() % 10);
    }

    @GetMapping("/user/{id}")
    public String getUserObligationsPage(@PathVariable(name = "id") Long userId, Model model) {
        model.addAttribute("userId", userId);
        return "obligation/obligations";
    }


    @GetMapping("/user/{id}/all")
    @ResponseBody
    public List<Obligation> getUserObligations(@PathVariable(name = "id") Long userId) {
        return obligationService.getObligationsByUserId(userId);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Obligation getObligation(@PathVariable(name = "id") Long obligationId) {
        return obligationService.getObligationById(obligationId);
    }

}
