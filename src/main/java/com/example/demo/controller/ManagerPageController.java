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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private final  CosmeticItemRepository cosmeticItemRepository;

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
    @GetMapping("/manager/reports/export/items-template.csv")
    public void exportItemsTemplate(HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"items-template.csv\"");

        try (var writer = new java.io.PrintWriter(resp.getWriter())) {


            writer.println("name;price;qty;category;brand;manufacturer;certificate;status");

            var items = cosmeticViewRepository.findAll();

            for (CosmeticView it : items) {

                String name = safe(it.getItemName());
                String price = it.getPrice() != null ? it.getPrice().toPlainString() : "0";
                String qty = it.getQuantity() != null ? it.getQuantity().toString() : "0";

                String category = safe(it.getCosmeticTypeName());
                String brand = safe(it.getBrandName());
                String manufacturer = safe(it.getManufacturerName());
                String certificate = safe(it.getCertificateName());
                String status = safe(it.getStatusName());

                writer.printf(
                        "%s;%s;%s;%s;%s;%s;%s;%s%n",
                        name, price, qty, category, brand, manufacturer, certificate, status
                );
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s.replace(";", ",");
    }

    @PostMapping("/manager/reports/import/items")
    public String importItems(@RequestParam("file") MultipartFile file,
                              RedirectAttributes ra) {

        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Файл не выбран");
            return "redirect:/manager/ManagerPage";
        }

        int imported = 0, skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {

                if (first) { first = false; continue; }
                if (line.isBlank()) continue;

                String[] p = line.split(";");
                if (p.length < 8) { skipped++; continue; }

                String name = p[0].trim();
                BigDecimal price;
                Integer qty;

                try {
                    price = new BigDecimal(p[1].trim().replace(",", "."));
                    qty = Integer.parseInt(p[2].trim());
                } catch (Exception ex) {
                    skipped++;
                    continue;
                }

                String cat = p[3].trim();
                String brand = p[4].trim();
                String manu = p[5].trim();
                String cert = p[6].trim();
                String status = p[7].trim();

                // поиск справочных значений
                var typeOpt = cosmeticTypesRepository.findByCosmeticTypeName(cat);
                var brandOpt = brandsRepository.findByBrandName(brand);
                var manuOpt = manufactureRepository.findByManufacturerName(manu);
                var certOpt = qualityCertificRepository.findByCertificateName(cert);
                var statOpt = productStatusRepository.findByStatusName(status);

                if (typeOpt.isEmpty() || brandOpt.isEmpty() ||
                        manuOpt.isEmpty() || certOpt.isEmpty() || statOpt.isEmpty()) {
                    skipped++;
                    continue;
                }

                Cosmetic_items item = cosmeticItemRepository
                        .findByItemName(name)
                        .orElse(new Cosmetic_items());

                item.setItemName(name);
                item.setPrice(price);
                item.setQuantity(qty);

                item.setCosmeticTypeId(typeOpt.get().getCosmeticTypeId());
                item.setBrandId(brandOpt.get().getBrandId());
                item.setManufacturerId(manuOpt.get().getManufacturerId());
                item.setCertificateId(certOpt.get().getCertificateId());
                item.setProductStatusId(statOpt.get().getProductStatusId());

                cosmeticItemRepository.save(item);
                imported++;
            }

            ra.addFlashAttribute("msg",
                    "Импорт завершён. Успешно: " + imported + ", пропущено: " + skipped);

        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Ошибка импорта: " + ex.getMessage());
        }

        return "redirect:/manager/ManagerPage";
    }
}

