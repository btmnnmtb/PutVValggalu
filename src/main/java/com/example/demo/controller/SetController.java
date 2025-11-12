package com.example.demo.controller;

import com.example.demo.model.CosmeticItemForm;
import com.example.demo.model.CosmeticView;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.User;
import com.example.demo.repository.*;
import com.example.demo.service.AuditLogService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SetController {
    private final UsersRepository usersRepository;
    private final CosmeticItemRepository cosmeticItemRepository;
    private final CosmeticViewRepository cosmeticViewRepository;
    private final BrandsRepository brandsRepository;
    private final CosmeticTypesRepository cosmeticTypesRepository;
    private final ManufactureRepository manufactureRepository;
    private final QualityCertificRepository qualityCertificRepository;
    private final ProductStatusRepository productStatusRepository;
    private final AuditLogService auditLogService;

    // Константы статусов (совпадают с твоей БД)
    private static final int ST_APPROVED = 7;
    private static final int ST_PENDING = 8;
    private static final int ST_REWORK = 10;

    @GetMapping("/Set/approvals")
    public String approvals(Model model, Authentication auth,
                            @org.springframework.web.bind.annotation.RequestParam(value = "filter", required = false) String filter) {

        var username = auth.getName();
        var me = usersRepository.findByLogin(username).orElse(null);
        var role = me != null ? me.getRole().getRoleName().trim() : "неизвестно";

        long pendingCount = cosmeticItemRepository.countByProductStatusId(ST_PENDING);
        long approvedCount = cosmeticItemRepository.countByProductStatusId(ST_APPROVED);
        long rejectedCount = cosmeticItemRepository.countByProductStatusId(ST_REWORK);

        List<CosmeticView> requests;
        if ("approved".equalsIgnoreCase(filter)) {
            requests = cosmeticViewRepository.findAllByStatusNameOrderByCosmeticItemIdDesc("Одобрен");
        } else if ("rejected".equalsIgnoreCase(filter)) {
            requests = cosmeticViewRepository.findAllByStatusNameOrderByCosmeticItemIdDesc("Отказ(Переделать)");
        } else if ("pending".equalsIgnoreCase(filter)) {
            requests = cosmeticViewRepository.findAllByStatusNameOrderByCosmeticItemIdDesc("На рассмотренние");
        } else {
            requests = cosmeticViewRepository.findAllByStatusNameOrderByCosmeticItemIdDesc("На рассмотренние");
        }

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("items", cosmeticViewRepository.findAll());
        model.addAttribute("brands", brandsRepository.findAll());
        model.addAttribute("types", cosmeticTypesRepository.findAll());
        model.addAttribute("manufacturers", manufactureRepository.findAll());
        model.addAttribute("certificates", qualityCertificRepository.findAll());
        model.addAttribute("statuses", productStatusRepository.findAll());

        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);

        model.addAttribute("requests", requests);
        model.addAttribute("currentFilter", filter == null ? "pending" : filter);

        return "SetPage";
    }

    @PostMapping("/Set/approvals/{itemId}/approve")
    public String approve(@PathVariable Integer itemId,
                          @RequestParam(value = "note", required = false) String note,
                          RedirectAttributes ra,
                          Authentication auth,
                          HttpServletRequest request) {
        var actor = usersRepository.findByLogin(auth.getName()).orElse(null);

        var item = cosmeticItemRepository.findById(itemId).orElseThrow();


        var before = snapshotItem(item);

        item.setProductStatusId(ST_APPROVED);
        if (note != null && !note.isBlank()) {
            item.setModerationNote(note.trim());
        }
        cosmeticItemRepository.save(item);

        var after = snapshotItem(item);

        var details = auditLogService.diff(before, after);
        details.put("action", "approve");
        details.put("itemId", itemId);


        var target = (User) null;

        auditLogService.log(
                "Одобрил заявку на товар",
                actor,
                target,
                details
        );

        ra.addFlashAttribute("ok", "Товар #" + itemId + " одобрен");
        return "redirect:/Set/approvals?filter=pending";
    }
    @PostMapping("/Set/approvals/{itemId}/supply")
    public String supply(@PathVariable Integer itemId,
                         @RequestParam("qty") int qty,
                         Authentication auth,
                         HttpServletRequest request,
                         RedirectAttributes ra) {

        if (qty < 1) {
            ra.addFlashAttribute("err", "Количество должно быть ≥ 1");
            return "redirect:/Set/approvals?filter=approved";
        }

        var actor = usersRepository.findByLogin(auth.getName()).orElse(null);

        var item = cosmeticItemRepository.findById(itemId).orElseThrow();

        if (item.getProductStatusId() != ST_APPROVED) {
            ra.addFlashAttribute("err", "Пополнение возможно только для одобренных товаров");
            return "redirect:/Set/approvals?filter=pending";
        }


        var before = snapshotItem(item);

        item.setQuantity(item.getQuantity() + qty);
        item.setProductStatusId(ST_APPROVED);
        cosmeticItemRepository.save(item);


        var after = snapshotItem(item);

        var details = auditLogService.diff(before, after);
        details.put("action", "supply");
        details.put("itemId", itemId);
        details.put("addedQty", qty);

        auditLogService.log(
                "Пополнил остаток товара",
                actor,
                null,
                details
        );

        ra.addFlashAttribute("ok", "Товар #" + itemId + " пополнен на +" + qty);
        return "redirect:/Set/approvals?filter=approved";
    }
    @PostMapping("/Set/approvals/{itemId}/reject")
    public String reject(@PathVariable Integer itemId,
                         @RequestParam(value = "note", required = false) String note,
                         RedirectAttributes ra,
                         Authentication auth,
                         HttpServletRequest request) {

        var actor = usersRepository.findByLogin(auth.getName()).orElse(null);
        var item  = cosmeticItemRepository.findById(itemId).orElseThrow();

        var before = snapshotItem(item);

        item.setProductStatusId(ST_REWORK);
        if (note != null && !note.isBlank()) {
            item.setModerationNote(note.trim());
        }
        cosmeticItemRepository.save(item);

        var after = snapshotItem(item);

        var details = auditLogService.diff(before, after);
        details.put("action", "reject");
        details.put("itemId", itemId);

        auditLogService.log(
                "Отклонил заявку на товар",
                actor,
                null,
                details
        );

        ra.addFlashAttribute("ok", "Товар #" + itemId + " отправлен на переделку");
        return "redirect:/Set/approvals?filter=pending";
    }

    private static ObjectNode snapshotItem(Cosmetic_items item) {
        var o = JsonNodeFactory.instance.objectNode();
        o.put("cosmetic_item_id", item.getCosmeticItemId());
        o.put("name", item.getItemName());
        o.put("status_id", item.getProductStatusId());
        o.put("quantity", item.getQuantity());
        if (item.getPrice() != null) o.put("price", item.getPrice().doubleValue());
        o.put("brand_id", item.getBrandId());
        o.put("type_id", item.getCosmeticTypeId());
        o.put("manufacturer_id", item.getManufacturerId());
        o.put("certificate_id", item.getCertificateId());
        if (item.getModerationNote() != null) o.put("moderation_note", item.getModerationNote());
        return o;
    }


}