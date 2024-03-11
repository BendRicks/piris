package ru.bendricks.piris.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.service.AccountService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/atm")
public class ATMController {

    private final AccountService accountService;

    @GetMapping("/main")
    public String getMainPage() {
        return "atm/main";
    }

    @GetMapping("/balance")
    public String getBalancePage(Model model) {
        var account = accountService.getAccountById(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("balance", account != null ? "Баланс: " + account.getBalance() / 100 : "Ошибка. Не удается найти счёт");
        return "atm/balance";
    }

    @GetMapping("/put_money")
    public String getPutMoneyPage() {
        return "atm/put_money";
    }

    @PostMapping("/put_money")
    @ResponseBody
    public void putMoney(@RequestBody Map<String, String> putData) throws Exception {
        var amount = Long.valueOf(putData.get("amount"));
        accountService.putMoneyCard(amount);
    }

    @GetMapping("/withdraw_money")
    public String getWithdrawMoneyPage() {
        return "atm/withdraw_money";
    }

    @PostMapping("/withdraw_money")
    @ResponseBody
    public void withdrawMoney(@RequestBody Map<String, String> putData) throws Exception {
        var amount = Long.valueOf(putData.get("amount"));
        accountService.withdrawMoneyCard(amount);
    }

}
