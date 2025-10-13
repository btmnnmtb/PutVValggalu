package com.example.demo.controller;

import com.example.demo.repository.CosmeticViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CosmeticController {

    private final CosmeticViewRepository cosmeticViewRepository;

    @GetMapping("/TestPage")
    public String testPage(Model model) {
        model.addAttribute("items", cosmeticViewRepository.findAll());
        return "TestPage";
    }


}
