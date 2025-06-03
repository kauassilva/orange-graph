package com.learning.orange_graph.Dto.transaction;

public record TransactionRequest(
        Long senderId,
        Long receiverId,
        Double value
) {}