package com.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AiProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiProjectApplication.class, args);
	}
	
    @Bean //  This annotation tells Spring to create a RestTemplate bean
    public RestTemplate restTemplate() {
        return new RestTemplate(); //  Create and return a new RestTemplate instance
    }

}
