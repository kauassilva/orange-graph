package com.learning.orange_graph.tad;

import com.learning.orange_graph.Entity.Account;

import java.util.*;

public class BankGraph {

    private Map<Long, List<Long>> adjacentList = new HashMap<>();
    private Map<Long, Account> accountMap = new HashMap<>();

    public void addNode(Account account) {
        if (!adjacentList.containsKey(account.getId())) {
            adjacentList.put(account.getId(), new ArrayList<>());
            accountMap.put(account.getId(), account);
        }
    }

    public void addEdge(Account sender, Account receiver) {
        adjacentList.get(sender.getId()).add(receiver.getId());
    }

    public boolean hasNode(Long accountId) {
        return adjacentList.containsKey(accountId);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Long nodeId : adjacentList.keySet()) {
            Account account = accountMap.get(nodeId);
            stringBuilder.append("[ ").append(account.getName()).append(" ] fez transações para: ");

            for (Long neighbourId : adjacentList.get(nodeId)) {
                Account neighbourAccount = accountMap.get(neighbourId);
                stringBuilder.append("[ ").append(neighbourAccount.getName()).append(" ], ");
            }

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public List<Long> getNeighbors(Long accountId) {
        return adjacentList.getOrDefault(accountId, Collections.emptyList());
    }
}
