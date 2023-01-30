package ru.ianedw.avitoparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
public class AvitoParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(AvitoParserApplication.class, args);
	}

}
