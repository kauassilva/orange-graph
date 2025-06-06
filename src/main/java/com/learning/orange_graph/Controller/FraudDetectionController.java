package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Service.FraudDetectionService;

import javax.management.RuntimeErrorException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check-suspicion")
public class FraudDetectionController {

    private final FraudDetectionService service;

    public FraudDetectionController(FraudDetectionService service) {
        this.service = service;
    }

    @GetMapping("/{receiverId}")
    public ResponseEntity<SuspicionCheckResponseDto> checkSuspicion(@PathVariable Long receiverId) {
        if (receiverId == null) {
            throw new RuntimeException("Receiver ID cannot be null");
        }

        SuspicionCheckResponseDto response = service.checkSuspicion(receiverId);

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
