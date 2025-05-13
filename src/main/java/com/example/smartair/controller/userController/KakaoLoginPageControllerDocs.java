package com.example.smartair.controller.userController;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "카카오 로그인 페이지", description = "카카오 OAuth2 로그인 페이지 렌더링 API")
@RequestMapping("/login")
public interface KakaoLoginPageControllerDocs {

    @Operation(
            summary = "카카오 로그인 페이지 링크 제공",
            description = """
            ## 카카오 로그인 링크 렌더링

            클라이언트에서 카카오 로그인을 요청할 수 있도록 OAuth2 인증 URL을 포함한 login 페이지를 반환합니다.

            ---
            **응답 (`Model`)**
            - `location`: 카카오 로그인 redirect URL (String)

            해당 location 값을 통해 사용자는 Kakao OAuth2 인증을 시작할 수 있습니다.

            ---
            **예시 URL 형식**
            ```text
            https://kauth.kakao.com/oauth/authorize?response_type=code&client_id={client_id}&redirect_uri={redirect_uri}
            ```
            """
    )
    @GetMapping("/page")
    String loginPage(Model model);
}

