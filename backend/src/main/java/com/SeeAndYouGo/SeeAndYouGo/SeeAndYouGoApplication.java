package com.SeeAndYouGo.SeeAndYouGo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeeAndYouGoApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SeeAndYouGoApplication.class);
		ApplicationContext context = app.run(SeeAndYouGoApplication.class, args);
		DataLoader dataLoader = context.getBean(DataLoader.class);
		dataLoader.run();
	}
}
