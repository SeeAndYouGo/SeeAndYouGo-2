package com.SeeAndYouGo.SeeAndYouGo.Keyword.dto;

import com.SeeAndYouGo.SeeAndYouGo.Keyword.Keyword;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class KeywordResponseDto {
    private List<String> keywords;

    @Builder
    public KeywordResponseDto(List<String> keywords) {
        this.keywords = keywords;
    }

    public static KeywordResponseDto toDTO(List<Keyword> keywords) {
        return KeywordResponseDto.builder()
                .keywords(keywords.stream().map(x -> x.getName()).collect(Collectors.toList()))
                .build();
    }
}