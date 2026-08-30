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
}
