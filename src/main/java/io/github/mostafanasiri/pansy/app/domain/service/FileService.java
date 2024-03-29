package io.github.mostafanasiri.pansy.app.domain.service;

import io.github.mostafanasiri.pansy.app.common.exception.InternalErrorException;
import io.github.mostafanasiri.pansy.app.data.entity.jpa.FileEntity;
import io.github.mostafanasiri.pansy.app.data.repository.jpa.FileJpaRepository;
import io.github.mostafanasiri.pansy.app.domain.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.app.domain.exception.InvalidInputException;
import io.github.mostafanasiri.pansy.app.domain.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class FileService {
    @Autowired
    private FileJpaRepository repository;

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
            throw new RuntimeException("Couldn't create 'uploads' folder");
        }
    }

    private static String generateFileName(String fileExtension) {
        return String.format(
                "file-%s.%s",
                System.currentTimeMillis(),
                fileExtension
        );
    }

    public @NonNull List<File> save(@NonNull MultipartFile[] files) {
        var fileEntities = Arrays.stream(files)
                .map(f -> {
                    String fileExtension = StringUtils.getFilenameExtension(f.getOriginalFilename());

                    if (!Arrays.asList(allowedFileExtensions).contains(fileExtension)) {
                        throw new InvalidInputException("File type is not allowed");
                    }

                    String fileName = generateFileName(fileExtension);
                    copyFileToUploadsFolder(f, fileName);

                    return new FileEntity(fileName);
                })
                .toList();

        var savedFileEntities = repository.saveAll(fileEntities);

        return savedFileEntities.stream()
                .map(fileEntity -> new File(fileEntity.getId(), fileEntity.getName()))
                .toList();
    }

    private void copyFileToUploadsFolder(@NonNull MultipartFile file, @NonNull String fileName) {
        try {
            Path destinationFile = filesLocation.resolve(fileName);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    public Resource getFileResource(@NonNull String fileName) {
        try {
            Path file = filesLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new InternalErrorException("Could not read the file");
            }
        } catch (InvalidPathException e) {
            throw new InvalidInputException("Invalid file name");
        } catch (MalformedURLException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * @throws EntityNotFoundException if at least one of the given file ids does not exist.
     */
    public void checkIfFilesExist(@NonNull Set<Integer> fileIds) {
        var result = repository.findAllById(fileIds);

        if (result.size() < fileIds.size()) {
            // At least one of the required files was not found
            fileIds.forEach(id -> {
                result.stream()
                        .filter(f -> f.getId() == id)
                        .findAny()
                        .orElseThrow(() -> new EntityNotFoundException(File.class, id));
            });
        }
    }

    /**
     * @throws InvalidInputException if at least one of the given file ids is already attached to an entity.
     */
    public void checkIfFilesAreAlreadyAttachedToAnEntity(@NonNull List<Integer> fileIds) {
        var result = repository.getFileIdsThatAreAttachedToAnEntity(fileIds);

        if (!result.isEmpty()) {
            throw new InvalidInputException(
                    String.format(
                            "File with id %s is already attached to an entity",
                            result.get(0)
                    )
            );
        }
    }
}
