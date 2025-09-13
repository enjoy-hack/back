package com.example.enjoy.service.loginService;

import com.example.enjoy.dto.loginDto.MemberCommand;
import com.example.enjoy.dto.loginDto.SejongMemberInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SejongPortalLoginService {

    public SejongMemberInfo getMemberAuthInfos(MemberCommand memberCommand) {

        // 실제 포털 인증
        try {
            // OkHttpClient 생성
            OkHttpClient client = buildClient();

            // 포털 로그인 요청
            doPortalLogin(client, memberCommand);

            // SSO 리다이렉트 -> 고전독서인증 사이트
            String ssoUrl = "http://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https://classic.sejong.ac.kr/classic/index.do";
            Request ssoReq = new Request.Builder().url(ssoUrl).get().build();
            try (Response ssoResp = client.newCall(ssoReq).execute()) {
                if (!ssoResp.isSuccessful()) {
                    throw new RuntimeException("Connection Eroor");
                }
            }

            // 고전독서인증현황 페이지 GET
            String html = fetchReadingStatusHtml(client);

            // JSoup 파싱 -> MemberDto
            return parseHTMLAndGetMemberInfo(html);

        } catch (IOException e) {
            log.error("세종포털 인증 중 IOException: {}", e.getMessage());
            throw new RuntimeException("Connection Eroor");
        }
    }

    /**
     * 세종포털에 ID/PW로 로그인 (POST)
     */
    private void doPortalLogin(OkHttpClient client, MemberCommand memberCommand) throws IOException {
        String loginUrl = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";

        String studentId = memberCommand.getSejongPortalId();
        String password = memberCommand.getSejongPortalPassword();

        // POST form data
        RequestBody formBody = new FormBody.Builder()
                .add("mainLogin", "N")
                .add("rtUrl", "library.sejong.ac.kr")
                .add("id", studentId)
                .add("password", password)
                .build();

        // 요청 객체 생성
        Request request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .header("Host", "portal.sejong.ac.kr")
                .header("Referer", "https://portal.sejong.ac.kr")
                .header("Cookie", "chknos=false")
                .build();

        // 실제 요청 (재시도 로직 포함)
        Response response = executeWithRetry(client, request);

        // 응답 바디를 한 번 읽어주고 닫음 (로그인 결과 페이지)
        String body = response.body() != null ? response.body().string() : "";
        response.close();
    }

    /**
     * 고전독서인증현황 페이지 HTML 반환
     * @param client OkHttpClient
     * @return status.do 페이지의 HTML 문자열
     */
    private String fetchReadingStatusHtml(OkHttpClient client) throws IOException {
        String finalUrl = "https://classic.sejong.ac.kr/classic/reading/status.do";

        Request finalReq = new Request.Builder()
                .url(finalUrl)
                .get()
                .build();

        // GET 요청
        try (Response finalResp = client.newCall(finalReq).execute()) {
            if (finalResp.body() == null || finalResp.code() != 200) {
                throw new RuntimeException("SEJONG_AUTH_DATA_FETCH_ERROR");
            }
            return finalResp.body().string();
        }
    }


    /**
     * 요청 실행 시 예외 발생 시
     * 최대 3회 재시도
     */
    private Response executeWithRetry(OkHttpClient client, Request request) throws IOException {
        Response response = null;
        int tryCount = 0;
        while (tryCount < 3) {
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (SocketTimeoutException e) {
                tryCount++;
                log.warn("[PortalLogin] Timeout 발생 -> 재시도... ({}회)", tryCount);
            }
        }
        throw new RuntimeException("세종대 API 연결 오류");
    }

    /**
     * 고전독서인증현황 페이지 (status.do) 파싱
     * 학과명, 학번, 이름, 학년, 사용자상태 추출
     *
     * @param html 고전독서인증현황 페이지 HTML
     * @return MemberDto
     */
    private SejongMemberInfo parseHTMLAndGetMemberInfo(String html) {
        Document doc = Jsoup.parse(html);

        // "사용자 정보" 테이블 tr 추출
        String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        String major = getValueFromList(rowValues, 0); // 학과명
        String studentId = getValueFromList(rowValues, 1); // 학번
        String studentName = getValueFromList(rowValues, 2); // 이름
        String year = getValueFromList(rowValues, 3); // 학년
        String status = getValueFromList(rowValues, 4); // 학생 상태
        String completedSemester = getValueFromList(rowValues, 5); // 이수 학기

        return SejongMemberInfo.builder()
                .major(major)
                .studentId(studentId)
                .name(studentName)
                .grade(year)
                .status(status)
                .completedSemester(completedSemester)
                .build();
    }

    /**
     * List index 범위 체크 후 값 꺼내기
     */
    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }

    private OkHttpClient buildClient() {
        try {
            // SSLContext 생성, 모든 인증서 신뢰 설정
            SSLContext sslCtx = SSLContext.getInstance("SSL");
            sslCtx.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
            SSLSocketFactory sslFactory = sslCtx.getSocketFactory();

            // hostnameVerifier: 모든 호스트네임에 대해 OK 처리
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            // 쿠키 관리
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // OkHttpClient 생성
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslFactory, trustAllManager())
                    .hostnameVerifier(hostnameVerifier)
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 모든 서버 인증서를 신뢰하는 X509TrustManager 구현
     */
    private X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }



}
