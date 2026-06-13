package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionResponseDto;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.PredictionResponseDto;
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
    private final PredictionService predictionService;

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

    /**
     * 외부 예측 서버에서 받아온 혼잡도 예측 결과를 그대로 릴레이한다.
     * 호출 전 예측 서버 헬스체크를 수행한다.
     * @param restaurant 식당 식별자 (예: restaurant1, 제1학생회관)
     * @param observedAt 관측 시각 (yyyy-MM-dd HH:mm:ss)
     */
    @GetMapping("/connection/prediction")
    public PredictionResponseDto predictConnection(@RequestParam("restaurant") String restaurant,
                                                   @RequestParam("observed_at") String observedAt) {
        return predictionService.predict(restaurant, observedAt);
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