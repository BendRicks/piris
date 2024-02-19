package ru.bendricks.piris.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/piris")
public class MainController {

    private final UserService userService;

    @GetMapping("/users")
    public String getUsersPage(Model model) {
        return "user/users_page";
    }

    @GetMapping("/get_all_users")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers(){
        List<User> users = userService.getAllUsers();
        users.forEach(user -> user.setPasswordHash(null));
        return users;
    }

    @GetMapping("/user/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable(name = "id") long id){
        User user = userService.getUserById(id);
        user.setPasswordHash(null);
        return user;
    }

}
