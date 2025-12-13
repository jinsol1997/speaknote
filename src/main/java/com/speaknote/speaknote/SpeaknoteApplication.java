package com.speaknote.speaknote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpeaknoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeaknoteApplication.class, args);
	}

}
