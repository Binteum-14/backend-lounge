package com.ddcj.binteum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BinteumApplication {

	public static void main(String[] args) {
		SpringApplication.run(BinteumApplication.class, args);
	}

}
