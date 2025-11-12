package com.example.demo.controller;

import com.example.demo.model.ForgotForm;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserService userService;
    @ModelAttribute("forgotForm")
    public ForgotForm forgotForm() { return new ForgotForm(); }

    @GetMapping("/ForgotPassword")
    public String forgotPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) model.addAttribute("errorMessage", "Неверный логин");
        return "ForgotPassword";
    }

    @PostMapping("/forgotSubmit")
    public String forgotSubmit(@RequestParam String login,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Пароли не совпадают");
            return "ForgotPassword";
        }
        try {
            boolean updated = userService.updatePasswordByLogin(login, newPassword);
            if (!updated) {
                model.addAttribute("errorMessage", "Пользователь с таким логином не найден");
                return "ForgotPassword";
            }
            model.addAttribute("successMessage", "Пароль обновлён. Войдите с новым паролем.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "ForgotPassword";
        }
    }

}