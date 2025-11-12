package com.example.demo.service;

import com.example.demo.Config.SecurityAuditor;
import com.example.demo.Config.UserDetailsConfig;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    @Autowired
    private UsersRepository usersRepository;
    private final AuditLogService auditLog;
    private final SecurityAuditor securityAuditor;
    private final ObjectMapper om;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RolesRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
        return new UserDetailsConfig(user);
    }
    @Transactional
    public boolean updatePasswordByLogin(String login, String newPasswordRaw) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (newPasswordRaw == null || newPasswordRaw.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (newPasswordRaw.length() < 6 || newPasswordRaw.length() > 20) {
            throw new IllegalArgumentException("Пароль должен быть от 6 до 20 символов");
        }

        var userOpt = usersRepository.findByLogin(login.trim());
        if (userOpt.isEmpty()) {
            return false;
        }

        var user = userOpt.get();
        var before = om.createObjectNode().put("login", user.getLogin());
        user.setPasswordHash(passwordEncoder.encode(newPasswordRaw));
        usersRepository.saveAndFlush(user);

        var after = om.createObjectNode().put("login", user.getLogin());
        var actor = securityAuditor.getCurrentUserOrNull();
        auditLog.log("Изменил пользователя", actor, user, auditLog.diff(before, after));

        return true;
    }

    @Transactional
    public void createUser(String login, String rawPassword, String roleName) {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("Логин не может быть пустым");
        if (rawPassword == null || rawPassword.isBlank())
            throw new IllegalArgumentException("Пароль не может быть пустым");

        String normLogin = login.trim();
        if (usersRepository.existsByLoginIgnoreCase(normLogin)) {
            throw new IllegalArgumentException("Такой логин уже существует");
        }

        var role = roleRepository.findByRoleName(roleName.trim())
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));

        var user = new User();
        user.setLogin(normLogin);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        usersRepository.saveAndFlush(user);

        var actor = securityAuditor.getCurrentUserOrNull();
        var details = om.createObjectNode()
                .put("login", user.getLogin())
                .put("role", user.getRole().getRoleName());

        auditLog.log("Создал пользователя", actor, user, details);
    }


    @Transactional
    public User updateUser(Integer id, String newLogin, String newPasswordRaw, String roleName) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var before = om.createObjectNode()
                .put("login", user.getLogin())
                .put("role", user.getRole().getRoleName());

        if (newLogin == null || newLogin.isBlank()) {
            throw new IllegalArgumentException("Login не может быть пустым");
        }
        user.setLogin(newLogin.trim());

        if (newPasswordRaw != null && !newPasswordRaw.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPasswordRaw));
        }

        var role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль \"" + roleName + "\" не найдена"));
        user.setRole(role);

        usersRepository.save(user);

        var after = om.createObjectNode()
                .put("login", user.getLogin())
                .put("role", user.getRole().getRoleName());

        var actor = securityAuditor.getCurrentUserOrNull();
        auditLog.log(
                "Изменил пользователя",
                actor,
                user,
                auditLog.diff(before, after)
        );

        return user;
    }

    @Transactional
    public void deleteUser(Integer id) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        var details = om.createObjectNode()
                .put("id", user.getUserId())
                .put("login", user.getLogin())
                .put("role", user.getRole().getRoleName());

        var actor = securityAuditor.getCurrentUserOrNull();

        usersRepository.delete(user);
        usersRepository.flush();

        auditLog.log("Удалил пользователя", actor, null, details);
    }
}
