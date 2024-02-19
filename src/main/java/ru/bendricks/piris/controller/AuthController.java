package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.model.Sex;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.UserService;
import ru.bendricks.piris.service.UserValidator;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserValidator userValidator;

    @GetMapping("/signup")
    public String getRegisterPage(User user, Model model){
        model.addAttribute("url", "/auth/signup");
        return "auth/signup";
    }

    @GetMapping("/edit/{id}")
    public String getRegisterPage(Model model, @PathVariable(name = "id") long id){
        model.addAttribute("url", "/auth/edit");
        model.addAttribute("user", userService.getUserById(id));
        return "auth/signup";
    }

    @PostMapping("/edit")
    public String editUser(@Valid User user, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors())
            return "auth/signup";
        userService.registerUser(user);
        return "redirect:/piris/users";
    }

    @PostMapping("/signup")
    public String registerUser(@Valid User user, BindingResult bindingResult, Model model){
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors())
            return "auth/signup";
        userService.registerUser(user);
        return "redirect:/piris/users";
    }

    @GetMapping("/signin")
    public String getLoginPage(){
        return "auth/signin";
    }

    @DeleteMapping("/user/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable("id") long id) {
        userService.deleteUser(id);
    }

}
