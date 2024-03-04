package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.dto.ObligationCreateDTO;
import ru.bendricks.piris.model.Account;
import ru.bendricks.piris.model.Obligation;
import ru.bendricks.piris.model.ObligationType;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.AccountService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/payment/create")
    @ResponseBody
    public Account createDeposit(@RequestBody @Valid Account account) throws Exception {
        return accountService.createPaymentAccount(account);
    }

    @GetMapping("/payment/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String getObligationCreatePage(@RequestParam("userId") Long userId, Model model) {
        model.addAttribute("url", "/obligation/deposit/create");
        model.addAttribute("account", Account.builder().owner(User.builder().id(userId).build()).build());
        return "account/account_create";
    }

}
