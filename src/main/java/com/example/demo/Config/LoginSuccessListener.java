package com.example.demo.Config;

import com.example.demo.model.Logs;
import com.example.demo.repository.ActionsRepository;
import com.example.demo.repository.LogsRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginSuccessListener {

    private final UsersRepository usersRepository;
    private final ActionsRepository actionsRepository;
    private final LogsRepository logsRepository;

    @EventListener
    public void onSuccess(org.springframework.security.authentication.event.AuthenticationSuccessEvent e) {
        String username = e.getAuthentication().getName();

        var userOpt = usersRepository.findByLogin(username);
        if (userOpt.isEmpty()) return;

        var action = actionsRepository.findByActionName("Войти")
                .orElseThrow(() -> new IllegalStateException("Action LOGIN not seeded"));

        logsRepository.save(Logs.builder()
                .action(action)
                .user(userOpt.get())
                .build());
    }
}
