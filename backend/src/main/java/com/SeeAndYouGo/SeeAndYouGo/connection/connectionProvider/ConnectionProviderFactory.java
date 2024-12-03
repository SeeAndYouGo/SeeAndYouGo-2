package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectionProviderFactory {

    private final DormitoryConnectionProvider dormitory;
    private final InformationCenterConnectionProvider informationCenter;

    public ConnectionProvider getConnectionProvider(Restaurant restaurant) {
        switch (restaurant) {
            case 제1학생회관:
            case 제2학생회관:
            case 제3학생회관:
            case 상록회관:
            case 생활과학대:
                return informationCenter;
            case 학생생활관:
                return dormitory;
            default:
                throw new IllegalArgumentException("Unknown restaurant: " + restaurant);
        }
    }
}
