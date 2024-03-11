package ru.bendricks.piris.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.config.CustomUserDetails;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.AccountService;
import ru.bendricks.piris.service.UserService;
import ru.bendricks.piris.service.UserValidator;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;

    @GetMapping("/signin")
    public String getLoginPage(){
        return "auth/signin";
    }

    @GetMapping("/atm/signin")
    public String getAtmLoginPage(){
        return "atm/atm_signin";
    }

    @PostMapping("/atm/signin")
    public String atmSignIn(HttpServletRequest req, @RequestBody MultiValueMap<String, String> cardData, Model model) {
        return accountService.authenticateCard(req, cardData, model) ? "redirect:/atm/main" : "atm/atm_signin";
    }

    @GetMapping("/me")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public User getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.user();
        user.setPasswordHash(null);
        return user;
    }

}
