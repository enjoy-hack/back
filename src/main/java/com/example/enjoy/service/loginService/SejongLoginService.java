package com.example.enjoy.service.loginService;


import com.chuseok22.sejongportallogin.core.SejongMemberInfo;
import com.chuseok22.sejongportallogin.infrastructure.SejongPortalLoginService;
import com.example.enjoy.dto.loginDto.MemberCommand;
import com.example.enjoy.dto.loginDto.MemberDto;
import com.example.enjoy.entity.user.User;
import com.example.enjoy.exception.CustomException;
import com.example.enjoy.exception.ErrorCode;
import com.example.enjoy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import okhttp3.JavaNetCookieJar;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SejongLoginService {

    private final SejongPortalLoginService sejongPortalLoginService;
    private final UserRepository userRepository;

    public MemberDto login(MemberCommand memberCommand){
        boolean hasLoginHistory = userRepository.existsByStudentId(memberCommand.getSejongPortalId());
        SejongMemberInfo info = sejongPortalLoginService.getMemberAuthInfos(memberCommand.getSejongPortalId(), memberCommand.getSejongPortalPassword());

        // 로그인 성공 시 사용자 정보 저장 (첫 로그인인 경우)
        if (!hasLoginHistory) {
            User newUser = User.builder()
                    .major(info.getMajor())
                    .studentId(info.getStudentId())
                    .username(info.getName())
                    .grade(info.getGrade())
                    .completedSemester(info.getCompletedSemester())
                            .build();
            userRepository.save(newUser);
        }

        return MemberDto.builder()
                .major(info.getMajor())
                .studentIdString(info.getStudentId())
                .studentName(info.getName())
                .grade(info.getGrade())
                .completedSemester(info.getCompletedSemester())
                .hasLoginHistory(hasLoginHistory)
                .build();
    }

    public MemberDto getMemberAuthInfos(MemberCommand memberCommand) throws IOException {
        try {
            SejongMemberInfo info = sejongPortalLoginService.getMemberAuthInfos(memberCommand.getSejongPortalId(), memberCommand.getSejongPortalPassword());
            return MemberDto.builder()
                    .major(info.getMajor())
                    .studentIdString(info.getStudentId())
                    .studentName(info.getName())
                    .grade(info.getGrade())
                    .completedSemester(info.getCompletedSemester())
                    .build();
        } catch (Exception e) {
            log.error("세종대학교 포털 로그인 정보 가져오기 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.SEJONG_AUTH_DATA_FETCH_ERROR);
        }
    }


}


