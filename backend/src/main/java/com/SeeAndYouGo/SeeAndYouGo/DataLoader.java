package com.SeeAndYouGo.SeeAndYouGo;

import com.SeeAndYouGo.SeeAndYouGo.Connection.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final IterService iterService;
    private final ConnectionService connectionService;

    @Autowired
    public DataLoader(IterService iterService, ConnectionService connectionService) {
        this.iterService = iterService;
        this.connectionService = connectionService;
    }

    @Override
    public void run(String... args) throws Exception {
        iterService.weeklyIterative();
        connectionService.saveAndCacheConnection();
    }
}