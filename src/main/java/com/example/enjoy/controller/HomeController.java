package com.example.enjoy.controller;


import ch.qos.logback.core.model.Model;
import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TrackService trackService;

    @GetMapping("/home")
    public String showMyProgress(Model model) { // 1. Model 객체를 파라미터로 추가
        // TODO: 추후 Spring Security 등과 연동하여 실제 로그인한 사용자 ID를 가져와야 함
        Long currentStudentId = 1L; // 2. 테스트용 임시 학생 ID 사용

        // 3. 학생의 이수 현황을 계산하는 새로운 서비스 메서드 호출
        List<TrackProgressDto> progressData = trackService.calculateTrackProgress(currentStudentId);

        // 4. 조회된 데이터를 "progressData"라는 이름으로 모델에 추가
        //model.addAttribute("progressData", progressData);

        // 5. 데이터를 표시할 뷰(html)의 이름을 반환
        return "home";
    }
}
