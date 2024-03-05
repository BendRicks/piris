package ru.bendricks.piris.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.UserService;
import ru.bendricks.piris.service.UserValidator;

import java.lang.reflect.Method;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserValidator userValidator;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable("id") long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public User registerUser(@Valid @RequestBody User user, BindingResult bindingResult, Model model) throws MethodArgumentNotValidException, NoSuchMethodException {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors())
            throw new MethodArgumentNotValidException(
                    new MethodParameter(UserController.class.getMethod("registerUser", User.class, BindingResult.class, Model.class), 0), bindingResult
            );
        userService.registerUser(user);
        user.setPasswordHash(null);
        return user;
    }

    @PostMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public User editUser(@Valid @RequestBody User user, BindingResult bindingResult, Model model) throws NoSuchMethodException, MethodArgumentNotValidException {
        if (bindingResult.hasErrors())
            throw new MethodArgumentNotValidException(
                    new MethodParameter(UserController.class.getMethod("editUser", User.class, BindingResult.class, Model.class), 0), bindingResult
            );
        userService.registerUser(user);
        user.setPasswordHash(null);
        return user;
    }

    @GetMapping("/get_all_users")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers(){
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setPasswordHash(null));
        return users;
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable(name = "id") long id){
        User user = userService.getUserById(id);
        user.setPasswordHash(null);
        return user;
    }

    //    @GetMapping("/edit/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public String getRegisterPage(Model model, @PathVariable(name = "id") long id){
//        model.addAttribute("url", "/auth/edit");
//        model.addAttribute("user", userService.getUserById(id));
//        return "auth/signup";
//    }

//    @GetMapping("/create")
//    @PreAuthorize("hasRole('ADMIN')")
//    public String getRegisterPage(User user, Model model){
//        model.addAttribute("url", "/auth/signup");
//        return "auth/signup";
//    }

}
