package com.learning.orange_graph.Controller;

import com.learning.orange_graph.Dto.SuspicionCheckResponseDto;
import com.learning.orange_graph.Service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudDetectionControllerTest {

    @Mock
    private FraudDetectionService service;

    @InjectMocks
    private FraudDetectionController controller;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testCheckSuspicion_Success() {
        SuspicionCheckResponseDto dto = Mockito.mock(SuspicionCheckResponseDto.class);
        when(service.checkSuspicion(anyLong(), anyInt(), anyInt(), anyInt(), anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(dto);

        ResponseEntity<SuspicionCheckResponseDto> response = controller.checkSuspicion(
                1L, 24, 5, 5, 1000.0, 900.0, 0.1, 3);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(dto, response.getBody());
        verify(service).checkSuspicion(1L, 24, 5, 5, 1000.0, 900.0, 0.1, 3);
    }

}