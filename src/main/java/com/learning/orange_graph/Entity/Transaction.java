package com.learning.orange_graph.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "TRANSACTION_TABLE")

public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private Account receiver;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Account recipient;

    @Column(name = "value")
    private Double value;

    @Column(name = "date_Time_Transaction")
    private LocalDateTime date_Time_Transaction;

}
