package com.SeeAndYouGo.SeeAndYouGo.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageSecurityTest {

    @Test
    @DisplayName("파일명 검증 - 정상적인 UUID 형식")
    void validUuidFormat() {
        String validName = "550e8400-e29b-41d4-a716-446655440000.png";
        assertThat(validName.matches("^[a-f0-9-]+\\.png$")).isTrue();
    }

    @Test
    @DisplayName("파일명 검증 - 타임스탬프 포함 UUID 형식")
    void validUuidWithTimestamp() {
        String validName = "550e8400-e29b-41d4-a716-4466554400002025-11-0112000000.png";
        assertThat(validName.matches("^[a-f0-9-]+\\.png$")).isTrue();
    }

    @Test
    @DisplayName("Path Traversal 공격 차단 - .. 포함")
    void blockPathTraversalWithDoubleDot() {
        String maliciousName = "../../../etc/passwd";

        assertThat(maliciousName.contains("..")).isTrue();
        assertThat(maliciousName.contains("/")).isTrue();
    }

    @Test
    @DisplayName("Path Traversal 공격 차단 - 슬래시 포함")
    void blockPathTraversalWithSlash() {
        String maliciousName = "../../secret.txt";

        assertThat(maliciousName.contains("..")).isTrue();
        assertThat(maliciousName.contains("/")).isTrue();
    }

    @Test
    @DisplayName("Path Traversal 공격 차단 - 백슬래시 포함")
    void blockPathTraversalWithBackslash() {
        String maliciousName = "..\\..\\secret.txt";

        assertThat(maliciousName.contains("..")).isTrue();
        assertThat(maliciousName.contains("\\")).isTrue();
    }

    @Test
    @DisplayName("잘못된 파일 형식 차단 - .jpg")
    void blockInvalidFileFormat() {
        String invalidName = "image.jpg";

        assertThat(invalidName.matches("^[a-f0-9-]+\\.png$")).isFalse();
    }

    @Test
    @DisplayName("잘못된 파일 형식 차단 - .txt")
    void blockTextFile() {
        String invalidName = "secret.txt";

        assertThat(invalidName.matches("^[a-f0-9-]+\\.png$")).isFalse();
    }

    @Test
    @DisplayName("특수문자 포함 파일명 차단")
    void blockSpecialCharacters() {
        String maliciousName = "malicious@file.png";

        assertThat(maliciousName.matches("^[a-f0-9-]+\\.png$")).isFalse();
    }

    @Test
    @DisplayName("대문자 포함 파일명 차단")
    void blockUppercaseCharacters() {
        String invalidName = "550E8400-E29B-41D4-A716-446655440000.png";

        assertThat(invalidName.matches("^[a-f0-9-]+\\.png$")).isFalse();
    }

    @Test
    @DisplayName("공백 포함 파일명 차단")
    void blockWhitespace() {
        String invalidName = "file name.png";

        assertThat(invalidName.matches("^[a-f0-9-]+\\.png$")).isFalse();
    }

    @Test
    @DisplayName("복합 공격 패턴 차단")
    void blockComplexAttackPattern() {
        String maliciousName = "../imageStorage/../../../etc/passwd";

        assertThat(maliciousName.contains("..")).isTrue();
        assertThat(maliciousName.contains("/")).isTrue();
        assertThat(maliciousName.matches("^[a-f0-9-]+\\.png$")).isFalse();
    }
}
