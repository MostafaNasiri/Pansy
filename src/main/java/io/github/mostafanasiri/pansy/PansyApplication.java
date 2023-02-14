package io.github.mostafanasiri.pansy;

import io.github.mostafanasiri.pansy.app.domain.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
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
