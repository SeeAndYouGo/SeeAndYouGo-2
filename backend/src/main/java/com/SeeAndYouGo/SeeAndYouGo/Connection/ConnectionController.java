package com.SeeAndYouGo.SeeAndYouGo.Connection;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class ConnectionController {
    private final ConnectionService connectedService;

    @GetMapping("/connection/{restaurant}")
    public ResponseEntity<ConnectionResponseDto> congestionRequest(@PathVariable("restaurant") String place) throws Exception {
        Connection recentConnection = connectedService.getRecentConnected(place);
        return ResponseEntity.ok(new ConnectionResponseDto(recentConnection, place));
    }

    @GetMapping("/connection/cache")
    public void cache() throws Exception {
        connectedService.saveAndCacheConnection();
    }
}
