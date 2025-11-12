package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.CartService;
import com.example.demo.service.CommentsService;
import com.example.demo.service.CosmeticItemsService;
import com.example.demo.service.FavourService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequiredArgsConstructor
public class ManagerPageController {

    private final CosmeticViewRepository cosmeticViewRepository;
    private final UsersRepository usersRepository;
    private final BrandsRepository brandsRepository;
    private final CosmeticTypesRepository cosmeticTypesRepository;
    private final ManufactureRepository manufactureRepository;
    private final QualityCertificRepository qualityCertificRepository;
    private final ProductStatusRepository productStatusRepository;

    private final CosmeticItemsService cosmeticItemsService;
    private HttpServletResponse resp ;

    @GetMapping("manager/ManagerPage")
    public String managerPage(Model model, Authentication auth) {
        var username = auth.getName();
        var me = usersRepository.findByLogin(username).orElse(null);
        var role = me != null ? me.getRole().getRoleName().trim() : "неизвестно";

        model.addAttribute("username", username);
        model.addAttribute("role", role);

        model.addAttribute("items", cosmeticViewRepository.findAll());
        model.addAttribute("brands", brandsRepository.findAll());
        model.addAttribute("types", cosmeticTypesRepository.findAll());
        model.addAttribute("manufacturers", manufactureRepository.findAll());
        model.addAttribute("certificates", qualityCertificRepository.findAll());
        model.addAttribute("statuses", productStatusRepository.findAll());
        try {
            var stats = cosmeticItemsService.getInventoryStats();
            model.addAttribute("stats", stats);
        } catch (Exception ignore) {
        }

        if (!model.containsAttribute("form")) model.addAttribute("form", new CosmeticItemForm());
        return "ManagerPage";
    }

    @PostMapping("/manager/add")
    public String add(@Valid @ModelAttribute("form") CosmeticItemForm form,
                      BindingResult br,
                      RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("openAddModal", true);
            return "redirect:/manager/ManagerPage";
        }
        try {
            cosmeticItemsService.create(form);
            ra.addFlashAttribute("msg", "Товар добавлен");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("openAddModal", true);
        }
        return "redirect:/manager/ManagerPage";
    }

    @PostMapping("/manager/edit")
    public String update(@RequestParam Integer id,
                         @Valid @ModelAttribute("form") CosmeticItemForm form,
                         BindingResult br,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("openEditModal", true);
            ra.addFlashAttribute("editId", id);
            return "redirect:/manager/ManagerPage";
        }
        try {
            cosmeticItemsService.update(id, form);
            ra.addFlashAttribute("msg", "Изменения сохранены");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("openEditModal", true);
            ra.addFlashAttribute("editId", id);
        }
        return "redirect:/manager/ManagerPage";
    }

    @PostMapping("/manager/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            cosmeticItemsService.delete(id);
            ra.addFlashAttribute("msg", "Товар удалён");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Нельзя удалить: " + ex.getMessage());
        }
        return "redirect:/manager/ManagerPage";
    }
    @GetMapping("/manager/reports/export/all.zip")
    public void exportAllZip(HttpServletResponse resp) throws IOException {
        this.resp = resp;
        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition", "attachment; filename=\"reports-all.zip\"");

        try (ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream())) {
            // 1) by-category.csv
            zos.putNextEntry(new ZipEntry("by-category.csv"));
            byte[] byCategory = loadCsv("/manager/reports/export/by-category.csv");
            zos.write(byCategory);
            zos.closeEntry();

            // 2) stock-buckets.csv
            zos.putNextEntry(new ZipEntry("stock-buckets.csv"));
            byte[] stockBuckets = loadCsv("/manager/reports/export/stock-buckets.csv");
            zos.write(stockBuckets);
            zos.closeEntry();

            // 3) top-brands.csv
            zos.putNextEntry(new ZipEntry("top-brands.csv"));
            byte[] topBrands = loadCsv("/manager/reports/export/top-brands.csv");
            zos.write(topBrands);
            zos.closeEntry();

            zos.finish();
        }
    }
    private byte[] loadCsv(String path) throws IOException {
        URL url = new URL("http", "localhost", 8080, path); // порт подставь свой
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }
}
