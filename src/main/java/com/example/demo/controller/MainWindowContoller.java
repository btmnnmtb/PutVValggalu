package com.example.demo.controller;

import com.example.demo.model.Brands;
import com.example.demo.model.User;
import com.example.demo.repository.BrandsRepository;
import com.example.demo.repository.CosmeticViewRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class MainWindowContoller {
    private final CosmeticViewRepository cosmeticViewRepository;
    private final UsersRepository usersRepository;
    private final BrandsRepository brandsRepository;
    @GetMapping("MainWindow")
    private String mainWindow(Model model , Authentication authentication) {
        String username = authentication.getName();
        User user = usersRepository.findByLogin(username).orElse(null);
        String role = (user != null) ? user.getRole().getRoleName().trim() : "неизвестно";
        model.addAttribute("username", username);
        model.addAttribute("roles", role);
        List<Brands> brands = brandsRepository.findAll();
        model.addAttribute("brands", brands);

        return "MainWindow";
    }
}
