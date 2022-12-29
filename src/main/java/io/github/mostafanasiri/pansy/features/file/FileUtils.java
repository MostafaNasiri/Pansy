package io.github.mostafanasiri.pansy.features.file;

import io.github.mostafanasiri.pansy.common.exception.InternalErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class FileUtils {
    @Autowired
    private FileServiceConfig config;

    public MediaType getMediaType(String fileExtension) {
        switch (fileExtension) {
            case "jpeg":
            case "jpg":
                return MediaType.IMAGE_JPEG;

            case "png":
                return MediaType.IMAGE_PNG;

            default:
                throw new InternalErrorException("Invalid file extension.");
        }
    }

    public String createFileUrl(File file) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(file.getName())
                .toUriString();
    }
}
