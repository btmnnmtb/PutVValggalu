package com.example.demo.service;

import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    final private AuditLogService auditLogService;

    @Transactional
    public User registerUser(String username, String rawPassword , String roleName){
        var role = rolesRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + roleName));

        var user = new User();
        user.setLogin(username.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        usersRepository.saveAndFlush(user);

        var details = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                .put("login", user.getLogin())
                .put("role", role.getRoleName());

        auditLogService.log("Зарегистрироваля", user, user, details);

        return user;
    }

}
