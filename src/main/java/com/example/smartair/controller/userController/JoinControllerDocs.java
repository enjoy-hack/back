package com.example.smartair.controller.userController;

import com.example.smartair.dto.userDto.JoinDTO;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.service.UserService.JoinService;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;

public interface JoinControllerDocs {

    @Operation(
            summary = "사용자 회원가입",
            description = """
            ## 사용자 회원가입 요청

            사용자가 이메일, 닉네임, 비밀번호, 역할(ROLE)을 입력하여 회원가입을 시도합니다.

            ---

            **요청 형식 (RequestBody)**
            ```json
            {
          "email": "example@example.com",
          "nickname": "홍길동",
          "password": "password123",
          "role": "USER"
            }
            ```

            **Role 값 예시**
            - USER
            - ADMIN
            - MANAGER

            ---

            **응답**
            - `200 OK`: 회원가입 성공 - `{ "message": "success" }`
            - `400 Bad Request`: 이미 존재하는 사용자 - `{ "errorCode": "USER_ALREADY_EXISTS" }`
            """,
            requestBody = @RequestBody(
                    description = "회원가입에 필요한 사용자 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = JoinDTO.class),
                            examples = @ExampleObject(
                                    name = "Join Request Example",
                                    value = "{\n  \"email\": \"test@example.com\",\n  \"nickname\": \"tester\",\n  \"password\": \"securePass123\",\n  \"role\": \"USER\"\n}"
                            )
                    )
            )
    )
    ResponseEntity<?> joinProcess(JoinDTO joinDTO);
}
