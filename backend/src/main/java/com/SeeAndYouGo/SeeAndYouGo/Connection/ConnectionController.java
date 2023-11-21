package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ConnectionController {
    private final ConnectionService connectedService;

    @GetMapping("/connection/{restaurant}")
    public ResponseEntity<ConnectionResponse> congestionRequest(@PathVariable("restaurant") String place) throws Exception {
        Connection recentConnection = connectedService.getRecentConnected(place);

        ConnectionResponse connectionResponse = new ConnectionResponse();
        connectionResponse.setRestaurantName(recentConnection.getRestaurant().getName());
        connectionResponse.setCapacity(recentConnection.getRestaurant().getCapacity());
        connectionResponse.setConnected(recentConnection.getConnected());
        connectionResponse.setDateTime(recentConnection.getTime());
        return ResponseEntity.ok(connectionResponse);
    }

    @GetMapping("/connection/cache")
    public void cache() throws Exception {
        connectedService.saveAndCacheConnection();
    }
}
