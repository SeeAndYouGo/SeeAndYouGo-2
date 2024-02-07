package com.SeeAndYouGo.SeeAndYouGo.Keyword.dto;

import com.SeeAndYouGo.SeeAndYouGo.Keyword.Keyword;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class KeywordAddResponseDto {
    private List<String> keywords;

    private Boolean isExceed;

    @Builder
    public KeywordAddResponseDto(List<String> keywords, boolean isExceed) {
        this.keywords = keywords;
        this.isExceed = isExceed;
    }

    public static KeywordAddResponseDto toDTO(List<Keyword> keywords) {
        return KeywordAddResponseDto.builder()
                .keywords(keywords.stream().map(Keyword::getName).collect(Collectors.toList()))
                .build();
    }

    public static KeywordAddResponseDto toDTO(List<Keyword> keywords, boolean isExceed) {
        return KeywordAddResponseDto.builder()
                .keywords(keywords.stream().map(Keyword::getName).collect(Collectors.toList()))
                .isExceed(isExceed)
                .build();
    }
}