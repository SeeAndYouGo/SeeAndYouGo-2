package com.SeeAndYouGo.SeeAndYouGo.Connection;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ConnectionController {
    private final ConnectionService connectionService;

    @GetMapping("/connection/{restaurant}")
    public ResponseEntity<ConnectionResponseDto> congestionRequest(@PathVariable("restaurant") String restaurant) throws Exception {
        Connection recentConnection = connectionService.getRecentConnected(restaurant);
        return ResponseEntity.ok(new ConnectionResponseDto(recentConnection, restaurant));
    }

    @GetMapping("/connection/cache")
    public void cache() throws Exception {
        connectionService.saveAndCacheConnection();
    }

    @PostMapping("/connection/test")
    public String bridgeConnection() throws Exception {
        return connectionService.fetchConnectionInfoToString();
    }
}
