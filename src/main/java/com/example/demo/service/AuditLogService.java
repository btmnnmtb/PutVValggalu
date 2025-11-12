package com.example.demo.service;

import com.example.demo.model.Logs;
import com.example.demo.model.User;
import com.example.demo.repository.ActionsRepository;
import com.example.demo.repository.LogsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final ActionsRepository actionsRepository;
    private final LogsRepository logsRepository;

    @Transactional
    public void log(String actionName, User actor, User target, JsonNode details) {
        var action = actionsRepository.findByActionName(actionName)
                .orElseThrow(() -> new IllegalStateException("Action '" + actionName + "' not seeded"));

        var log = Logs.builder()
                .action(action)
                .user(actor)
                .targetUser(target)
                .details(details != null ? details : com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode())
                .build();

        logsRepository.save(log);
    }

    public ObjectNode diff(ObjectNode before, ObjectNode after) {
        var n = after.objectNode();
        n.set("before", before);
        n.set("after",  after);
        return n;
    }
    public void logApprove(User actor, User target, ObjectNode before, ObjectNode after) {
        log("Одобрил заявку на товар", actor, target, diff(before, after));
    }
    public void logReject(User actor, User target, ObjectNode before, ObjectNode after, String ip) {
        log("Отклонил заявку на товар", actor, target, diff(before, after));
    }
    public void logSupply(User actor, User target, ObjectNode before, ObjectNode after, int addedQty, String ip) {
        var d = diff(before, after);
        d.put("addedQty", addedQty);
        log("Пополнил остаток товара", actor, target, d);
    }
}
