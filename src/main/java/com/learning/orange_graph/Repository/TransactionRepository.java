package com.learning.orange_graph.Repository;

import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySender(Account sender);

    // Retorna todas as transacoes onde a conta é o remetente dentro de um determinado periodo
    List<Transaction> findBySenderIdAndDateTimeTransactionBetween(Long senderId, LocalDateTime start, LocalDateTime end);

    // Retorna todas as transacoes onde a conta é o destinatario dentro de um determinado periodo
    List<Transaction> findByReceiverIdAndDateTimeTransactionBetween(Long receiverId, LocalDateTime start, LocalDateTime end);

 }
