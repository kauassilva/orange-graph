package com.learning.orange_graph.Service;

import com.learning.orange_graph.Dto.Account_DTO;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Repository.Account_Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Account_Service {
    private final Account_Repository account_repository;

    public Account_Service(Account_Repository account_repository) {
        this.account_repository = account_repository;
    }

    public List<Account> findAllAccount() {
        return account_repository.findAll();
    }

    public Optional<Account> findAccountById(Long id){
        Optional<Account> account = account_repository.findById(id);

        if(account.isEmpty()) throw new RuntimeException();

        return account;
    }

    public Account createAccount(Account_DTO account) {
        return account_repository.save(account.toAccount());
    }

    public Account updateAccount(Long id, Account_DTO account) {
        Optional<Account> findAccount = account_repository.findById(id);

        Account updAccount = findAccount.get();

        updAccount.setName(account.name());
        updAccount.setCpf(account.cpf());
        updAccount.setEmail(account.email());
        updAccount.setBalance(account.balance());
        updAccount.setSuspect(account.suspect());

        return account_repository.save(updAccount);
    }

    public void deleteAccountById(Long id) {
        account_repository.deleteById(id);
    }

}
