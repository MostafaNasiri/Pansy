package io.github.mostafanasiri.pansy.app.presentation;

import io.github.mostafanasiri.pansy.app.common.exception.InternalErrorException;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class FileUtils {
    public MediaType getMediaType(@NonNull String fileExtension) {
        return switch (fileExtension) {
            case "jpeg", "jpg" -> MediaType.IMAGE_JPEG;

            case "png" -> MediaType.IMAGE_PNG;

            default -> throw new InternalErrorException("Invalid file extension");
        };
    }

    public String createFileUrl(@NonNull String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(fileName)
                .toUriString();
    }
}
