package com.example.demo.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id")
    private Actions action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "logs_user_id_fkey"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", foreignKey = @ForeignKey(name = "logs_target_user_id_fkey"))
    private User targetUser;

    @Column(name = "log_date", nullable = false)
    private Instant logDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb", nullable = false)
    private JsonNode details;


    public String getFormattedLogDate() {
        if (logDate == null) return "";
        var tz = ZoneId.of("Europe/Moscow");
        var local = logDate.atZone(tz);
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(local);
    }

    @PrePersist
    public void prePersist() {
        if (logDate == null) logDate = Instant.now();
        if (details == null) details = JsonNodeFactory.instance.objectNode();
    }
}