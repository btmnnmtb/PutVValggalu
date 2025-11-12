package com.example.demo.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")

@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true, nullable = false, length = 20)
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 3, max = 20, message = "Логин должен быть от 3 до 20 символов")
    private String login;

    @Column(name = "password_hash", length = 255, nullable = false)
    @JsonIgnore
    private String passwordHash;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnoreProperties({"users","hibernateLazyInitializer","handler"})
    private Roles role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Favourites> favourites = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference("user-comments")
    private List<Comments> comments = new ArrayList<>();

    @Transient
    @Size(min = 6, max = 20, message = "Пароль должен быть от 6 до 20 символов")
    private String rawPassword;

}
