package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Carts {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cart_id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonManagedReference
    private User user;
    @Builder.Default
    @OneToMany(mappedBy = "carts", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart_items> cartItems = new ArrayList<>();

    @Column(name = "total")
    @Builder.Default
    private Double total = 0.0;

}
