package ru.bendricks.piris.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.model.ObligationType;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.ObligationService;
import ru.bendricks.piris.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private final ObligationService obligationService;

    @GetMapping("/main")
    public String getUserPage() {
        return "user/users_page";
    }

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        model.addAttribute("plans", obligationService.getObligationPlansByObligationType(ObligationType.DEPOSIT, ObligationType.DEPOSIT_UNTOUCH, ObligationType.CREDIT, ObligationType.CREDIT_ANUAL));
        return "admin/admin_page";
    }

}
