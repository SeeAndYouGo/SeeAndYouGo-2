package com.SeeAndYouGo.SeeAndYouGo.oAuth;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.ApiException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * 카카오/구글 토큰 엔드포인트 역할을 하는 로컬 HTTP 서버로 OAuthHttpClient 의 응답 상태 처리를 검증한다.
 */
@DisplayName("OAuthHttpClient - 소셜 로그인 제공자 응답 처리")
class OAuthHttpClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("정상 응답이면 본문을 반환한다")
    void returnsBodyOnSuccess() throws IOException {
        String url = serve(200, "{\"access_token\":\"abc\"}");

        String body = OAuthHttpClient.postForAccessToken(url, "code=ok");

        assertThat(body).isEqualTo("{\"access_token\":\"abc\"}");
    }

    @Test
    @DisplayName("인가 코드 만료·재사용(invalid_grant)이면 400 COMMON_002 로 다시 시도를 안내한다")
    void invalidGrantBecomesBadRequest() throws IOException {
        String url = serve(400, "{\"error\":\"invalid_grant\",\"error_description\":\"authorization code not found\",\"error_code\":\"KOE320\"}");

        ApiException e = catchThrowableOfType(() -> OAuthHttpClient.postForAccessToken(url, "code=used"), ApiException.class);

        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        assertThat(e.getMessage()).isEqualTo("로그인에 실패했습니다. 다시 시도해주세요.");
    }

    @Test
    @DisplayName("앱 키·redirect URI 설정 오류 등 그 외 4xx 는 제공자 응답을 담아 서버 오류로 남긴다")
    void otherClientErrorStaysServerError() throws IOException {
        String url = serve(401, "{\"error\":\"invalid_client\",\"error_code\":\"KOE010\"}");

        assertThatThrownBy(() -> OAuthHttpClient.postForAccessToken(url, "code=x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("invalid_client");
    }

    @Test
    @DisplayName("사용자 정보 조회 실패도 서버 오류로 남긴다")
    void userInfoFailureStaysServerError() throws IOException {
        String url = serve(500, "internal error");

        assertThatThrownBy(() -> OAuthHttpClient.getWithBearer(url, "token"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("500");
    }

    private String serve(int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/oauth/token";
    }
}
