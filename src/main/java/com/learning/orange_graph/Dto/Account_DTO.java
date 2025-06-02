package com.learning.orange_graph.Dto;

import com.learning.orange_graph.Entity.Account;

public record Account_DTO (String name, String cpf, String email, Double balance, Boolean suspect) {

    public Account toAccount() {
        return new Account(name, cpf, email, balance, suspect);
    }

}
