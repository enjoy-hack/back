package com.example.smartair.service.hvacService;

import com.example.smartair.entity.user.User;
import com.example.smartair.repository.hvacRepository.PATRepository;
import com.example.smartair.service.hvacService.ThinQService;
import com.example.smartair.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ThinQServiceTest {

    @Mock
    private RestTemplate restTemplate; // RestTemplate을 모킹하여 실제 HTTP 호출을 방지

    @Mock
    private PATRepository patRepository; // PATRepository를 모킹하여 데이터베이스 호출을 방지

    @InjectMocks
    private ThinQService thinQService; // 테스트 대상 클래스

    @Mock
    private EncryptionUtil encryptionUtil; // EncryptionUtil을 모킹하여 암호화/복호화 기능을 테스트

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    void getDeviceList_ValidPat_ShouldReturnDeviceList() throws Exception {
        // given: 테스트에 필요한 데이터와 모킹 설정
        User user = new User();
        user.setId(1L);

        String encryptedPat = "encryptedPat";
        String decryptedPat = "decryptedPat";

        // PATRepository 모킹: 사용자 ID로 PAT를 찾았을 때 암호화된 PAT 반환
        when(patRepository.findByUserId(user.getId()))
                .thenReturn(java.util.Optional.of(new com.example.smartair.entity.hvacSetting.PATEntity(user.getId(), encryptedPat)));

        // EncryptionUtil 모킹: 암호화된 PAT를 복호화
        mockStatic(EncryptionUtil.class);
        when(encryptionUtil.decrypt(encryptedPat)).thenReturn(decryptedPat);

        // RestTemplate 모킹: API 호출 결과를 반환
        String apiResponse = "{\"devices\":[]}";
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(apiResponse));

        // when: 테스트 대상 메서드 호출
        ResponseEntity<String> response = thinQService.getDeviceList(user);

        // then: 결과 검증
        assertEquals(200, response.getStatusCodeValue()); // HTTP 상태 코드가 200인지 확인
        assertEquals(apiResponse, response.getBody()); // 응답 본문이 예상 값과 일치하는지 확인

        // PAT 복호화 및 API 호출이 정확히 한 번씩 호출되었는지 검증
        verify(patRepository, times(1)).findByUserId(user.getId());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class));
    }

    @Test
    void getDeviceList_NoPat_ShouldThrowException() {
        // given: PATRepository가 빈 값을 반환하도록 설정
        User user = new User();
        user.setId(1L);
        when(patRepository.findByUserId(user.getId())).thenReturn(java.util.Optional.empty());

        // when & then: 예외가 발생하는지 확인
        Exception exception = assertThrows(RuntimeException.class, () -> thinQService.getDeviceList(user));
        assertEquals("PAT 토큰을 찾을 수 없습니다. 사용자 ID: " + user.getId(), exception.getMessage());

        // PAT 복호화 및 API 호출이 발생하지 않았는지 검증
        verify(patRepository, times(1)).findByUserId(user.getId());
        verifyNoInteractions(restTemplate);
    }
}