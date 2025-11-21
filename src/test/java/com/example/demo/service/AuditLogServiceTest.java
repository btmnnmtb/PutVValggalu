package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.ActionsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {
    @Mock
    ActionsRepository actionsRepository;
    @InjectMocks
    AuditLogService auditLogService;

    @Test
    void logActionNotFound() {
        User actor = new User();
        actor.setUserId(1);

        User target = new User();
        target.setUserId(2);

        JsonNode details = JsonNodeFactory.instance.objectNode();

        String actionName = "actionName";
        when(actionsRepository.findByActionName(actionName)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> auditLogService.log(actionName, actor, target, details)
        );
        assertEquals("Action '" + actionName + "' not seeded", exception.getMessage());


    }
}