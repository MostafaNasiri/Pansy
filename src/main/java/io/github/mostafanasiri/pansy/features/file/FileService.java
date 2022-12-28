package io.github.mostafanasiri.pansy.features.file;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InternalErrorException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;

@Service
public class FileService {
    @Autowired
    private FileRepository repository;

    private final String[] allowedFileExtensions = new String[]{
            "jpg",
            "jpeg",
            "png"
    };
    private final Path filesLocation;

    public FileService() {
        filesLocation = Paths.get("uploads");
    }

    public void init() {
        try {
            Files.createDirectories(filesLocation);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create 'uploads' folder.");
        }
    }

    public File save(MultipartFile file) {
        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        if (!Arrays.asList(allowedFileExtensions).contains(fileExtension)) {
            throw new InvalidInputException("File type is not allowed.");
        }

        String fileName = String.format(
                "file-%s.%s",
                System.currentTimeMillis(),
                fileExtension
        );

        copyFile(file, fileName);

        return repository.save(new File(fileName));
    }

    private void copyFile(MultipartFile file, String fileName) {
        try {
            Path destinationFile = filesLocation.resolve(fileName);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    public Resource getFile(String fileName) {
        try {
            Path file = filesLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new InternalErrorException("Could not read the file.");
            }
        } catch (InvalidPathException e) {
            throw new InvalidInputException("Invalid file name.");
        } catch (MalformedURLException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    public File getFile(int fileId) {
        return repository.findById(fileId)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException(File.class, fileId);
                });
    }
}
