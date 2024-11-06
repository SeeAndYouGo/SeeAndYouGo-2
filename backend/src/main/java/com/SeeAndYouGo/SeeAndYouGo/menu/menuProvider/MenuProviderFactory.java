package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuProviderFactory {
    private final JsonMenuProvider jsonMenuProvider;
    private final ApiMenuProvider apiMenuProvider;
    private final CrawlingMenuProvider crawlingMenuProvider;

    public MenuProvider createMenuProvider(Restaurant restaurant) {
        switch (restaurant) {
            case 제1학생회관:
                return jsonMenuProvider;
            case 제2학생회관:
            case 제3학생회관:
            case 상록회관:
            case 생활과학대:
                return apiMenuProvider;
            case 학생생활관:
                return crawlingMenuProvider;
            default:
                throw new IllegalArgumentException("Unknown restaurant: " + restaurant);
        }
    }
}
