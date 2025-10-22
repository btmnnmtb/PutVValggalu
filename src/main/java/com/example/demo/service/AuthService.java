package com.example.demo.service;

import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public User registerUser(String username, String rawPassword , String rrole){
        Roles roles = rolesRepository.findByRoleName(rrole)
                .orElseThrow(() -> new RuntimeException("Роль не найдена: " + rrole));
        User user = new User();
        user.setLogin(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(roles);
        return usersRepository.save(user);
    }

}
