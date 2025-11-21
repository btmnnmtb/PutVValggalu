package com.example.demo.controller;

import com.example.demo.model.Order_status;
import com.example.demo.model.Orders;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderStatusRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.AuditLogService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderContoller {

    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final AuditLogService auditLogService;
    @GetMapping("/orders")
    public String listMyOrders(Authentication auth,
                               @RequestParam(required = false) String q,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String period,
                               Model model) {

        var username = auth.getName();
        var user = usersRepository.findByLogin(username).orElseThrow();
        var role = user.getRole().getRoleName() == null ? "" : user.getRole().getRoleName().trim();

        var isAdmin = role.equalsIgnoreCase("Администратор");
        var isSet = role.equalsIgnoreCase("Сотрудник склада");

        List<Orders> orders = (isAdmin || isSet)
                ? orderRepository.findAllWithItemsOrderByOrderDateDesc()
                : orderRepository.findAllByUser_LoginOrderByOrderDateDesc(username);

        if (q != null && !q.isBlank()) {
            String qLower = q.toLowerCase();

            orders = orders.stream()
                    .filter(o ->
                            // по номеру
                            String.valueOf(o.getOrderId()).contains(qLower)
                                    ||
                                    // по названию товара
                                    (o.getItems() != null && o.getItems().stream().anyMatch(it ->
                                            it.getCosmeticItem() != null &&
                                                    it.getCosmeticItem().getItemName() != null &&
                                                    it.getCosmeticItem().getItemName().toLowerCase().contains(qLower)
                                    ))
                    )
                    .toList();
        }

        if (status != null && !status.isBlank()) {
            orders = orders.stream()
                    .filter(o -> o.getOrderStatus() != null
                            && status.equals(o.getOrderStatus().getOrderStatus()))
                    .toList();
        }

        if (period != null && !period.isBlank()) {
            LocalDateTime from = switch (period) {
                case "week"  -> LocalDateTime.now().minusWeeks(1);
                case "month" -> LocalDateTime.now().minusMonths(1);
                case "year"  -> LocalDateTime.now().minusYears(1);
                default -> null;
            };

            if (from != null) {
                orders = orders.stream()
                        .filter(o -> {
                            if (o.getOrderDate() == null) return false;
                            return o.getOrderDate().toInstant().isAfter(from.atZone(java.time.ZoneId.systemDefault()).toInstant());
                        })
                        .toList();
            }
        }

        long total = orders.size();
        long processing = orders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().getOrderStatus() != null)
                .filter(o -> o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Собираеться")
                        || o.getOrderStatus().getOrderStatus().equalsIgnoreCase("В обработке"))
                .count();

        long delivered = orders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().getOrderStatus() != null)
                .filter(o -> o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Готов к выдачи")
                        || o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Доставлено")
                        || o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Завершен"))
                .count();

        List<Order_status> statuses = orderStatusRepository.findAll();

        // В модель:
        model.addAttribute("orders", orders);
        model.addAttribute("ordersTotal", total);
        model.addAttribute("ordersProcessing", processing);
        model.addAttribute("ordersDelivered", delivered);

        model.addAttribute("statuses", statuses);

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("period", period);

        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("pageTitle", isAdmin ? "Все заказы" : "Мои заказы");

        return "Order"; // твой шаблон
    }

    @GetMapping("/manager/OrderManager")
    public String listAllOrders(Authentication auth, Model model) {
        var username = auth.getName();
        var user = usersRepository.findByLogin(username).orElseThrow();
        var role = user.getRole().getRoleName() == null ? "" : user.getRole().getRoleName().trim();

        var orders = orderRepository.findAllWithItemsOrderByOrderDateDesc();
        final String S_PROCESS = "Собираеться";
        final String S_READY   = "Готов к выдачи";
        final String S_DONE    = "Завершен";

        long total      = orderRepository.count();
        long processing = orderRepository.countByOrderStatus_OrderStatusIgnoreCase(S_PROCESS);
        long ready      = orderRepository.countByOrderStatus_OrderStatusIgnoreCase(S_READY);
        long completed  = orderRepository.countByOrderStatus_OrderStatusIgnoreCase(S_DONE);



        model.addAttribute("orders", orders);
        model.addAttribute("ordersTotal", total);
        model.addAttribute("ordersProcessing", processing);
        model.addAttribute("ordersReady", ready);
        model.addAttribute("ordersCompleted", completed);
        model.addAttribute("statuses", orderStatusRepository.findAll());
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("pageTitle", "Управление заказами");

        return "OrderManager";
    }
    @PostMapping("/manager/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam("statusId") Integer statusId,
            RedirectAttributes ra,
            Authentication auth,
            HttpServletRequest request) {

        Orders order = orderRepository.findById(id).orElseThrow();
        Order_status newStatus = orderStatusRepository.findById(statusId).orElseThrow();


        var actor = usersRepository.findByLogin(auth.getName()).orElse(null);


        var target = order.getUser();


        var before = JsonNodeFactory.instance.objectNode();
        before.put("order_id", order.getOrderId());
        before.put("status_id", order.getOrderStatus() == null ? null : order.getOrderStatus().getOrdersStatusId());
        before.put("status_name", order.getOrderStatus() == null ? null : order.getOrderStatus().getOrderStatus());


        order.setOrderStatus(newStatus);
        orderRepository.save(order);


        var after = JsonNodeFactory.instance.objectNode();
        after.put("order_id", order.getOrderId());
        after.put("status_id", newStatus.getOrdersStatusId());
        after.put("status_name", newStatus.getOrderStatus());


        var details = auditLogService.diff(before, after);
        details.put("action", "update_order_status");
        details.put("orderId", order.getOrderId());
        details.put("oldStatusName", before.get("status_name").isNull() ? null : before.get("status_name").asText());
        details.put("newStatusName", newStatus.getOrderStatus());


        auditLogService.log(
                "Изменил статус заказа",
                actor,
                target,
                details
        );

        ra.addFlashAttribute("ok", "Статус заказа №" + id + " обновлён");
        return "redirect:/manager/OrderManager";
    }


    private static String mapStatusKeyToDb(String key){
        if (key == null || key.isBlank()) return null;
        return switch (key) {
            case "processing" -> "Собираеться";
            case "shipped"    -> "Готов к выдачи";
            case "delivered"  -> "Завершен";

            default -> null;
        };
    }




}
