package com.learning.orange_graph.Service;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Repository.TransactionRepository;
import com.learning.orange_graph.tad.BankGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        Account account = graph.getAccountById(accountId);
        if (account == null) {
            throw new RuntimeException("Conta com ID " + accountId + " não encontrada no grafo.");
        }

        // Definicao do tempo de analise (por padrão, 24 horas)
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(timeWindowHours);

        // Listar as transacoes de entrada e saída da conta
        List<Transaction> incomingTransactions = transactionRepository
                .findByReceiverIdAndDateTimeTransactionBetween(accountId, startTime, endTime);
        List<Transaction> outgoingTransactions = transactionRepository
                .findBySenderIdAndDateTimeTransactionBetween(accountId, startTime, endTime);

        // coleta a quantidade de transacoes onde a conta é o remetente e o destinatario
        int incomingCount = incomingTransactions.size();
        int outgoingCount = outgoingTransactions.size();

        // Coleta o valor total de todas as transacoes onde a conta é o remetente e o destinatario
        double totalIncomingValue = incomingTransactions.stream().mapToDouble(Transaction::getValue).sum();
        double totalOutgoingValue = outgoingTransactions.stream().mapToDouble(Transaction::getValue).sum();

        // Valido se há alto grau de entrada/saida da conta
        boolean highDegree = false;
        if (outgoingCount >= minOutgoingTransactions && incomingCount >= minIncomingTransactions) {
            log.info(String.format("ALTO GRAU DE ENTRADA/SAIDA: %d entradas e %d saídas no periodo.",
                    outgoingCount, incomingCount));
            highDegree = true;
        }

        // Valida se há alto volume de transações saindo e entrando no conta
        boolean highVolume = false;
        if (totalOutgoingValue >= minTotalOutgoingValue && totalIncomingValue >= minTotalIncomingValue) {
            log.info(String.format("ALTO VOLUME DE TRANSAÇÕES: Recebeu R$ %.2f e enviou R$ %.2f.",
                    totalIncomingValue, totalOutgoingValue));
            highVolume = true;
        }

        // valida se as transações são rapidamente repassadas (30 minutos)
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

        // verifica se a conta retem baixo percentual do saldo
        double retentionRatio = 1.0; // 100% de retenção se não ter entradas ou saidas
        if (totalOutgoingValue > 0 && account.getBalance() != null) {
            // redefine o percentual de retenção com base no saldo e o dinheiro movimentado
            retentionRatio = account.getBalance() / totalOutgoingValue;

            // Se percentual de retenção for menor que o limite
            if (retentionRatio < balanceRetentionThreshold) {
                log.info(String.format("BAIXA RETENÇÃO DE SALDO: Saldo atual (R$ %.2f) é muito baixo (%.2f%%) comparado ao volume recebido (R$ %.2f).", account.getBalance(), retentionRatio*100, totalOutgoingValue));
            }
        }

        // Conjunto de IDs de contas remetentes que enviaram para a conta em análise
        Set<Long> uniqueIncomingSenders = incomingTransactions.stream()
                .map(transaction -> transaction.getSender().getId())
                .collect(Collectors.toSet());

        // Conjunto de IDs de contas destinatários que receberam da conta em análise
        Set<Long> uniqueOutgoingReceiversFromGraph = new HashSet<>(graph.getNeighbors(accountId));

        // Se houve uma quantidade maior ou igual ao limite normal de contas remetentes enviando para a conta em análise, dentro do período
        if (uniqueIncomingSenders.size() >= minUniqueCounterpartiesThreshold) {
            log.info(String.format("ALTA DIVERSIDADE DE REMETENTES: Recebeu de %d remetentes únicos.", uniqueIncomingSenders.size()));
        }
        // Se houve uma quantidade maior ou igual ao limite normal de contas destinatárias que a conta em análise fez transações, dentro do período
        if (uniqueOutgoingReceiversFromGraph.size() >= minUniqueCounterpartiesThreshold) {
            log.info(String.format("ALTA DIVERSIDADE DE DESTINATÁRIOS: Enviou para %d destinatários únicos.", uniqueOutgoingReceiversFromGraph.size()));
        }

        // Verificação final da suspeita
        boolean isSuspect = false;
        if (highDegree && highVolume && immediateFlow && (retentionRatio < balanceRetentionThreshold)) {
            isSuspect = true;
        } else if (highDegree && highVolume && uniqueIncomingSenders.size() >= minUniqueCounterpartiesThreshold && uniqueOutgoingReceiversFromGraph.size() >= minUniqueCounterpartiesThreshold) {
            isSuspect = true;
        }

        if (isSuspect) {
            return new SuspicionCheckResponseDto(isSuspect, "ALERTA: Padrões de CONTA LARANJA fortes detectados!");
        } else {
            return new SuspicionCheckResponseDto(isSuspect, String.format("Nenhum padrão forte de conta laranja detectado para a conta %s (ID: %d).", account.getName(), accountId));
        }
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
                sender.setSuspectRating(sender.getSuspectRating()+1);
            }
        }

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
