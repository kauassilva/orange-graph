package com.learning.orange_graph.Dto.transaction;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private String senderName;
    private String receiverName;
    private Double value;
    private LocalDateTime transactionDate;
}
