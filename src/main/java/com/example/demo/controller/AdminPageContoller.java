package com.example.demo.controller;

import com.example.demo.repository.LogsRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.demo.model.CreateUserDto;
import com.example.demo.model.UpdateUserDto;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminPageContoller {

    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserService userService;
    private final LogsRepository logsRepository;

    @GetMapping("/AdminPage")
    public String adminUsers(Authentication authentication, Model model) {
        var username = authentication.getName();
        var me = usersRepository.findByLogin(username).orElse(null);
        var role = me != null ? me.getRole().getRoleName().trim() : "неизвестно";

        model.addAttribute("username", username);
        model.addAttribute("role", role);

        model.addAttribute("users", usersRepository.findAll());
        model.addAttribute("usersCount", usersRepository.count());
        model.addAttribute("ordersCount", orderRepository.count());
        model.addAttribute("totalRevenue", orderRepository.sumTotalRevenue());
        model.addAttribute("roles", rolesRepository.findAll());
        model.addAttribute("logs",
                logsRepository.findAll(Sort.by(Sort.Direction.DESC, "logDate")));


        if (!model.containsAttribute("createUser")) model.addAttribute("createUser", new CreateUserDto());
        if (!model.containsAttribute("editUser"))   model.addAttribute("editUser", new UpdateUserDto());

        DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

        var startYm = YearMonth.now().minusMonths(11);
        var months = new ArrayList<YearMonth>(12);
        for (int i = 0; i < 12; i++) {
            months.add(startYm.plusMonths(i));
        }

        var fromTs = Timestamp.valueOf(startYm.atDay(1).atStartOfDay());
        System.out.println("fromTs = " + fromTs);

        List<OrderRepository.OrderMonthlyAgg> rows = orderRepository.statsRevenueByMonth(fromTs);

        System.out.println("=== rows from DB ===");
        for (var r : rows) {
            System.out.printf("ym=%s revenue=%s cnt=%s%n",
                    r.getYm(), r.getRevenue(), r.getCnt());
        }

        Map<YearMonth, OrderRepository.OrderMonthlyAgg> byYm = new HashMap<>();
        for (var r : rows) {
            try {
                YearMonth ym = YearMonth.parse(r.getYm(), YM_FMT);
                byYm.put(ym, r);
            } catch (Exception ex) {
                System.out.println("Cannot parse ym: " + r.getYm() + " -> " + ex);
            }
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> revenue = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        System.out.println("=== loop by months ===");
        for (var m : months) {
            labels.add(m.getMonthValue() + "." + m.getYear());
            var r = byYm.get(m);
            System.out.printf("month=%s found=%s revenue=%s cnt=%s%n",
                    m,
                    (r != null),
                    (r != null ? r.getRevenue() : BigDecimal.ZERO),
                    (r != null ? r.getCnt() : 0L));

            revenue.add(r != null ? r.getRevenue() : BigDecimal.ZERO);
            counts.add(r != null ? r.getCnt() : 0L);
        }

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartRevenue", revenue);
        model.addAttribute("chartCounts", counts);

        return "AdminPage";
    }

    @GetMapping(value = "/admin/reports/orders.csv")
    public void exportOrdersMonthlyCsv(HttpServletResponse resp) throws Exception {
        var startYm = YearMonth.now().minusMonths(11);
        var fromTs = Timestamp.valueOf(startYm.atDay(1).atStartOfDay());
        List<OrderRepository.OrderMonthlyAgg> rows = orderRepository.statsRevenueByMonth(fromTs);

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=orders_monthly.csv");

        try (PrintWriter w = resp.getWriter()) {
            w.println("month,revenue,count");
            for (var r : rows) {
                w.printf("%s,%s,%d%n", r.getYm(), r.getRevenue(), r.getCnt());
            }
        }
    }

    @PostMapping("/admin/users/add")
    public String addUser(@Valid @ModelAttribute("createUser") CreateUserDto form,
                          BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.createUser", br);
            ra.addFlashAttribute("createUser", form);
            ra.addFlashAttribute("openAddModal", true);
            return "redirect:/AdminPage";
        }
        try {
            userService.createUser(form.getLogin(), form.getRawPassword(), form.getRoleName());
            ra.addFlashAttribute("success", "Пользователь \"" + form.getLogin() + "\" создан");
        } catch (IllegalArgumentException ex) {
            br.rejectValue("login", "unique", ex.getMessage());
            ra.addFlashAttribute("org.springframework.validation.BindingResult.createUser", br);
            ra.addFlashAttribute("createUser", form);
            ra.addFlashAttribute("openAddModal", true);
        } catch (DataIntegrityViolationException ex) {
            br.rejectValue("login", "unique", "Такой логин уже существует");
            ra.addFlashAttribute("org.springframework.validation.BindingResult.createUser", br);
            ra.addFlashAttribute("createUser", form);
            ra.addFlashAttribute("openAddModal", true);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("createUser", form);
            ra.addFlashAttribute("openAddModal", true);
        }
        return "redirect:/AdminPage";
    }
    @PostMapping("/admin/users/update")
    public String updateUser(@Valid @ModelAttribute("editUser") UpdateUserDto form,
                             BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.editUser", br);
            ra.addFlashAttribute("editUser", form);
            ra.addFlashAttribute("openEditModal", true);
            return "redirect:/AdminPage";
        }
        try {
            userService.updateUser(form.getUserId(), form.getLogin(), form.getRawPassword(), form.getRoleName());
            ra.addFlashAttribute("success", "Данные пользователя обновлены");
        } catch (IllegalArgumentException ex) {
            br.rejectValue("login", "unique", ex.getMessage());
            ra.addFlashAttribute("org.springframework.validation.BindingResult.editUser", br);
            ra.addFlashAttribute("editUser", form);
            ra.addFlashAttribute("openEditModal", true);
        } catch (DataIntegrityViolationException ex) {
            br.rejectValue("login", "unique", "Такой логин уже существует");
            ra.addFlashAttribute("org.springframework.validation.BindingResult.editUser", br);
            ra.addFlashAttribute("editUser", form);
            ra.addFlashAttribute("openEditModal", true);
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("editUser", form);
            ra.addFlashAttribute("openEditModal", true);
        }
        return "redirect:/AdminPage";
    }


    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("success", "Пользователь удалён");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Не удалось удалить пользователя: " + ex.getMessage());
        }
        return "redirect:/AdminPage";
    }
}
