package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class ConnectionController {
    private final ConnectionService connectionService;

    /**
     * restaurant에 해당하는 가장 최근 혼잡도 불러오기(혼잡도는 5분마다 갱신됨)
     * @param restaurant : 원하는 restaurant(제1학생회관, 제2학생회관, 제3학생회관, 상록회관, 생활과학대)
     */
    @GetMapping("/connection/{restaurant}")
    public ConnectionResponseDto congestionRequest(@PathVariable("restaurant") String restaurant) throws Exception {
        ConnectionVO recentConnection = connectionService.getRecentConnection(restaurant);
        if (recentConnection == null) {
            log.warn("최근 혼잡도 정보를 찾을 수 없습니다. restaurant={}", restaurant);
            return new ConnectionResponseDto();
        }
        return new ConnectionResponseDto(recentConnection);
    }

    @GetMapping("/connection/cache")
    public void cache() throws Exception {
        connectionService.saveRecentConnection();
    }

    @PostMapping("/connection/local")
    public ConnectionVO bridgeConnection(@RequestParam String AUTH_KEY,
                                         @RequestParam(name = "restaurant") String restaurantToString,
                                         HttpServletResponse response) throws Exception {
        boolean isRightSecretKey = connectionService.checkSecretKey(AUTH_KEY);

        if(!isRightSecretKey){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new IllegalArgumentException("Invalid AUTH_KEY: Unauthorized access");
        }

        String restaurantName = Restaurant.parseName(restaurantToString);
        Restaurant restaurant = Restaurant.valueOf(restaurantName);

        return connectionService.getRecentConnectionMap(restaurant);
    }
}