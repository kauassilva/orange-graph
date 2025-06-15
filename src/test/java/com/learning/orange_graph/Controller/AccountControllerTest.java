package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Entity.Account;
import com.learning.orange_graph.Service.Account_Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    private Account_Service accountService;
    private Account_Controller controller;

    @BeforeEach
    void setUp() {
        accountService = mock(Account_Service.class);
        controller = new Account_Controller(accountService);
    }

    @Test
    void testFindAllAccount_ReturnsList() {
        Account account1 = mock(Account.class);
        Account account2 = mock(Account.class);
        List<Account> expectedList = List.of(account1, account2);

        when(accountService.findAllAccount()).thenReturn(expectedList);

        ResponseEntity<List<Account>> response = controller.findAllAccount();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(expectedList, response.getBody());
        verify(accountService).findAllAccount();
    }

    @Test
void testFindAccountById_ReturnsAccount() {
    Long id = 1L;
    Account account = mock(Account.class);
    Optional<Account> optionalAccount = Optional.of(account);

    when(accountService.findAccountById(id)).thenReturn(optionalAccount);

    ResponseEntity<Optional<Account>> response = controller.findAccountById(id);

    assertEquals(200, response.getStatusCodeValue());
    assertTrue(response.getBody().isPresent());
    assertEquals(account, response.getBody().get());
    verify(accountService).findAccountById(id);
}

@Test
void testFindAccountById_NotFound() {
    Long id = 2L;
    when(accountService.findAccountById(id)).thenReturn(Optional.empty());

    ResponseEntity<Optional<Account>> response = controller.findAccountById(id);

    assertEquals(404, response.getStatusCodeValue());
    assertNull(response.getBody());
    verify(accountService).findAccountById(id);
}

@Test
void testCreateAccount_ReturnsCreatedAccount() {
    com.learning.orange_graph.Dto.Account_DTO accountDTO = mock(com.learning.orange_graph.Dto.Account_DTO.class);
    Account newAccount = mock(Account.class);

    when(accountService.createAccount(accountDTO)).thenReturn(newAccount);

    ResponseEntity<Account> response = controller.createAccount(accountDTO);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(newAccount, response.getBody());
    verify(accountService).createAccount(accountDTO);
}

@Test
void testUpdateAccount_ReturnsUpdatedAccount() {
    Long id = 1L;
    com.learning.orange_graph.Dto.Account_DTO accountDTO = mock(com.learning.orange_graph.Dto.Account_DTO.class);
    Account updatedAccount = mock(Account.class);

    when(accountService.updateAccount(id, accountDTO)).thenReturn(updatedAccount);

    ResponseEntity<Account> response = controller.updateAccount(id, accountDTO);

    assertEquals(200, response.getStatusCodeValue());
    assertEquals(updatedAccount, response.getBody());
    verify(accountService).updateAccount(id, accountDTO);
}

 @Test
void testDeleteAccountById_ReturnsNoContent() {
    Long id = 1L;

    ResponseEntity<?> response = controller.deleteAccountById(id);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(accountService).deleteAccountById(id);
}

}