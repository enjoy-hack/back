package com.example.smartair.controller.hvacController;

import com.example.smartair.dto.hvacDto.PATRequestDto;
import com.example.smartair.entity.hvacSetting.PATEntity;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.hvacRepository.PATRepository;
import com.example.smartair.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PATControllerTest {

    @Mock
    private PATRepository patRepository;

    @InjectMocks
    private PATController patController;

    @Mock
    private EncryptionUtil encryptionUtil; // EncryptionUtil을 모킹하여 암호화/복호화 기능을 테스트

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // @Mock, @InjectMocks 적용
    }

    @Test
    void savePAT_ValidRequest_ShouldReturnOk() throws Exception {
        // ✅ 테스트 목적: PAT 토큰이 정상적으로 들어온 경우, 암호화 후 저장되고 200 OK 반환
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        User user = new User();
        user.setId(1L);
        when(userDetails.getUser()).thenReturn(user);  // 사용자 정보 설정

        PATRequestDto request = new PATRequestDto();
        request.setPatToken("validToken");  // 유효한 토큰 설정

        // EncryptionUtil.encrypt() mocking (정상 암호화 반환)
        String encryptedToken = "encryptedToken";

        when(encryptionUtil.encrypt("validToken")).thenReturn(encryptedToken);

        // 호출
        ResponseEntity<?> response = patController.savePAT(userDetails, request);

        // ✅ 검증
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("PAT를 암호화하여 저장하였습니다.", response.getBody());
        verify(patRepository, times(1)).save(any(PATEntity.class));  // 저장 메서드 1번 호출 확인
    }

    @Test
    void savePAT_InvalidToken_ShouldReturnBadRequest() {
        // ✅ 테스트 목적: 빈 PAT 토큰을 보낼 경우, 400 Bad Request 반환 및 저장하지 않음
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        PATRequestDto request = new PATRequestDto();
        request.setPatToken("");  // 비어있는 토큰

        ResponseEntity<?> response = patController.savePAT(userDetails, request);

        // ✅ 검증
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("PAT 토큰이 유효하지 않습니다.", response.getBody());
        verify(patRepository, never()).save(any(PATEntity.class));  // 저장 호출 없어야 함
    }

    @Test
    void savePAT_NullUserDetails_ShouldReturnUnauthorized() {
        // ✅ 테스트 목적: 인증 정보 없이 요청한 경우, 401 Unauthorized 반환
        PATRequestDto request = new PATRequestDto();
        request.setPatToken("validToken");

        ResponseEntity<?> response = patController.savePAT(null, request);

        // ✅ 검증
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid Token", response.getBody());
        verify(patRepository, never()).save(any(PATEntity.class));  // 저장 호출 없어야 함
    }
}
