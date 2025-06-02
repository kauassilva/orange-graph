package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.Account_DTO;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Service.Account_Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("accounts")
public class Account_Controller {
    private final Account_Service account_service;

    public Account_Controller(Account_Service account_service) {
        this.account_service = account_service;
    }

    @GetMapping
    public ResponseEntity<List<Account>> findAllAccount(){
        List<Account> allAccounts = account_service.findAllAccount();

        return ResponseEntity.ok(allAccounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Account>> findAccountById(@PathVariable Long id){
        Optional<Account> account = account_service.findAccountById(id);

        if(account.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account_DTO account){
        Account newAccount = account_service.createAccount(account);

        return ResponseEntity.ok(newAccount);
    }

    @PutMapping("{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account_DTO account){
        Account updAccount = account_service.updateAccount(id, account);

        return ResponseEntity.ok(updAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccountById(@PathVariable Long id) {
        account_service.deleteAccountById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
