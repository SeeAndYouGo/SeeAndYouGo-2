package com.SeeAndYouGo.SeeAndYouGo.Keyword.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KeywordRequestDto {
    private String keyword;
    private String user_id;
}
