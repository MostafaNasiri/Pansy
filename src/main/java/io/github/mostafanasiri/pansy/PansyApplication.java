package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.features.file.domain.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PansyApplication implements ApplicationRunner {
	@Autowired
	private FileService fileService;

	public static void main(String[] args) {
		SpringApplication.run(PansyApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		fileService.init();
	}
}
