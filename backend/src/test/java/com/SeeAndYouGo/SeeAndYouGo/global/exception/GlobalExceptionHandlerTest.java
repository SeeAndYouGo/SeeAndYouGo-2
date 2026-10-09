package com.SeeAndYouGo.SeeAndYouGo.global.exception;

import com.SeeAndYouGo.SeeAndYouGo.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(handler)
            .build();

    @Test
    void responseStatusException_preservesForbiddenStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/weekly-menu");
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");

        ResponseEntity<ApiResponse<Void>> response =
                handler.handleResponseStatusException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("AUTH_002");
        assertThat(response.getBody().getMessage()).isEqualTo("접근 권한이 없습니다.");
    }

    @Test
    void missingRequestHeader_returnsBadRequest() throws Exception {
        expectInvalidInput(get("/test/header"));
    }

    @Test
    void missingRequestPart_returnsBadRequest() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "a.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1});
        expectInvalidInput(multipart("/test/part").file(image));
    }

    @Test
    void notMultipartRequest_returnsBadRequest() throws Exception {
        expectInvalidInput(post("/test/part").contentType(MediaType.APPLICATION_JSON).content("{}"));
    }

    @Test
    void methodNotSupported_returnsBadRequest() throws Exception {
        expectInvalidInput(post("/test/get-only"));
    }

    @Test
    void mediaTypeNotSupported_returnsBadRequest() throws Exception {
        expectInvalidInput(put("/test/json").contentType(MediaType.TEXT_PLAIN).content("x"));
    }

    private void expectInvalidInput(RequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON_002"))
                .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/header")
        String header(@RequestHeader("RefreshToken") String refreshToken) {
            return refreshToken;
        }

        @PostMapping("/test/part")
        String part(@RequestPart("dto") String dto) {
            return dto;
        }

        @GetMapping("/test/get-only")
        String getOnly() {
            return "ok";
        }

        @PutMapping(value = "/test/json", consumes = MediaType.APPLICATION_JSON_VALUE)
        String json(@RequestBody Map<String, Object> body) {
            return "ok";
        }
    }
}
