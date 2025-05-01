package com.example.smartair.controller.customUserController;

import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.customUserService.CustomUserService;
import com.example.smartair.service.customUserService.UserSatisfactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomUserController {

    private final CustomUserService customUserService;

    @GetMapping("/customTemp")
    public ResponseEntity<?> getCustom(@AuthenticationPrincipal CustomUserDetails userDetails){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        return ResponseEntity.ok(customUserService.getCustom(user));
    }

    @PostMapping("/customTemp")
    public ResponseEntity<?> setCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody Double customTemp){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        customUserService.setCustomTemp(user, customTemp);

        return ResponseEntity.ok("sucess");
    }

    @PutMapping("/customTemp")
    public ResponseEntity<?> updateCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody Double customTemp){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        customUserService.updateCustomTemp(user, customTemp);

        return ResponseEntity.ok("sucess");
    }

    @PostMapping("/customMoi")
    public ResponseEntity<?> setCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody Double customMoi){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        customUserService.setCustomMoi(user, customMoi);

        return ResponseEntity.ok("sucess");
    }

    @PutMapping("/customMoi")
    public ResponseEntity<?> updateCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody Double customMoi){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        customUserService.updateCustomMoi(user, customMoi);

        return ResponseEntity.ok("sucess");
    }



}
