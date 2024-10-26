package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ConnectionController {
    private final ConnectionService connectionService;

    @GetMapping("/connection/{restaurant}")
    public ConnectionResponseDto congestionRequest(@PathVariable("restaurant") String restaurant) throws Exception {
        Connection recentConnection = connectionService.getRecentConnected(restaurant);
        return new ConnectionResponseDto(recentConnection, restaurant);
    }

    @GetMapping("/connection/cache")
    public void cache() throws Exception {
        connectionService.saveAndCacheConnection();
    }

    @PostMapping("/connection/test")
    public String bridgeConnection(@RequestParam String AUTH_KEY, HttpServletResponse response) throws Exception {
        boolean isRightSecretKey = connectionService.checkSecretKey(AUTH_KEY);

        if(!isRightSecretKey){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return "Invalid AUTH_KEY: Unauthorized access";
        }

        return connectionService.fetchConnectionInfoToString();
    }
}