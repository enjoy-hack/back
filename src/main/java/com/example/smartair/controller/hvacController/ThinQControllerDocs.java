package com.example.smartair.controller.hvacController;

import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "ThinQ API", description = "LG ThinQ 디바이스 제어 및 상태 조회 API")
public interface ThinQControllerDocs {

}
