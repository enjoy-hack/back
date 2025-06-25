package com.example.smartair.controller.userController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그인", description = "로그인 관련 API")
@RestController
@Slf4j
@AllArgsConstructor
public class LoginController {





//    @Operation(
//            summary = "로그인",
//            description = "이메일과 비밀번호로 로그인하여 액세스 토큰과 리프레시 토큰을 받습니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "로그인 성공",
//                            content = @Content(
//                                    schema = @Schema(implementation = TokenDto.class)
//                            )
//                    )
//            }
//    )
//    @PostMapping("/login")
//    public ResponseEntity<TokenDto> login(@RequestBody LoginDTO loginRequestDto) {
//        TokenDto tokenDto = loginService.login(loginRequestDto);
//        // 응답 본문에 TokenDto를 직접 반환
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(tokenDto);
//    }

}
