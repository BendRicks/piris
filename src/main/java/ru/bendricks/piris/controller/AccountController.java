package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.model.Account;
import ru.bendricks.piris.model.Currency;
import ru.bendricks.piris.service.AccountService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<Account>> getCurrentUserAccounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var allAccounts = accountService.getAccountsByUserId(userDetails.getId());
        return Map.of("payment", allAccounts.stream().filter(account -> account.getAccountType().getCode() == 3014).toList(),
                "deposit", allAccounts.stream().filter(account -> account.getAccountType().getCode() == 3404 || account.getAccountType().getCode() == 3470).toList(),
                "credit", allAccounts.stream().filter(account -> account.getAccountType().getCode() == 2400 || account.getAccountType().getCode() == 2470).toList());
    }

    @GetMapping("/user/{id}/all")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public List<Account> getUserAccounts(@PathVariable(name = "id") Long userId) {
        return accountService.getAccountsByUserId(userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public Account getAccountById(@PathVariable(name = "id") String iban) {
        return accountService.getAccountById(iban);
    }

    @PostMapping("/payment/create")
    @ResponseStatus(HttpStatus.OK)
    public Account createPaymentAccount(@RequestBody @Valid Account account) {
        return accountService.createPaymentAccount(account);
    }

    @GetMapping("/user/{id}/payment")
    public List<Account> getPaymentAccounts(@PathVariable(name = "id") Long userId, @RequestParam(name = "currency") Currency currency) {
        return accountService.getPaymentAccounts(userId, currency);
    }

}
