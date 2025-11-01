package com.example.demo.service;

import com.example.demo.Config.UserDetailsConfig;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    public User createUser(String username, String rawPassword, String roleName) {
        usersRepository.findByLogin(username).ifPresent(u -> {
            throw new IllegalArgumentException("Пользователь с логином \"" + username + "\" уже существует");
        });
        var role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль \"" + roleName + "\" не найдена"));
        var user = User.builder()
                .login(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        return usersRepository.save(user);
    }
    @Transactional
    public User updateUser(Integer id, String newLogin, String newPasswordRaw, String roleName) {
        var user = usersRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (newLogin == null || newLogin.isBlank()) {
            throw new IllegalArgumentException("Login не может быть пустым");
        }
        user.setLogin(newLogin);

        if (newPasswordRaw != null && !newPasswordRaw.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPasswordRaw));
        }

        var role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль \"" + roleName + "\" не найдена"));
        user.setRole(role);

        return usersRepository.save(user);
    }

}
