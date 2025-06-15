package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.transaction.TransactionRequest;
import com.learning.orange_graph.Dto.transaction.TransactionResponse;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

    private TransactionService transactionService;
    private TransactionController controller;

    @BeforeEach
    void setUp() {
        transactionService = mock(TransactionService.class);
        controller = new TransactionController(transactionService);
    }

    @Test
    void testPerformTransaction_ReturnsResponseOk() {
        TransactionRequest request = mock(TransactionRequest.class);

        Account sender = mock(Account.class);
        Account receiver = mock(Account.class);
        when(sender.getName()).thenReturn("Alice");
        when(receiver.getName()).thenReturn("Bob");

        Transaction transaction = mock(Transaction.class);
        when(transaction.getId()).thenReturn(1L);
        when(transaction.getSender()).thenReturn(sender);
        when(transaction.getReceiver()).thenReturn(receiver);
        when(transaction.getValue()).thenReturn(100.0);
        LocalDateTime now = LocalDateTime.now();
        when(transaction.getDateTimeTransaction()).thenReturn(now);

        when(transactionService.performTransaction(request)).thenReturn(transaction);

        ResponseEntity<TransactionResponse> responseEntity = controller.performTransaction(request);

        assertEquals(200, responseEntity.getStatusCodeValue());
        TransactionResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(1L, response.getId());
        assertEquals("Alice", response.getSenderName());
        assertEquals("Bob", response.getReceiverName());
        assertEquals(100.0, response.getValue());
        assertNotNull(response.getTransactionDate());
        assertTrue(Math.abs(java.time.Duration.between(now, response.getTransactionDate()).toMillis()) < 1000);

        verify(transactionService).performTransaction(request);
    }
}