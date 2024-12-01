package com.SeeAndYouGo.SeeAndYouGo.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class VisitorCountDto {
    private final String visitTotal;
    private final String visitToday;
}