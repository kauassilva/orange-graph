package com.learning.orange_graph.Entity;

import java.time.LocalDate;

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
    public Account(String name, String cpf, String email, Double balance, Boolean suspect, LocalDate creationDate) {
        this.name = name;
        this.cpf = cpf;
        this.email = email;
        this.balance = balance;
        this.suspect = suspect;
        this.creationDate = creationDate;
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

    @Column(name = "creationDate")
    private LocalDate creationDate;
    
}
