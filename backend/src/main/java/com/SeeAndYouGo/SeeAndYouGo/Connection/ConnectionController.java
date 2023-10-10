package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConnectionController {
    private final ConnectionService connectedService;

    @GetMapping("/connection/{restaurant}")
    public ResponseEntity<ConnectionResponse> congestionRequest(@PathVariable("restaurant") String place) throws Exception {

        ConnectionResponse connectionResponse = new ConnectionResponse();
        Connection recentConnection = connectedService.getRecentConnected(place);
        String name = recentConnection.getName();

        // 이 부분도 Capacity를 활용하게 하자!
        if(name.contains("1")) connectionResponse.setCapacity(486);
        else if(name.contains("2")) connectionResponse.setCapacity(392);
        else if(name.contains("3")) connectionResponse.setCapacity(273);
        else if(name.contains("상록")) connectionResponse.setCapacity(194);
        else if(name.contains("생활")) connectionResponse.setCapacity(170);

        connectionResponse.setConnected(recentConnection.getConnected());
        connectionResponse.setDateTime(recentConnection.getTime());
        return ResponseEntity.ok(connectionResponse);
    }
}
