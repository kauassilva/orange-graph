package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.transaction.TransactionRequest;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Exceptions.AccountNotFoundException;
import com.learning.orange_graph.Exceptions.InsufficientBalanceException;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Repository.TransactionRepository;
import com.learning.orange_graph.Service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionRepository transactionRepository;
    private Account_Repository accountRepository;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        accountRepository = mock(Account_Repository.class);
        service = new TransactionService(transactionRepository, accountRepository);
    }

    @Test
    void testPerformTransaction_Success() {
        Long senderId = 1L;
        Long receiverId = 2L;
        double value = 100.0;

        Account sender = mock(Account.class);
        Account receiver = mock(Account.class);

        when(sender.getBalance()).thenReturn(200.0);
        when(receiver.getBalance()).thenReturn(50.0);

        when(accountRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        TransactionRequest request = mock(TransactionRequest.class);
        when(request.senderId()).thenReturn(senderId);
        when(request.receiverId()).thenReturn(receiverId);
        when(request.value()).thenReturn(value);

        Transaction savedTransaction = new Transaction();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        Transaction result = service.performTransaction(request);

        assertEquals(savedTransaction, result);
        verify(sender).setBalance(100.0); // 200 - 100
        verify(receiver).setBalance(150.0); // 50 + 100
        verify(accountRepository).save(sender);
        verify(accountRepository).save(receiver);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testFindAccountById_ReturnsAccount() throws Exception {
        Long accountId = 1L;
        Account account = mock(Account.class);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        var method = TransactionService.class.getDeclaredMethod("findAccountById", Long.class);
        method.setAccessible(true);

        Account result = (Account) method.invoke(service, accountId);
        assertEquals(account, result);
    }

    @Test
    void testFindAccountById_ThrowsException() throws Exception {
        Long accountId = 2L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        var method = TransactionService.class.getDeclaredMethod("findAccountById", Long.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, accountId);
        });

        assertTrue(exception.getCause() instanceof AccountNotFoundException);
        assertTrue(exception.getCause().getMessage().contains("Conta com ID " + accountId + " não encontrada"));
    }

     @Test
    void testValidateSufficientBalance_Sufficient() throws Exception {
        Account sender = mock(Account.class);
        when(sender.getBalance()).thenReturn(200.0);

        var method = TransactionService.class.getDeclaredMethod("validateSufficientBalance", Account.class, Double.class);
        method.setAccessible(true);
        method.invoke(service, sender, 100.0);
    }

    @Test
    void testValidateSufficientBalance_Insufficient() throws Exception {
        Account sender = mock(Account.class);
        when(sender.getBalance()).thenReturn(50.0);

        var method = TransactionService.class.getDeclaredMethod("validateSufficientBalance", Account.class, Double.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, sender, 100.0);
        });

        assertTrue(exception.getCause() instanceof InsufficientBalanceException);
        assertEquals("Saldo insuficiente", exception.getCause().getMessage());
    }

     @Test
    void testValidateTransactionValue_PositiveValue() throws Exception {
        var method = TransactionService.class.getDeclaredMethod("validateTransactionValue", Double.class);
        method.setAccessible(true);
        method.invoke(service, 10.0);
    }

    @Test
    void testValidateTransactionValue_Zero() throws Exception {
        var method = TransactionService.class.getDeclaredMethod("validateTransactionValue", Double.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, 0.0);
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Valor da transação deve ser positivo", exception.getCause().getMessage());
    }

    @Test
    void testValidateTransactionValue_Negative() throws Exception {
        var method = TransactionService.class.getDeclaredMethod("validateTransactionValue", Double.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, -5.0);
        });

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Valor da transação deve ser positivo", exception.getCause().getMessage());
    }

}