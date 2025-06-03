package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.transaction.TransactionResponse;
import com.learning.orange_graph.Dto.transaction.TransactionRequest;
import com.learning.orange_graph.Entity.Transaction;
import com.learning.orange_graph.Service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("transaction")
@Tag(name = "Transações", description = "Requsição POST para operações de transação")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Realiza transação entre contas", description = "Endpoint para realização de uma transação entre duas contas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<TransactionResponse> performTransaction(@RequestBody TransactionRequest transactionRequest) {
        Transaction transaction = transactionService.performTransaction(transactionRequest);

        TransactionResponse response = new TransactionResponse(
                transaction.getId(),
                transaction.getSender().getName(),
                transaction.getReceiver().getName(),
                transaction.getValue(),
                transaction.getDate_Time_Transaction()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}