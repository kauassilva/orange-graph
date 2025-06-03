package com.learning.orange_graph.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "ACCOUNT_TABLE")

public class Account {
    public Account(String cpf, String name, String email, Double balance, Boolean suspect) {
        this.cpf = cpf;
        this.name = name;
        this.email = email;
        this.balance = balance;
        this.suspect = suspect;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "email")
    private String email;

    @Column(name = "balance")
    private Double balance;

    @Column(name = "suspect")
    private Boolean suspect;
    
}
