package com.SeeAndYouGo.SeeAndYouGo.menu.menuProvider;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

@Component
public class MenuProviderFactory {
    private final JsonMenuProvider jsonMenuProvider;
    private final ApiMenuProvider apiMenuProvider;
    private final CrawlingMenuProvider crawlingMenuProvider;

    private final Map<MenuProviderType, MenuProvider> providerMap = new EnumMap<>(MenuProviderType.class);

    public MenuProviderFactory(JsonMenuProvider jsonMenuProvider,
                               ApiMenuProvider apiMenuProvider,
                               CrawlingMenuProvider crawlingMenuProvider) {
        this.jsonMenuProvider = jsonMenuProvider;
        this.apiMenuProvider = apiMenuProvider;
        this.crawlingMenuProvider = crawlingMenuProvider;
    }

    @PostConstruct
    public void init() {
        providerMap.put(MenuProviderType.JSON, jsonMenuProvider);
        providerMap.put(MenuProviderType.API, apiMenuProvider);
        providerMap.put(MenuProviderType.CRAWLING, crawlingMenuProvider);
    }

    public MenuProvider createMenuProvider(Restaurant restaurant) {
        MenuProvider provider = providerMap.get(restaurant.getMenuProviderType());
        if (provider == null) {
            throw new IllegalArgumentException("No MenuProvider for type: " + restaurant.getMenuProviderType());
        }
        return provider;
    }
}
