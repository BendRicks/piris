package ru.bendricks.piris.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bendricks.piris.model.User;
import ru.bendricks.piris.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;



}
