package com.learning.orange_graph.Service;

import com.learning.orange_graph.Dto.transaction.TransactionRequest;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Exceptions.AccountNotFoundException;
import com.learning.orange_graph.Exceptions.InsufficientBalanceException;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final Account_Repository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, Account_Repository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Transaction performTransaction(TransactionRequest request) {

        Account sender = findAccountById(request.senderId());
        Account receiver = findAccountById(request.receiverId());

        validateTransactionValue(request.value());
        validateSufficientBalance(sender, request.value());

        sender.setBalance(sender.getBalance() - request.value());
        receiver.setBalance(receiver.getBalance() + request.value());

        accountRepository.save(sender);
        accountRepository.save(receiver);

        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.value());
        transaction.setDateTimeTransaction(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }
    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Conta com ID " + accountId + " não encontrada"));
    }

    private void validateSufficientBalance(Account sender, Double value) {
        if (sender.getBalance() < value) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }
    }

    private void validateTransactionValue(Double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }
    }
}
