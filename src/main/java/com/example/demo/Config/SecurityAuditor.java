package com.example.demo.Config;

import com.example.demo.model.User;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityAuditor {

    private final UsersRepository usersRepository;

    public User getCurrentUserOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        var username = auth.getName();
        return usersRepository.findByLogin(username).orElse(null);
    }
}