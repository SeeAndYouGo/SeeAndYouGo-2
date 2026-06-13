package com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ConnectionProviderFactory {

    private final DormitoryConnectionProvider dormitoryProvider;
    private final InformationCenterConnectionProvider infoCenterProvider;

    private final Map<ConnectionProviderType, ConnectionProvider> providerMap = new EnumMap<>(ConnectionProviderType.class);

    public ConnectionProviderFactory(DormitoryConnectionProvider dormitoryProvider,
                                     InformationCenterConnectionProvider infoCenterProvider) {
        this.dormitoryProvider = dormitoryProvider;
        this.infoCenterProvider = infoCenterProvider;
    }

    @PostConstruct
    public void init() {
        providerMap.put(ConnectionProviderType.DORMITORY, dormitoryProvider);
        providerMap.put(ConnectionProviderType.INFO_CENTER, infoCenterProvider);
    }

    public ConnectionProvider getConnectionProvider(Restaurant restaurant) {
        ConnectionProvider provider = providerMap.get(restaurant.getConnectionProviderType());
        if (provider == null) {
            throw new IllegalArgumentException("No ConnectionProvider for type: " + restaurant.getConnectionProviderType());
        }
        return provider;
    }
}
