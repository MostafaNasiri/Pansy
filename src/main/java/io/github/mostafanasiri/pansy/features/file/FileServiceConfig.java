package io.github.mostafanasiri.pansy.features.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration("application")
@ConfigurationProperties("uploader")
public class FileServiceConfig {
    private String folder;
}
