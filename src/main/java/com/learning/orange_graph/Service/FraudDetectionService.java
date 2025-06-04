package com.learning.orange_graph.Service;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Repository.TransactionRepository;
import com.learning.orange_graph.tad.BankGraph;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FraudDetectionService {

    private final Account_Repository accountRepository;
    private final TransactionRepository transactionRepository;

    public FraudDetectionService(Account_Repository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public SuspicionCheckResponseDto checkSuspicion(Long receiverId) {
        Optional<Account> tempAccount = accountRepository.findById(receiverId);
        Account account = tempAccount.get();

        boolean isSuspect = account.getSuspect();
        SuspicionCheckResponseDto suspicionCheckResponseDto;

        if (isSuspect) {
            suspicionCheckResponseDto = new SuspicionCheckResponseDto(isSuspect, "Conta maldita que rouba os outros!");
        } else {
            suspicionCheckResponseDto = new SuspicionCheckResponseDto(isSuspect, "Conta do bem, pode fazer o PIX!");
        }

        BankGraph graph = createInMemoryGraph();
        System.out.println(graph);
        Set<Long> longs = depthFirstTraversal(receiverId);
        System.out.println(longs);

        return suspicionCheckResponseDto;
    }

    private BankGraph createInMemoryGraph() {
        List<Account> accounts = accountRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();
        BankGraph bankGraph = new BankGraph();

        // Adiciona os nós/vértices no grafo
        accounts.forEach(bankGraph::addNode);

        // Adiciona as 'relações' entre os nós/vértices
        transactions.forEach(transaction
                -> bankGraph.addEdge(transaction.getSender(), transaction.getReceiver()));

        return bankGraph;
    }

    private Set<Long> depthFirstTraversal(Long accountId) {
        BankGraph graph = createInMemoryGraph();

        // Conjunto para armazenar os nós visitados em ordem
        Set<Long> visited = new LinkedHashSet<>();
        // Pilha para gerenciar os nós a serem visitados
        Stack<Long> stack = new Stack<>();

        // Se o grafo não possui a conta, retorna vazio
        if (!graph.hasNode(accountId)) {
            return visited;
        }

        // Adiciona o nó raiz na pilha
        stack.push(accountId);

        // Enquanto a pilha não estiver vazia
        while (!stack.isEmpty()) {
            // Remove o nó do topo da pilha
            Long currentVertexId = stack.pop();

            // Se o nó ainda não foi visitado
            if (!visited.contains(currentVertexId)) {
                // Marca como visitado
                visited.add(currentVertexId);

                // Obtém os vizinhos (contas que o nó atual transferiu)
                List<Long> neighbors = graph.getNeighbors(currentVertexId);

                for (int i = neighbors.size()-1; i >= 0; i--) {
                    Long neighborId = neighbors.get(i);

                    // Empilha apenas se ainda não foi visitado
                    if (!visited.contains(neighborId)) {
                        stack.push(neighborId);
                    }
                }
            }
        }

        return visited;
    }

}
