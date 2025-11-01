package com.example.demo.controller;

import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class OrderContoller {

    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
        @GetMapping("/orders")
        public String listMyOrders (Authentication auth, Model model){
            var username = auth.getName();
            var user = usersRepository.findByLogin(username).orElseThrow();
            var role = user.getRole().getRoleName() == null ? "" : user.getRole().getRoleName().trim();

            var isAdmin = role.equalsIgnoreCase("Администратор");

            var orders = isAdmin
                    ? orderRepository.findAllWithItemsOrderByOrderDateDesc()
                    : orderRepository.findAllByUser_LoginOrderByOrderDateDesc(username);

            long total = orders.size();
            long processing = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().getOrderStatus() != null)
                    .filter(o -> o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Собираеться")
                            || o.getOrderStatus().getOrderStatus().equalsIgnoreCase("В обработке"))
                    .count();

            long delivered = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().getOrderStatus() != null)
                    .filter(o -> o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Готов к выдачи")
                            || o.getOrderStatus().getOrderStatus().equalsIgnoreCase("Доставлено"))
                    .count();

            model.addAttribute("orders", orders);
            model.addAttribute("ordersTotal", total);
            model.addAttribute("ordersProcessing", processing);
            model.addAttribute("ordersDelivered", delivered);

            model.addAttribute("username", username);
            model.addAttribute("role", role);
            model.addAttribute("pageTitle", isAdmin ? "Все заказы" : "Мои заказы");

            return "Order";
        }
    }
