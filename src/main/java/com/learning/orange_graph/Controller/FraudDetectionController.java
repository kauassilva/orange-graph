package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Service.FraudDetectionService;

import javax.management.RuntimeErrorException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/check-suspicion")
public class FraudDetectionController {

    private final FraudDetectionService service;

    public FraudDetectionController(FraudDetectionService service) {
        this.service = service;
    }

    @GetMapping("/{receiverId}")
    public ResponseEntity<SuspicionCheckResponseDto> checkSuspicion(
            @PathVariable Long receiverId,
            @RequestParam(defaultValue = "24") int timeWindowHours,
            @RequestParam(defaultValue = "5") int minIncomingTransactions,
            @RequestParam(defaultValue = "5") int minOutgoingTransactions,
            @RequestParam(defaultValue = "1000.0") double minTotalIncomingValue,
            @RequestParam(defaultValue = "900.0") double minTotalOutgoingValue,
            @RequestParam(defaultValue = "0.1") double balanceRetentionThreshold,
            @RequestParam(defaultValue = "3") int minUniqueCounterpartiesThreshold) {

        SuspicionCheckResponseDto response = service.checkSuspicion(
                receiverId, timeWindowHours, minIncomingTransactions, minOutgoingTransactions,minTotalIncomingValue,
                minTotalOutgoingValue, balanceRetentionThreshold, minUniqueCounterpartiesThreshold);

        return ResponseEntity.ok(response);
    }

   @PostMapping("/lot-transaction/{transactionId}")
   public ResponseEntity<SuspicionCheckResponseDto> suspicionLotTransaction(@PathVariable Long transactionId){
    if (transactionId == null) {
        throw new RuntimeException("Transaction ID cannot be null");
    }

    
    SuspicionCheckResponseDto response = service.suspicionLotTransaction(transactionId);
    return ResponseEntity.ok(response);
   }

    

}
