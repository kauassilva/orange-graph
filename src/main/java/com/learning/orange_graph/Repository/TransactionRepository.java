package com.learning.orange_graph.Repository;

import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySender(Account sender);

 }
