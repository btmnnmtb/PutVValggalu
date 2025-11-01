package com.example.demo.controller;


import com.example.demo.model.CreateUserDto;
import com.example.demo.model.UpdateUserDto;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminPageContoller {

    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserService userService;

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

        if (!model.containsAttribute("createUser")) model.addAttribute("createUser", new CreateUserDto());
        if (!model.containsAttribute("editUser"))   model.addAttribute("editUser", new UpdateUserDto());

        return "AdminPage";
    }

    @PostMapping("/admin/users/add")
    public String addUser(@Valid @ModelAttribute("createUser") CreateUserDto form,
                          BindingResult br,
                          RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.createUser", br);
            ra.addFlashAttribute("createUser", form);
            ra.addFlashAttribute("openAddModal", true);
            return "redirect:/AdminPage";
        }
        try {
            userService.createUser(form.getLogin(), form.getRawPassword(), form.getRoleName());
            ra.addFlashAttribute("success", "Пользователь \"" + form.getLogin() + "\" создан");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("createUser", form);
            ra.addFlashAttribute("openAddModal", true);
        }
        return "redirect:/AdminPage";
    }

    @PostMapping("/admin/users/update")
    public String updateUser(@Valid @ModelAttribute("editUser") UpdateUserDto form,
                             BindingResult br,
                             RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.editUser", br);
            ra.addFlashAttribute("editUser", form);
            ra.addFlashAttribute("openEditModal", true);
            return "redirect:/AdminPage";
        }
        try {
            userService.updateUser(form.getUserId(), form.getLogin(), form.getRawPassword(), form.getRoleName());
            ra.addFlashAttribute("success", "Данные пользователя обновлены");
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
            usersRepository.deleteById(id);
            ra.addFlashAttribute("success", "Пользователь удалён");
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Не удалось удалить пользователя: " + ex.getMessage());
        }
        return "redirect:/AdminPage";
    }
}
