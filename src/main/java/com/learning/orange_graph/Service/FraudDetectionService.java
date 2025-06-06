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

    public Transaction validateTransaction(Long transactionId){
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
    }

    public SuspicionCheckResponseDto suspicionLotTransaction(Long transactionId){
        Transaction transaction = validateTransaction(transactionId);
        Account sender = transaction.getSender();
        Account receiver = transaction.getReceiver();
        Long receiverId = receiver.getId();

        boolean isSuspect = false;
        SuspicionCheckResponseDto suspicionCheckResponseDto;

        List<Transaction> transactionSender = transactionRepository.findBySender(sender);
        Map<Double, Integer> countValue = new HashMap<>();

        for (Transaction t : transactionSender) {
            Double value = t.getValue();
            countValue.put(value, countValue.getOrDefault(value, 0) + 1);
        }


        for (Map.Entry<Double, Integer> entry : countValue.entrySet()) {
            if(entry.getValue() >= 5) {
               isSuspect = true;
            }
        }
        sender.setSuspect(isSuspect);
        accountRepository.save(sender);

         if (isSuspect) {
            suspicionCheckResponseDto = new SuspicionCheckResponseDto(isSuspect, "A conta " +sender.getName() + " demonstrou transações suspeitas");
        } else {
            suspicionCheckResponseDto = new SuspicionCheckResponseDto(isSuspect, "Sem transações suspeitas");
        }

        BankGraph graph = createInMemoryGraph();
        System.out.println(graph);
        Set<Long> longs = depthFirstTraversal(receiverId);
        System.out.println(longs);
        return suspicionCheckResponseDto;

        
    }

    

    

}


//ESSE AQUI É O CERTO
