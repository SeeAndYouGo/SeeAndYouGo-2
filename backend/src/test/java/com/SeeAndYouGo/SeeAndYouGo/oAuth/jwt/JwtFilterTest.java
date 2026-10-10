package com.SeeAndYouGo.SeeAndYouGo.oAuth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(tokenProvider, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void accessToken_setsAuthenticationFromTokenClaims() throws Exception {
        when(tokenProvider.resolveTokenStatus("admin-token")).thenReturn(TokenStatus.VALID);
        when(tokenProvider.getAuthentication("admin-token")).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        "admin@seeandyougo.com",
                        null,
                        Collections.singleton(new SimpleGrantedAuthority("ADMIN"))
                )
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer admin-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("admin@seeandyougo.com");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ADMIN");
    }

    @Test
    void expiredAccessToken_respondsWithAuth001() throws Exception {
        when(tokenProvider.resolveTokenStatus("expired-token")).thenReturn(TokenStatus.EXPIRED);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH_001");
    }

    @Test
    void invalidAccessToken_respondsWithAuth003() throws Exception {
        when(tokenProvider.resolveTokenStatus("forged-token")).thenReturn(TokenStatus.INVALID);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer forged-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH_003");
    }

    @Test
    void requestWithoutToken_setsGuestAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("none");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("GUEST");
    }

    @Test
    void expiredAccessToken_onPublicGet_passesAsGuest() throws Exception {
        when(tokenProvider.resolveTokenStatus("expired-token")).thenReturn(TokenStatus.EXPIRED);

        for (String uri : new String[]{"/api/connection/restaurant1", "/api/daily-menu/restaurant2",
                "/api/review/restaurant1", "/api/visitors/count", "/api/oauth/kakao"}) {
            SecurityContextHolder.clearContext();
            MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
            request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer expired-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            jwtFilter.doFilter(request, response, chain);

            assertThat(chain.getRequest()).as(uri).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).as(uri).isEqualTo("none");
            assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                    .extracting("authority")
                    .containsExactly("GUEST");
        }
    }

    @Test
    void expiredAccessToken_onLoginRequiredApi_respondsWithAuth001() throws Exception {
        when(tokenProvider.resolveTokenStatus("expired-token")).thenReturn(TokenStatus.EXPIRED);

        String[][] requests = {
                {"GET", "/api/user/nickname"},
                {"GET", "/api/reviews/expired-token"},
                {"GET", "/api/weekly-menu"},
                {"GET", "/api/dish/week"},
                {"POST", "/api/review"},
                {"POST", "/api/review/like/1"},
        };
        for (String[] r : requests) {
            MockHttpServletRequest request = new MockHttpServletRequest(r[0], r[1]);
            request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer expired-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            jwtFilter.doFilter(request, response, chain);

            assertThat(response.getStatus()).as(r[0] + " " + r[1]).isEqualTo(401);
            assertThat(response.getContentAsString()).as(r[0] + " " + r[1]).contains("AUTH_001");
            assertThat(chain.getRequest()).as(r[0] + " " + r[1]).isNull();
        }
    }

    @Test
    void invalidAccessToken_onPublicGet_respondsWithAuth003() throws Exception {
        when(tokenProvider.resolveTokenStatus("forged-token")).thenReturn(TokenStatus.INVALID);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/connection/restaurant1");
        request.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer forged-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("AUTH_003");
    }
}
