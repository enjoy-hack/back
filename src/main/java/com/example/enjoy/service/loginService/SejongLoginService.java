package com.example.enjoy.service.loginService;


import com.example.enjoy.dto.loginDto.MemberCommand;
import com.example.enjoy.dto.loginDto.MemberDto;
import com.example.enjoy.exception.CustomException;
import com.example.enjoy.exception.ErrorCode;
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

    /**
     * 세종포털 인증을 통해 사용자 정보를 가져옵니다
     */
    public MemberDto getMemberAuthInfos(MemberCommand command) {
        String sejongPortalId = command.getSejongPortalId();
        String sejongPortalPw = command.getSejongPortalPassword();

        try {
            // OkHttpClient 생성
            OkHttpClient client = buildClient();

            // 포털 로그인 요청
            doPortalLogin(client, sejongPortalId, sejongPortalPw);

            // 포털 -> 고전독서 SSO 시작점 접근
            String portalClassicLinkUrl = "https://portal.sejong.ac.kr/html/classic/classic.html";
            Request portalReq = new Request.Builder()
                    .url(portalClassicLinkUrl)
                    .get()
                    .build();
            try (Response portalResp = executeWithRetry(client, portalReq)) {
                log.debug("포털 링크 응답: {}", portalResp.code());
            }

            // SSO 리다이렉트 처리
            String ssoUrl = "https://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https://classic.sejong.ac.kr/classic/index.do";
            Request ssoReq = new Request.Builder()
                    .url(ssoUrl)
                    .get()
                    .header("Referer", "https://portal.sejong.ac.kr/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response ssoResp = executeWithRetry(client, ssoReq)) {
                log.debug("SSO 응답: {} - 쿠키 수: {}",
                        ssoResp.code(),
                        client.cookieJar().loadForRequest(HttpUrl.get(ssoUrl)).size());

                if (!ssoResp.isSuccessful()) {
                    throw new CustomException(ErrorCode.SEJONG_AUTH_CONNECTION_ERROR, "SSO 리다이렉트 실패");
                }
            }

            // 메인 페이지 방문 (중요: 세션 쿠키 설정을 위해)
            String mainUrl = "https://classic.sejong.ac.kr/classic/index.do";
            Request mainReq = new Request.Builder()
                    .url(mainUrl)
                    .get()
                    .header("Referer", ssoUrl)
                    .build();

            try (Response mainResp = executeWithRetry(client, mainReq)) {
                log.debug("메인 페이지 응답: {}", mainResp.code());
                if (!mainResp.isSuccessful()) {
                    throw new CustomException(ErrorCode.SEJONG_AUTH_CONNECTION_ERROR, "메인 페이지 접근 실패");
                }
            }

            // 고전독서인증현황 페이지 GET
            String html = fetchReadingStatusHtml(client);

            // HTML 파싱 및 정보 추출
            return parseHTMLAndGetMemberInfo(html);

        } catch (IOException e) {
            log.error("포털 인증 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.SEJONG_AUTH_CONNECTION_ERROR, "포털 인증 중 오류 발생");
        }
    }


    /**
     * 세종포털에 ID/PW로 로그인
     */
    private void doPortalLogin(OkHttpClient client, String studentId, String password) throws IOException {
        String loginUrl = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";

        RequestBody formBody = new FormBody.Builder()
                .add("mainLogin", "N")
                .add("rtUrl", "")  // 빈 값으로 변경하여 리디렉션 동작 확인
                .add("id", studentId)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .header("Host", "portal.sejong.ac.kr")
                .header("Origin", "https://portal.sejong.ac.kr")
                .header("Referer", "https://portal.sejong.ac.kr/jsp/login/login.jsp")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = executeWithRetry(client, request)) {
            log.debug("포털 로그인 응답: {}", response.code());
            String responseBody = response.body() != null ? response.body().string() : "";

            if (responseBody.contains("alert") && responseBody.contains("로그인")) {
                throw new CustomException(ErrorCode.SEJONG_AUTH_CREDENTIALS_INVALID, "포털 로그인 실패");
            }
        }
    }

    /**
     * 고전독서인증현황 페이지 HTML 가져오기
     */
    private String fetchReadingStatusHtml(OkHttpClient client) throws IOException {
        String statusUrl = "https://classic.sejong.ac.kr/classic/reading/status.do";

        // 요청 전 쿠키 상태 확인
        logCookies(client, statusUrl);

        Request request = new Request.Builder()
                .url(statusUrl)
                .get()
                .header("Referer", "https://classic.sejong.ac.kr/classic/index.do")
                .build();

        try (Response response = client.newCall(request).execute()) {
            log.debug("고전독서인증현황 응답: {}", response.code());

            if (response.code() != 200 || response.body() == null) {
                throw new CustomException(ErrorCode.SEJONG_AUTH_DATA_FETCH_ERROR,
                        "고전독서인증현황 페이지 조회 실패: " + response);
            }

            return response.body().string();
        }
    }

    /**
     * 디버깅용: 쿠키 정보 출력
     */
    private void logCookies(OkHttpClient client, String url) {
        List<Cookie> cookies = client.cookieJar().loadForRequest(HttpUrl.get(url));
        log.debug("URL {} 쿠키 정보 ({}):", url, cookies.size());
        for (Cookie cookie : cookies) {
            log.debug(" - {}: {}", cookie.name(), cookie.value());
        }
    }

    /**
     * 고전독서인증현황 페이지 파싱
     */
    private MemberDto parseHTMLAndGetMemberInfo(String html) {
        Document doc = Jsoup.parse(html);

        String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
        List<String> rowValues = new ArrayList<>();

        doc.select(selector).forEach(tr -> {
            String value = tr.select("td").text().trim();
            rowValues.add(value);
        });

        String major = getValueFromList(rowValues, 0);
        String studentId = getValueFromList(rowValues, 1);
        String studentName = getValueFromList(rowValues, 2);
        String year = getValueFromList(rowValues, 3);
        String status = getValueFromList(rowValues, 4);

        return MemberDto.builder()
                .major(major)
                .studentIdString(studentId)
                .studentName(studentName)
                .academicYear(year)
                .enrollmentStatus(status)
                .build();
    }

    /**
     * List에서 안전하게 값 가져오기
     */
    private String getValueFromList(List<String> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }

    /**
     * 재시도 로직이 포함된 요청 실행
     */
    private Response executeWithRetry(OkHttpClient client, Request request) throws IOException {
        int tryCount = 0;
        int maxRetries = 3;

        while (tryCount < maxRetries) {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() || tryCount == maxRetries - 1) {
                    return response;
                }
                response.close();
            } catch (SocketTimeoutException e) {
                if (tryCount == maxRetries - 1) {
                    throw e;
                }
            }
            tryCount++;
            log.debug("요청 재시도 ({}/{}): {}", tryCount, maxRetries, request.url());
        }
        throw new IOException("최대 재시도 횟수 초과: " + request.url());
    }

    private OkHttpClient buildClient() {
        try {
            // SSLContext 생성, 모든 인증서 신뢰 설정
            SSLContext sslCtx = SSLContext.getInstance("SSL");
            sslCtx.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
            SSLSocketFactory sslFactory = sslCtx.getSocketFactory();

            // hostnameVerifier: 모든 호스트네임에 대해 OK 처리
            HostnameVerifier hostnameVerifier = (hostname, session) -> true;

            // OkHttp 로깅 인터셉터
//      HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::info);
//      logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 쿠키 관리
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            // OkHttpClient 생성
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslFactory, trustAllManager())
                    .hostnameVerifier(hostnameVerifier)
                    .cookieJar(new JavaNetCookieJar(cookieManager))
//          .addInterceptor(logging)
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
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }
}


