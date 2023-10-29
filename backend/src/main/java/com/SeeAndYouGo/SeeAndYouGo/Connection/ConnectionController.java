package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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
    public void cash() throws Exception {
        connectedService.saveAndCashConnection();
    }
}
