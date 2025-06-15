package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.Account_DTO;
import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Repository.Account_Repository;
import com.learning.orange_graph.Service.Account_Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private Account_Repository accountRepository;
    private Account_Service accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(Account_Repository.class);
        accountService = new Account_Service(accountRepository);
    }

    @Test
    void testFindAllAccount_ReturnsList() {
        Account account1 = mock(Account.class);
        Account account2 = mock(Account.class);
        List<Account> expectedList = List.of(account1, account2);

        when(accountRepository.findAll()).thenReturn(expectedList);

        List<Account> result = accountService.findAllAccount();

        assertEquals(expectedList, result);
        verify(accountRepository).findAll();
    }

    @Test
    void testFindAccountById_ReturnsAccount() {
        Long id = 1L;
        Account account = mock(Account.class);
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findAccountById(id);

        assertTrue(result.isPresent());
        assertEquals(account, result.get());
        verify(accountRepository).findById(id);
    }

    @Test
    void testFindAccountById_ThrowsExceptionWhenNotFound() {
        Long id = 2L;
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountService.findAccountById(id));
        verify(accountRepository).findById(id);
    }

    @Test
void testCreateAccount_Success() {
    Account_DTO accountDTO = mock(Account_DTO.class);
    Account account = mock(Account.class);

    when(accountDTO.toAccount()).thenReturn(account);
    when(accountRepository.save(account)).thenReturn(account);

    Account result = accountService.createAccount(accountDTO);

    assertEquals(account, result);
    verify(accountDTO).toAccount();
    verify(accountRepository).save(account);
}

@Test
void testUpdateAccount_Success() {
    Long id = 1L;
    Account_DTO accountDTO = mock(Account_DTO.class);
    Account existingAccount = mock(Account.class);

    when(accountRepository.findById(id)).thenReturn(Optional.of(existingAccount));
    when(accountDTO.name()).thenReturn("Novo Nome");
    when(accountDTO.cpf()).thenReturn("12345678900");
    when(accountDTO.email()).thenReturn("novo@email.com");
    when(accountDTO.balance()).thenReturn(1000.0);
    when(accountDTO.suspectRating()).thenReturn(2);

    when(accountRepository.save(existingAccount)).thenReturn(existingAccount);

    Account result = accountService.updateAccount(id, accountDTO);

    assertEquals(existingAccount, result);
    verify(existingAccount).setName("Novo Nome");
    verify(existingAccount).setCpf("12345678900");
    verify(existingAccount).setEmail("novo@email.com");
    verify(existingAccount).setBalance(1000.0);
    verify(existingAccount).setSuspectRating(2);
    verify(accountRepository).save(existingAccount);
}

@Test
void testDeleteAccountById_CallsRepository() {
    Long id = 1L;

    accountService.deleteAccountById(id);

    verify(accountRepository).deleteById(id);
}
}