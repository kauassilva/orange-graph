package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Repository.TransactionRepository;
import com.learning.orange_graph.Service.FraudDetectionService;
import com.learning.orange_graph.tad.BankGraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {

    private Account_Repository accountRepository;
    private TransactionRepository transactionRepository;
    private FraudDetectionService service;

    @BeforeEach
    void setUp() {
        accountRepository = mock(Account_Repository.class);
        transactionRepository = mock(TransactionRepository.class);
        service = new FraudDetectionService(accountRepository, transactionRepository);
    }

    @Test
    void testCheckSuspicion_SuspectDetected() {
        Long accountId = 1L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn("Conta Teste");
        when(account.getCreationDate()).thenReturn(LocalDate.now());
        when(account.getBalance()).thenReturn(10.0);
        when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));

        Transaction incoming = mock(Transaction.class);
        Transaction outgoing = mock(Transaction.class);

        Account receiver = mock(Account.class);
        when(receiver.getId()).thenReturn(2L);

        when(incoming.getSender()).thenReturn(account);
        when(incoming.getReceiver()).thenReturn(receiver);
        when(incoming.getDateTimeTransaction()).thenReturn(LocalDateTime.now());
        when(incoming.getValue()).thenReturn(2000.0);
        when(outgoing.getSender()).thenReturn(account);
        when(outgoing.getReceiver()).thenReturn(receiver);
        when(outgoing.getDateTimeTransaction()).thenReturn(LocalDateTime.now().plusMinutes(10));
        when(outgoing.getValue()).thenReturn(1500.0);

        List<Transaction> incomingTransactions = Collections.singletonList(incoming);
        List<Transaction> outgoingTransactions = Collections.singletonList(outgoing);

        when(transactionRepository.findAll()).thenReturn(List.of(incoming, outgoing));
        when(transactionRepository.findByReceiverIdAndDateTimeTransactionBetween(anyLong(), any(), any()))
                .thenReturn(incomingTransactions);
        when(transactionRepository.findBySenderIdAndDateTimeTransactionBetween(anyLong(), any(), any()))
                .thenReturn(outgoingTransactions);

        SuspicionCheckResponseDto response = service.checkSuspicion(
                accountId,
                24,
                1,
                1,
                1000.0,
                1000.0,
                0.5,
                1
        );

        assertTrue(response.isSuspect());
        assertTrue(response.message().contains("ALERTA"));
    }

    @Test
    void testCheckSuspicion_NoSuspect() {
        Long accountId = 2L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn("Conta Normal");
        when(account.getCreationDate()).thenReturn(LocalDate.now().minusDays(10));
        when(account.getBalance()).thenReturn(5000.0);

        when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));

        Transaction incoming = mock(Transaction.class);
        Transaction outgoing = mock(Transaction.class);

        Account receiver = mock(Account.class);
        when(receiver.getId()).thenReturn(3L);

        when(incoming.getSender()).thenReturn(account);
        when(incoming.getReceiver()).thenReturn(receiver);
        when(incoming.getDateTimeTransaction()).thenReturn(LocalDateTime.now().minusDays(5));
        when(incoming.getValue()).thenReturn(100.0);

        when(outgoing.getSender()).thenReturn(account);
        when(outgoing.getReceiver()).thenReturn(receiver);
        when(outgoing.getDateTimeTransaction()).thenReturn(LocalDateTime.now().minusDays(4));
        when(outgoing.getValue()).thenReturn(50.0);

        List<Transaction> incomingTransactions = Collections.singletonList(incoming);
        List<Transaction> outgoingTransactions = Collections.singletonList(outgoing);

        when(transactionRepository.findAll()).thenReturn(List.of(incoming, outgoing));
        when(transactionRepository.findByReceiverIdAndDateTimeTransactionBetween(anyLong(), any(), any()))
                .thenReturn(incomingTransactions);
        when(transactionRepository.findBySenderIdAndDateTimeTransactionBetween(anyLong(), any(), any()))
                .thenReturn(outgoingTransactions);

        SuspicionCheckResponseDto response = service.checkSuspicion(
                accountId,
                24,
                5,
                5,
                1000.0,
                1000.0,
                0.5,
                3
        );

        assertFalse(response.isSuspect());
        assertTrue(response.message().contains("Nenhum padrão forte"));
    }

     @Test
    void testGetSuspicionCheckResponseDto_Suspect() throws Exception {
        Long accountId = 1L;
        Account account = mock(Account.class);

        var method = FraudDetectionService.class.getDeclaredMethod(
                "getSuspicionCheckResponseDto", Long.class, boolean.class, Account.class);
        method.setAccessible(true);

        SuspicionCheckResponseDto dto = (SuspicionCheckResponseDto) method.invoke(null, accountId, true, account);

        assertTrue(dto.isSuspect());
        assertTrue(dto.message().contains("ALERTA"));
    }

    @Test
    void testGetSuspicionCheckResponseDto_NotSuspect() throws Exception {
        Long accountId = 2L;
        Account account = mock(Account.class);
        when(account.getName()).thenReturn("Conta Teste");

        var method = FraudDetectionService.class.getDeclaredMethod(
                "getSuspicionCheckResponseDto", Long.class, boolean.class, Account.class);
        method.setAccessible(true);

        SuspicionCheckResponseDto dto = (SuspicionCheckResponseDto) method.invoke(null, accountId, false, account);

        assertFalse(dto.isSuspect());
        assertTrue(dto.message().contains("Nenhum padrão forte"));
        assertTrue(dto.message().contains("Conta Teste"));
        assertTrue(dto.message().contains(accountId.toString()));
    }

    @Test
void testIsSuspect_Indirect_AllFirstConditionTrue() {
    Long accountId = 10L;
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);
    when(account.getName()).thenReturn("Conta Teste");
    when(account.getCreationDate()).thenReturn(LocalDate.now());
    when(account.getBalance()).thenReturn(1.0);

    when(accountRepository.findAll()).thenReturn(Collections.singletonList(account));

    Transaction incoming = mock(Transaction.class);
    Transaction outgoing = mock(Transaction.class);
    Account receiver = mock(Account.class);
    when(receiver.getId()).thenReturn(20L);

    when(incoming.getSender()).thenReturn(account);
    when(incoming.getReceiver()).thenReturn(receiver);
    when(incoming.getDateTimeTransaction()).thenReturn(LocalDateTime.now());
    when(incoming.getValue()).thenReturn(2000.0);

    when(outgoing.getSender()).thenReturn(account);
    when(outgoing.getReceiver()).thenReturn(receiver);
    when(outgoing.getDateTimeTransaction()).thenReturn(LocalDateTime.now().plusMinutes(10));
    when(outgoing.getValue()).thenReturn(1500.0);

    List<Transaction> incomingTransactions = Collections.singletonList(incoming);
    List<Transaction> outgoingTransactions = Collections.singletonList(outgoing);

    when(transactionRepository.findAll()).thenReturn(List.of(incoming, outgoing));
    when(transactionRepository.findByReceiverIdAndDateTimeTransactionBetween(anyLong(), any(), any()))
            .thenReturn(incomingTransactions);
    when(transactionRepository.findBySenderIdAndDateTimeTransactionBetween(anyLong(), any(), any()))
            .thenReturn(outgoingTransactions);

    SuspicionCheckResponseDto response = service.checkSuspicion(
            accountId,
            24,
            1,
            1,
            1000.0,
            1000.0,
            0.5,
            1
    );

    assertTrue(response.isSuspect());
}

    @Test
    void testHasManyUniqueOutgoingReceivers_True() throws Exception {
        BankGraph graph = mock(BankGraph.class);
        Long accountId = 1L;

        when(graph.getNeighbors(accountId)).thenReturn(Arrays.asList(2L, 3L, 4L));

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasManyUniqueOutgoingReceivers", int.class, BankGraph.class, Long.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, 3, graph, accountId);
        assertTrue(result);
    }

    @Test
    void testHasManyUniqueOutgoingReceivers_False() throws Exception {
        BankGraph graph = mock(BankGraph.class);
        Long accountId = 1L;

        when(graph.getNeighbors(accountId)).thenReturn(Collections.singletonList(2L));

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasManyUniqueOutgoingReceivers", int.class, BankGraph.class, Long.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, 2, graph, accountId);
        assertFalse(result);
    }

    @Test
    void testHasManyUniqueIncomingSenders_True() throws Exception {
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        Transaction t3 = mock(Transaction.class);

        Account a1 = mock(Account.class);
        Account a2 = mock(Account.class);
        Account a3 = mock(Account.class);

        when(a1.getId()).thenReturn(1L);
        when(a2.getId()).thenReturn(2L);
        when(a3.getId()).thenReturn(3L);

        when(t1.getSender()).thenReturn(a1);
        when(t2.getSender()).thenReturn(a2);
        when(t3.getSender()).thenReturn(a3);

        List<Transaction> incomingTransactions = Arrays.asList(t1, t2, t3);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasManyUniqueIncomingSenders", int.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, 3, incomingTransactions);
        assertTrue(result);
    }

    @Test
    void testHasManyUniqueIncomingSenders_False() throws Exception {
        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);

        Account a1 = mock(Account.class);

        when(a1.getId()).thenReturn(1L);

        when(t1.getSender()).thenReturn(a1);
        when(t2.getSender()).thenReturn(a1);

        List<Transaction> incomingTransactions = Arrays.asList(t1, t2);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasManyUniqueIncomingSenders", int.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, 2, incomingTransactions);
        assertFalse(result);
    }

     @Test
    void testHasLowRetentionRatio_True() throws Exception {
        Account account = mock(Account.class);
        when(account.getBalance()).thenReturn(10.0);

        double totalOutgoingValue = 1000.0;
        double balanceRetentionThreshold = 0.05; // 5%

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasLowRetentionRatio", double.class, double.class, Account.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, balanceRetentionThreshold, totalOutgoingValue, account);
        assertTrue(result);
    }

    @Test
    void testHasLowRetentionRatio_False_HighRetention() throws Exception {
        Account account = mock(Account.class);
        when(account.getBalance()).thenReturn(100.0);

        double totalOutgoingValue = 100.0;
        double balanceRetentionThreshold = 0.5; // 50%

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasLowRetentionRatio", double.class, double.class, Account.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, balanceRetentionThreshold, totalOutgoingValue, account);
        assertFalse(result);
    }

    @Test
    void testHasLowRetentionRatio_False_ZeroOutgoing() throws Exception {
        Account account = mock(Account.class);
        when(account.getBalance()).thenReturn(10.0);

        double totalOutgoingValue = 0.0;
        double balanceRetentionThreshold = 0.5;

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasLowRetentionRatio", double.class, double.class, Account.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, balanceRetentionThreshold, totalOutgoingValue, account);
        assertFalse(result);
    }

    @Test
    void testHasLowRetentionRatio_False_NullBalance() throws Exception {
        Account account = mock(Account.class);
        when(account.getBalance()).thenReturn(null);

        double totalOutgoingValue = 100.0;
        double balanceRetentionThreshold = 0.5;

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasLowRetentionRatio", double.class, double.class, Account.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, balanceRetentionThreshold, totalOutgoingValue, account);
        assertFalse(result);
    }

     @Test
    void testHasImmediateFlow_True() throws Exception {
        Transaction incoming = mock(Transaction.class);
        LocalDateTime incomingTime = LocalDateTime.now();
        when(incoming.getDateTimeTransaction()).thenReturn(incomingTime);

        Transaction outgoing = mock(Transaction.class);
        when(outgoing.getDateTimeTransaction()).thenReturn(incomingTime.plusMinutes(10));

        List<Transaction> incomingTransactions = Collections.singletonList(incoming);
        List<Transaction> outgoingTransactions = Collections.singletonList(outgoing);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasImmediateFlow", boolean.class, boolean.class, List.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, true, true, incomingTransactions, outgoingTransactions);
        assertTrue(result);
    }

    @Test
    void testHasImmediateFlow_False_NoOutgoingWithin30Minutes() throws Exception {
        Transaction incoming = mock(Transaction.class);
        LocalDateTime incomingTime = LocalDateTime.now();
        when(incoming.getDateTimeTransaction()).thenReturn(incomingTime);

        Transaction outgoing = mock(Transaction.class);
        when(outgoing.getDateTimeTransaction()).thenReturn(incomingTime.plusMinutes(40));

        List<Transaction> incomingTransactions = Collections.singletonList(incoming);
        List<Transaction> outgoingTransactions = Collections.singletonList(outgoing);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasImmediateFlow", boolean.class, boolean.class, List.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, true, true, incomingTransactions, outgoingTransactions);
        assertFalse(result);
    }

    @Test
    void testHasImmediateFlow_False_HighDegreeOrVolumeFalse() throws Exception {
        Transaction incoming = mock(Transaction.class);
        LocalDateTime incomingTime = LocalDateTime.now();
        when(incoming.getDateTimeTransaction()).thenReturn(incomingTime);

        Transaction outgoing = mock(Transaction.class);
        when(outgoing.getDateTimeTransaction()).thenReturn(incomingTime.plusMinutes(10));

        List<Transaction> incomingTransactions = Collections.singletonList(incoming);
        List<Transaction> outgoingTransactions = Collections.singletonList(outgoing);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasImmediateFlow", boolean.class, boolean.class, List.class, List.class);
        method.setAccessible(true);

        boolean result1 = (boolean) method.invoke(null, false, true, incomingTransactions, outgoingTransactions);
        assertFalse(result1);

        boolean result2 = (boolean) method.invoke(null, true, false, incomingTransactions, outgoingTransactions);
        assertFalse(result2);

        boolean result3 = (boolean) method.invoke(null, false, false, incomingTransactions, outgoingTransactions);
        assertFalse(result3);
    }

    @Test
    void testHasHighVolume_True() throws Exception {
        double minTotalIncomingValue = 1000.0;
        double minTotalOutgoingValue = 2000.0;
        double totalIncomingValue = 1500.0;
        double totalOutgoingValue = 2500.0;

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighVolume", double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minTotalIncomingValue, minTotalOutgoingValue, totalOutgoingValue, totalIncomingValue);
        assertTrue(result);
    }

    @Test
    void testHasHighVolume_False_OutgoingBelowThreshold() throws Exception {
        double minTotalIncomingValue = 1000.0;
        double minTotalOutgoingValue = 2000.0;
        double totalIncomingValue = 1500.0;
        double totalOutgoingValue = 1500.0;

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighVolume", double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minTotalIncomingValue, minTotalOutgoingValue, totalOutgoingValue, totalIncomingValue);
        assertFalse(result);
    }

    @Test
    void testHasHighVolume_False_IncomingBelowThreshold() throws Exception {
        double minTotalIncomingValue = 1000.0;
        double minTotalOutgoingValue = 2000.0;
        double totalIncomingValue = 900.0; 
        double totalOutgoingValue = 2500.0;

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighVolume", double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minTotalIncomingValue, minTotalOutgoingValue, totalOutgoingValue, totalIncomingValue);
        assertFalse(result);
    }

    @Test
    void testHasHighVolume_False_BothBelowThreshold() throws Exception {
        double minTotalIncomingValue = 1000.0;
        double minTotalOutgoingValue = 2000.0;
        double totalIncomingValue = 900.0;
        double totalOutgoingValue = 1500.0;

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighVolume", double.class, double.class, double.class, double.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minTotalIncomingValue, minTotalOutgoingValue, totalOutgoingValue, totalIncomingValue);
        assertFalse(result);
    }

    @Test
    void testHasHighDegree_True() throws Exception {
        int minIncomingTransactions = 2;
        int minOutgoingTransactions = 2;

        List<Transaction> incomingTransactions = List.of(mock(Transaction.class), mock(Transaction.class));
        List<Transaction> outgoingTransactions = List.of(mock(Transaction.class), mock(Transaction.class), mock(Transaction.class));

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighDegree", int.class, int.class, List.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minIncomingTransactions, minOutgoingTransactions, outgoingTransactions, incomingTransactions);
        assertTrue(result);
    }

    @Test
    void testHasHighDegree_False_InsufficientIncoming() throws Exception {
        int minIncomingTransactions = 3;
        int minOutgoingTransactions = 1;

        List<Transaction> incomingTransactions = List.of(mock(Transaction.class)); // só 1
        List<Transaction> outgoingTransactions = List.of(mock(Transaction.class), mock(Transaction.class));

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighDegree", int.class, int.class, List.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minIncomingTransactions, minOutgoingTransactions, outgoingTransactions, incomingTransactions);
        assertFalse(result);
    }

    @Test
    void testHasHighDegree_False_InsufficientOutgoing() throws Exception {
        int minIncomingTransactions = 1;
        int minOutgoingTransactions = 2;

        List<Transaction> incomingTransactions = List.of(mock(Transaction.class), mock(Transaction.class));
        List<Transaction> outgoingTransactions = Collections.singletonList(mock(Transaction.class)); // só 1

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighDegree", int.class, int.class, List.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minIncomingTransactions, minOutgoingTransactions, outgoingTransactions, incomingTransactions);
        assertFalse(result);
    }

    @Test
    void testHasHighDegree_False_BothInsufficient() throws Exception {
        int minIncomingTransactions = 2;
        int minOutgoingTransactions = 2;

        List<Transaction> incomingTransactions = Collections.singletonList(mock(Transaction.class));
        List<Transaction> outgoingTransactions = Collections.singletonList(mock(Transaction.class));

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "hasHighDegree", int.class, int.class, List.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(null, minIncomingTransactions, minOutgoingTransactions, outgoingTransactions, incomingTransactions);
        assertFalse(result);
    }

     @Test
    void testGetOutgoingTransactions() throws Exception {
        FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);

        Long accountId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        List<Transaction> expectedList = List.of(t1, t2);

        when(transactionRepository.findBySenderIdAndDateTimeTransactionBetween(accountId, startTime, endTime))
                .thenReturn(expectedList);

        var method = FraudDetectionService.class.getDeclaredMethod(
                "getOutgoingTransactions", Long.class, LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Transaction> result = (List<Transaction>) method.invoke(service, accountId, startTime, endTime);

        assertEquals(expectedList, result);
        verify(transactionRepository).findBySenderIdAndDateTimeTransactionBetween(accountId, startTime, endTime);
    }

      @Test
    void testGetIncomingTransactions() throws Exception {
        FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);

        Long accountId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        Transaction t1 = mock(Transaction.class);
        Transaction t2 = mock(Transaction.class);
        List<Transaction> expectedList = List.of(t1, t2);

        when(transactionRepository.findByReceiverIdAndDateTimeTransactionBetween(accountId, startTime, endTime))
                .thenReturn(expectedList);

        var method = FraudDetectionService.class.getDeclaredMethod(
                "getIncomingTransactions", Long.class, LocalDateTime.class, LocalDateTime.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Transaction> result = (List<Transaction>) method.invoke(service, accountId, startTime, endTime);

        assertEquals(expectedList, result);
        verify(transactionRepository).findByReceiverIdAndDateTimeTransactionBetween(accountId, startTime, endTime);
    }

     @Test
    void testGetAccountOrThrow_ReturnsAccount() throws Exception {
        Long accountId = 1L;
        BankGraph graph = mock(BankGraph.class);
        Account account = mock(Account.class);

        when(graph.getAccountById(accountId)).thenReturn(account);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "getAccountOrThrow", Long.class, BankGraph.class);
        method.setAccessible(true);

        Account result = (Account) method.invoke(null, accountId, graph);
        assertEquals(account, result);
    }

    @Test
    void testGetAccountOrThrow_ThrowsException() throws Exception {
        Long accountId = 2L;
        BankGraph graph = mock(BankGraph.class);

        when(graph.getAccountById(accountId)).thenReturn(null);

        var method = com.learning.orange_graph.Service.FraudDetectionService.class.getDeclaredMethod(
                "getAccountOrThrow", Long.class, BankGraph.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(null, accountId, graph);
        });

        assertTrue(exception.getCause().getMessage().contains("Conta com ID " + accountId + " não encontrada no grafo."));
    }

    @Test
void testCreateInMemoryGraph() throws Exception {
    FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);

    Account account1 = mock(Account.class);
    Account account2 = mock(Account.class);

    Transaction transaction = mock(Transaction.class);
    when(transaction.getSender()).thenReturn(account1);
    when(transaction.getReceiver()).thenReturn(account2);

    List<Account> accounts = List.of(account1, account2);
    List<Transaction> transactions = List.of(transaction);

    when(accountRepository.findAll()).thenReturn(accounts);
    when(transactionRepository.findAll()).thenReturn(transactions);

    BankGraph bankGraphSpy = spy(new BankGraph());

    var method = FraudDetectionService.class.getDeclaredMethod("createInMemoryGraph");
    method.setAccessible(true);

    BankGraph result = (BankGraph) method.invoke(service);

    assertNotNull(result);
}

    @Test
void testDepthFirstTraversal_EmptyGraph() throws Exception {
    FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);
    BankGraph graph = mock(BankGraph.class);
    Long accountId = 1L;

    when(graph.hasNode(accountId)).thenReturn(false);

    var method = FraudDetectionService.class.getDeclaredMethod(
            "depthFirstTraversal", Long.class, BankGraph.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Set<Long> result = (Set<Long>) method.invoke(service, accountId, graph);

    assertTrue(result.isEmpty());
}

@Test
void testDepthFirstTraversal_SimpleGraph() throws Exception {
    FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);
    BankGraph graph = mock(BankGraph.class);
    Long accountId = 1L;

    // Simula um grafo: 1 -> 2, 1 -> 3, 2 -> 4
    when(graph.hasNode(accountId)).thenReturn(true);
    when(graph.getNeighbors(1L)).thenReturn(Arrays.asList(2L, 3L));
    when(graph.getNeighbors(2L)).thenReturn(Collections.singletonList(4L));
    when(graph.getNeighbors(3L)).thenReturn(Collections.emptyList());
    when(graph.getNeighbors(4L)).thenReturn(Collections.emptyList());

    var method = FraudDetectionService.class.getDeclaredMethod(
            "depthFirstTraversal", Long.class, BankGraph.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Set<Long> result = (Set<Long>) method.invoke(service, accountId, graph);

    assertEquals(new LinkedHashSet<>(List.of(1L, 2L, 4L, 3L)), result);
}

    @Test
    void testSuspicionTransactionDate_True() {
        FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);

        Account account = mock(Account.class);
        LocalDate creationDate = LocalDate.now();
        when(account.getCreationDate()).thenReturn(creationDate);

        Transaction transaction = mock(Transaction.class);
        LocalDateTime transactionDateTime = creationDate.atStartOfDay();
        when(transaction.getDateTimeTransaction()).thenReturn(transactionDateTime);

        List<Transaction> incomingTransactions = Collections.singletonList(transaction);

        boolean result = service.suspicionTransactionDate(incomingTransactions, account, 2000.0);
        assertTrue(result);
    }

    @Test
    void testSuspicionTransactionDate_False_DifferentDate() {
        FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);

        Account account = mock(Account.class);
        LocalDate creationDate = LocalDate.now();
        when(account.getCreationDate()).thenReturn(creationDate);

        Transaction transaction = mock(Transaction.class);
        LocalDateTime transactionDateTime = creationDate.minusDays(1).atStartOfDay();
        when(transaction.getDateTimeTransaction()).thenReturn(transactionDateTime);

        List<Transaction> incomingTransactions = Collections.singletonList(transaction);

        boolean result = service.suspicionTransactionDate(incomingTransactions, account, 2000.0);
        assertFalse(result);
    }

    @Test
    void testSuspicionTransactionDate_False_LowValue() {
        FraudDetectionService service = new FraudDetectionService(accountRepository, transactionRepository);

        Account account = mock(Account.class);
        LocalDate creationDate = LocalDate.now();
        when(account.getCreationDate()).thenReturn(creationDate);

        Transaction transaction = mock(Transaction.class);
        LocalDateTime transactionDateTime = creationDate.atStartOfDay();
        when(transaction.getDateTimeTransaction()).thenReturn(transactionDateTime);

        List<Transaction> incomingTransactions = Collections.singletonList(transaction);

        boolean result = service.suspicionTransactionDate(incomingTransactions, account, 500.0);
        assertFalse(result);
    }

}