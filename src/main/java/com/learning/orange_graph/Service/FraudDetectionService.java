package com.learning.orange_graph.Service;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Repository.TransactionRepository;
import com.learning.orange_graph.tad.BankGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FraudDetectionService {

    public static final int SUSPECT_RATING = 1;

    private final Account_Repository accountRepository;
    private final TransactionRepository transactionRepository;

    public FraudDetectionService(Account_Repository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public SuspicionCheckResponseDto checkSuspicion(
            Long accountId,
            int timeWindowHours,
            int minIncomingTransactions,
            int minOutgoingTransactions,
            double minTotalIncomingValue,
            double minTotalOutgoingValue,
            double balanceRetentionThreshold,
            int minUniqueCounterpartiesThreshold) {

        // Cria o grafo para analise
        BankGraph graph = createInMemoryGraph();

        // Obtem a conta e verifica sua existência
        Account account = getAccountOrThrow(accountId, graph);

        // Definicao do tempo de analise (por padrão, 24 horas)
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(timeWindowHours);

        // Listar as transacoes de entrada e saída da conta
        List<Transaction> incomingTransactions = getIncomingTransactions(accountId, startTime, endTime);
        List<Transaction> outgoingTransactions = getOutgoingTransactions(accountId, startTime, endTime);

        // Coleta o valor total de todas as transacoes onde a conta é o remetente e o destinatario
        double totalIncomingValue = incomingTransactions.stream().mapToDouble(Transaction::getValue).sum();
        double totalOutgoingValue = outgoingTransactions.stream().mapToDouble(Transaction::getValue).sum();

        boolean suspicionTransactionDate = suspicionTransactionDate(incomingTransactions, account, totalIncomingValue);

        // Valida se há alto grau de entrada/saida da conta
        boolean highDegree = hasHighDegree(minIncomingTransactions, minOutgoingTransactions, outgoingTransactions, incomingTransactions);

        // Valida se há alto volume de transações saindo e entrando no conta
        boolean highVolume = hasHighVolume(minTotalIncomingValue, minTotalOutgoingValue, totalOutgoingValue, totalIncomingValue);

        // valida se as transações são rapidamente repassadas (30 minutos)
        boolean immediateFlow = hasImmediateFlow(highDegree, highVolume, incomingTransactions, outgoingTransactions);

        // verifica se a conta retem baixo percentual do saldo
        boolean lowretentionRatio = hasLowRetentionRatio(balanceRetentionThreshold, totalOutgoingValue, account);

        // Se houve uma quantidade maior ou igual ao limite normal de contas remetentes enviando para a conta em análise, dentro do período
        boolean hasManyUniqueIncomingSenders = hasManyUniqueIncomingSenders(minUniqueCounterpartiesThreshold, incomingTransactions);

        // Se houve uma quantidade maior ou igual ao limite normal de contas destinatárias que a conta em análise fez transações, dentro do período
        boolean hasManyUniqueOutgoingReceivers = hasManyUniqueOutgoingReceivers(minUniqueCounterpartiesThreshold, graph, accountId);

        // Verificação final da suspeita
        boolean isSuspect = isSuspect(highDegree, highVolume, immediateFlow, lowretentionRatio, hasManyUniqueIncomingSenders, hasManyUniqueOutgoingReceivers, suspicionTransactionDate);

        return getSuspicionCheckResponseDto(accountId, isSuspect, account);
    }

    private static SuspicionCheckResponseDto getSuspicionCheckResponseDto(Long accountId, boolean isSuspect, Account account) {
        if (isSuspect) {
            return new SuspicionCheckResponseDto(isSuspect, "ALERTA: Padrões de CONTA LARANJA fortes detectados!");
        } else {
            return new SuspicionCheckResponseDto(isSuspect, String.format("Nenhum padrão forte de conta laranja detectado para a conta %s (ID: %d).", account.getName(), accountId));
        }
    }

    private static boolean isSuspect(boolean highDegree, boolean highVolume, boolean immediateFlow, boolean lowretentionRatio, boolean hasManyUniqueIncomingSenders, boolean hasManyUniqueOutgoingReceivers, boolean suspicionTransactionDate) {
        boolean isSuspect = false;

        if (highDegree && highVolume && immediateFlow && lowretentionRatio && suspicionTransactionDate) {
            isSuspect = true;
        } else if (highDegree && highVolume && hasManyUniqueIncomingSenders && hasManyUniqueOutgoingReceivers) {
            isSuspect = true;
        }

        return isSuspect;
    }

    private static boolean hasManyUniqueOutgoingReceivers(int minUniqueCounterpartiesThreshold, BankGraph graph, Long accountId) {
        Set<Long> uniqueOutgoingReceiversFromGraph = new HashSet<>(graph.getNeighbors(accountId));

        if (uniqueOutgoingReceiversFromGraph.size() >= minUniqueCounterpartiesThreshold) {
            log.info(String.format("ALTA DIVERSIDADE DE DESTINATÁRIOS: Enviou para %d destinatários únicos.", uniqueOutgoingReceiversFromGraph.size()));
            return true;
        }

        return false;
    }

    private static boolean hasManyUniqueIncomingSenders(int minUniqueCounterpartiesThreshold, List<Transaction> incomingTransactions) {
        Set<Long> uniqueIncomingSenders = incomingTransactions.stream()
                .map(transaction -> transaction.getSender().getId())
                .collect(Collectors.toSet());

        if (uniqueIncomingSenders.size() >= minUniqueCounterpartiesThreshold) {
            log.info(String.format("ALTA DIVERSIDADE DE REMETENTES: Recebeu de %d remetentes únicos.", uniqueIncomingSenders.size()));
            return true;
        }

        return false;
    }

    private static boolean hasLowRetentionRatio(double balanceRetentionThreshold, double totalOutgoingValue, Account account) {
        if (totalOutgoingValue > 0 && account.getBalance() != null) {
            double retentionRatio = account.getBalance() / totalOutgoingValue;

            // Se percentual de retenção for menor que o limite
            if (retentionRatio < balanceRetentionThreshold) {
                log.info(String.format("BAIXA RETENÇÃO DE SALDO: Saldo atual (R$ %.2f) é muito baixo (%.2f%%) comparado ao volume recebido (R$ %.2f).", account.getBalance(), retentionRatio*100, totalOutgoingValue));
                return true;
            }
        }

        return false;
    }

    private static boolean hasImmediateFlow(boolean highDegree, boolean highVolume, List<Transaction> incomingTransactions, List<Transaction> outgoingTransactions) {
        boolean immediateFlow = false;

        if (highDegree && highVolume) {
            // Para cada transação
            for (Transaction incoming : incomingTransactions) {
                // Verifica se existe alguma transação de saída logo após a entrada (de até 30 minutos)
                boolean foundImmediateOutgoing = outgoingTransactions.stream()
                    .anyMatch(outgoing ->
                        outgoing.getDateTimeTransaction().isAfter(incoming.getDateTimeTransaction()) &&
                        outgoing.getDateTimeTransaction().isBefore(incoming.getDateTimeTransaction().plusMinutes(30))
                    );

                // Se encontrou pelo menos uma, marca como true e interrompe o laço
                if (foundImmediateOutgoing) {
                    immediateFlow = true;
                    break;
                }
            }

            // Registra no log, se houve fluxo imediato
            if (immediateFlow) {
                log.info("FLUXO IMEDIATO DE VALORES: Dinheiro recebido é rapidamente repassado.");
            }
        }

        return immediateFlow;
    }

    private static boolean hasHighVolume(double minTotalIncomingValue, double minTotalOutgoingValue, double totalOutgoingValue, double totalIncomingValue) {
        if (totalOutgoingValue >= minTotalOutgoingValue && totalIncomingValue >= minTotalIncomingValue) {
            log.info(String.format("ALTO VOLUME DE TRANSAÇÕES: Recebeu R$ %.2f e enviou R$ %.2f.",
                    totalIncomingValue, totalOutgoingValue));
            return true;
        }

        return false;
    }

    private static boolean hasHighDegree(int minIncomingTransactions, int minOutgoingTransactions, List<Transaction> outgoingTransactions, List<Transaction> incomingTransactions) {
        int incomingCount = incomingTransactions.size();
        int outgoingCount = outgoingTransactions.size();

        if (outgoingCount >= minOutgoingTransactions && incomingCount >= minIncomingTransactions) {
            log.info(String.format("ALTO GRAU DE ENTRADA/SAIDA: %d entradas e %d saídas no periodo.",
                    outgoingCount, incomingCount));
            return true;
        }

        return false;
    }

    private List<Transaction> getOutgoingTransactions(Long accountId, LocalDateTime startTime, LocalDateTime endTime) {
        return transactionRepository
                .findBySenderIdAndDateTimeTransactionBetween(accountId, startTime, endTime);
    }

    private List<Transaction> getIncomingTransactions(Long accountId, LocalDateTime startTime, LocalDateTime endTime) {
        return transactionRepository
                .findByReceiverIdAndDateTimeTransactionBetween(accountId, startTime, endTime);
    }

    private static Account getAccountOrThrow(Long accountId, BankGraph graph) {
        Account account = graph.getAccountById(accountId);

        if (account == null) {
            throw new RuntimeException("Conta com ID " + accountId + " não encontrada no grafo.");
        }

        return account;
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

    private Set<Long> depthFirstTraversal(Long accountId, BankGraph graph) {
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

    public boolean suspicionTransactionDate(List<Transaction> incomingTransactions, Account account, double totalIncomingValue){
        Transaction transaction = incomingTransactions.get(0);

        LocalDate transactionDate = transaction.getDateTimeTransaction().toLocalDate();
        LocalDate creationDate = account.getCreationDate();

        if (transactionDate.equals(creationDate) && totalIncomingValue > 1000) {
            log.info(String.format("Conta recebeu um alto valor no mesmo dia de sua criação"));
            return true;
        }
        return false;

    }

}
