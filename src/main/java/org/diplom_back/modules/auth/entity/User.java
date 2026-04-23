package org.diplom_back.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user") // Кавычки важны, так как user — зарезервированное слово
@Data
public class User {
    @Id
    @Column(name = "user_id", length = 36)
    private String userId = UUID.randomUUID().toString();

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ADMIN', 'MANAGER', 'CLIENT')")
    private Role role;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Role {
        ADMIN, MANAGER, CLIENT
    }

    // Связь с личными данными (клиентом)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude // Чтобы не было бесконечной рекурсии в логах
    private Client client;

    // Связь с детьми
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Child> children = new ArrayList<>();
}