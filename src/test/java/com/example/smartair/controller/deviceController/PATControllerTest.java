package com.example.smartair.controller.deviceController;

import com.example.smartair.dto.deviceDto.PATRequestDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.deviceService.PATService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PATControllerTest {

    @Mock
    private PATService patService;

    @InjectMocks
    private PATController patController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // @Mock 및 @InjectMocks 초기화
    }

    @Test
    void savePAT_ValidRequest_ShouldReturnOk() throws Exception {
        // Given
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User user = new User();
        user.setId(1L);
        when(userDetails.getUser()).thenReturn(user);

        PATRequestDto request = new PATRequestDto();
        request.setPatToken("validToken");

        when(patService.savePAT(user, request))
                .thenReturn(ResponseEntity.ok("PAT 토큰이 암호화되어 저장되었습니다."));

        // When
        ResponseEntity<?> response = patController.savePAT(userDetails, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("PAT 토큰이 암호화되어 저장되었습니다.", response.getBody());
        verify(patService, times(1)).savePAT(user, request);
    }

    @Test
    void savePAT_InvalidToken_ShouldReturnBadRequest() throws Exception {
        // Given
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        PATRequestDto request = new PATRequestDto();
        request.setPatToken(""); // 유효하지 않은 토큰

        // When
        ResponseEntity<?> response = patController.savePAT(userDetails, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("PAT 토큰이 유효하지 않습니다.", response.getBody());
        verify(patService, never()).savePAT(any(), any());
    }

    @Test
    void savePAT_NullUserDetails_ShouldReturnUnauthorized() throws Exception {
        // Given
        PATRequestDto request = new PATRequestDto();
        request.setPatToken("validToken");

        // When
        ResponseEntity<?> response = patController.savePAT(null, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid Token", response.getBody());
        verify(patService, never()).savePAT(any(), any());
    }
}