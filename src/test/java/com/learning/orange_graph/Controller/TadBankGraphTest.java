package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.tad.BankGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

class TadBankGraphTest {

    @Test
    void testAddNode_AddsAccountToGraph() {
        BankGraph graph = new BankGraph();
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(1L);

        graph.addNode(account);

        assertTrue(graph.hasNode(1L));
        assertEquals(account, graph.getAccountById(1L));
        assertTrue(graph.getNeighbors(1L).isEmpty());
    }

    @Test
    void testAddNode_DoesNotDuplicateNode() {
        BankGraph graph = new BankGraph();
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(1L);

        graph.addNode(account);
        graph.addNode(account); 

        assertTrue(graph.hasNode(1L));
        assertEquals(account, graph.getAccountById(1L));
        assertTrue(graph.getNeighbors(1L).isEmpty());
    }

    @Test
void testAddEdge_AddsReceiverToSenderNeighbors() {
    BankGraph graph = new BankGraph();

    Account sender = mock(Account.class);
    Account receiver = mock(Account.class);

    when(sender.getId()).thenReturn(1L);
    when(receiver.getId()).thenReturn(2L);

    graph.addNode(sender);
    graph.addNode(receiver);

    graph.addEdge(sender, receiver);

    assertEquals(List.of(2L), graph.getNeighbors(1L));
    assertTrue(graph.getNeighbors(2L).isEmpty());
}

@Test
void testHasNode_ReturnsTrueIfNodeExists() {
    BankGraph graph = new BankGraph();
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(1L);

    graph.addNode(account);

    assertTrue(graph.hasNode(1L));
}

@Test
void testHasNode_ReturnsFalseIfNodeDoesNotExist() {
    BankGraph graph = new BankGraph();

    assertFalse(graph.hasNode(99L));
}

@Test
void testGetNeighbors_ReturnsNeighborsList() {
    BankGraph graph = new BankGraph();
    Account sender = mock(Account.class);
    Account receiver1 = mock(Account.class);
    Account receiver2 = mock(Account.class);

    when(sender.getId()).thenReturn(1L);
    when(receiver1.getId()).thenReturn(2L);
    when(receiver2.getId()).thenReturn(3L);

    graph.addNode(sender);
    graph.addNode(receiver1);
    graph.addNode(receiver2);

    graph.addEdge(sender, receiver1);
    graph.addEdge(sender, receiver2);

    List<Long> neighbors = graph.getNeighbors(1L);
    assertEquals(List.of(2L, 3L), neighbors);
}

@Test
void testGetNeighbors_ReturnsEmptyListIfNoNeighbors() {
    BankGraph graph = new BankGraph();
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(1L);

    graph.addNode(account);

    List<Long> neighbors = graph.getNeighbors(1L);
    assertTrue(neighbors.isEmpty());
}

@Test
void testGetNeighbors_ReturnsEmptyListIfNodeDoesNotExist() {
    BankGraph graph = new BankGraph();

    List<Long> neighbors = graph.getNeighbors(99L);
    assertTrue(neighbors.isEmpty());
}

@Test
void testGetAccountById_ReturnsAccountIfExists() {
    BankGraph graph = new BankGraph();
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(1L);

    graph.addNode(account);

    Account result = graph.getAccountById(1L);
    assertEquals(account, result);
}

@Test
void testGetAccountById_ReturnsNullIfNotExists() {
    BankGraph graph = new BankGraph();

    Account result = graph.getAccountById(99L);
    assertNull(result);
}
}