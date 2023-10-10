package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionRepository connectionRepository;

    public Connection getRecentConnected(String restaurantName){
        String changeRestaurantName = changeRestaurantName(restaurantName);
        Connection result = connectionRepository.findRecent(changeRestaurantName);

        return result;
    }

    public String changeRestaurantName(String name){
        String res = name;
        if(name.contains("1")) res= "1학생회관";
        else if(name.contains("2")) res= "2학생회관";
        else if(name.contains("3")) res= "3학생회관";
        else if(name.contains("4")) res= "상록회관";
        else if(name.contains("5")) res= "생활과학대";
        return res;
    }
}