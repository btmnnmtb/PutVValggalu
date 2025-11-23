package com.example.demo.controller;

import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthorizationController {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private AuthService authService;

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", rolesRepository.findAll());
        return "registration";
    }
    @PostMapping("/registration")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", rolesRepository.findAll());
            return "registration";
        }
        if (usersRepository.findByLogin(user.getLogin()).isPresent()) {
            model.addAttribute("roles", rolesRepository.findAll());
            model.addAttribute("loginExists", "Пользователь с таким логином уже существует");
            return "registration";
        }

        Roles autorole = rolesRepository.findByRoleName("Пользователь")
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
        user.setRole(autorole);

        authService.registerUser(user.getLogin(), user.getRawPassword(), autorole.getRoleName());

        return "redirect:/login";
    }
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Неверный логин или пароль");
        }
        return "login";
    }

}
