package com.learning.orange_graph.Service;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Repository.Account_Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FraudDetectionService {

    private final Account_Repository accountRepository;

    public FraudDetectionService(Account_Repository accountRepository) {
        this.accountRepository = accountRepository;
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

        return suspicionCheckResponseDto;
    }

}
