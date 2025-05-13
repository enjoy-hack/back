package com.example.smartair.controller.customUserController;

import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
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
public class CustomUserController implements CustomUserControllerDocs{

    private final CustomUserService customUserService;

    @GetMapping("/customTemp/{roomId}")
    public ResponseEntity<?> getCustom(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable("roomId") Long roomId){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        return ResponseEntity.ok(customUserService.getCustom(user, roomId));
    }



    @PostMapping("/customTemp/{roomId}")
    public ResponseEntity<?> setCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody Double customTemp,
                                              @PathVariable("roomId") Long roomId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        customUserService.saveOrUpdateCustomTemp(userDetails.getUser(), customTemp, roomId);
        return ResponseEntity.ok("success");
    }

    @PutMapping("/customTemp/{roomId}")
    public ResponseEntity<?> updateCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody Double customTemp,
                                              @PathVariable("roomId") Long roomId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        customUserService.saveOrUpdateCustomTemp(userDetails.getUser(), customTemp, roomId);
        return ResponseEntity.ok("success");
    }


    @PostMapping("/customMoi/{roomId}")
    public ResponseEntity<?> setCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody Double customMoi,
                                           @PathVariable("roomId") Long roomId){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        customUserService.saveOrUpdateCustomMoi(user, customMoi, roomId);

        return ResponseEntity.ok("sucess");
    }

    @PutMapping("/customMoi/{roomId}")
    public ResponseEntity<?> updateCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam("customMoi")Double customMoi,
                                             @PathVariable("roomId") Long roomId){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        customUserService.saveOrUpdateCustomMoi(user, customMoi, roomId);

        return ResponseEntity.ok("sucess");
    }

    @DeleteMapping("/customTemp/{roomId}")
    public ResponseEntity<?> deleteCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable("roomId") Long roomId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        customUserService.saveOrUpdateCustomTemp(userDetails.getUser(), null, roomId);
        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/customMoi/{roomId}")
    public ResponseEntity<?> deleteCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable("roomId") Long roomId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        customUserService.saveOrUpdateCustomMoi(userDetails.getUser(), null, roomId);
        return ResponseEntity.ok("success");
    }

}
