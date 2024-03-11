package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.model.Account;
import ru.bendricks.piris.model.Currency;
import ru.bendricks.piris.model.RecordStatus;
import ru.bendricks.piris.model.Transaction;
import ru.bendricks.piris.service.AccountService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping(value = "/transaction/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Transaction createTransaction(@RequestBody @Valid Transaction transaction, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        return accountService.transferMoney(transaction, userDetails);
    }

    @GetMapping("/my")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<Account>> getCurrentUserAccounts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var allAccounts = accountService.getAccountsByUserId(userDetails.getId());
        return Map.of("payment", allAccounts.stream().filter(account -> (account.getAccountType().getCode() == 3014 || account.getAccountType().getCode() == 7327) && account.getStatus() != RecordStatus.CLOSED).toList(),
                "deposit", allAccounts.stream().filter(account -> (account.getAccountType().getCode() == 3404 || account.getAccountType().getCode() == 3470) && account.getStatus() != RecordStatus.CLOSED).toList(),
                "credit", allAccounts.stream().filter(account -> (account.getAccountType().getCode() == 2400 || account.getAccountType().getCode() == 2470) && account.getStatus() != RecordStatus.CLOSED).toList());
    }

    @GetMapping("/user/{id}/all")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<Account>> getUserAccounts(@PathVariable(name = "id") Long userId) {
        var allAccounts = accountService.getAccountsByUserId(userId);
        return Map.of("payment", allAccounts.stream().filter(account -> account.getAccountType().getCode() == 3014 && account.getStatus() != RecordStatus.CLOSED).toList(),
                "deposit", allAccounts.stream().filter(account -> (account.getAccountType().getCode() == 3404 || account.getAccountType().getCode() == 3470) && account.getStatus() != RecordStatus.CLOSED).toList(),
                "credit", allAccounts.stream().filter(account -> (account.getAccountType().getCode() == 2400 || account.getAccountType().getCode() == 2470) && account.getStatus() != RecordStatus.CLOSED).toList());
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

    @PostMapping("/{iban}/card/issue")
    public void issueCard(@PathVariable(name = "iban") String iban) throws Exception {
        accountService.createPaymentCard(iban);
    }

    @PostMapping("/card/{cardNo}/update_credentials")
    public Map<String, String> updateCardCredentials(@PathVariable(name = "cardNo") Long cardNo, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        return Map.of("message", accountService.updateCardCredentials(cardNo, userDetails));
    }

}
