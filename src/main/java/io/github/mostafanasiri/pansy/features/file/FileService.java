package io.github.mostafanasiri.pansy.features.file;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InternalErrorException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    public List<File> save(@NonNull MultipartFile[] files) {
        var fileEntities = new ArrayList<File>();

        Arrays.stream(files)
                .forEach(f -> {
                    String fileExtension = StringUtils.getFilenameExtension(f.getOriginalFilename());

                    if (!Arrays.asList(allowedFileExtensions).contains(fileExtension)) {
                        throw new InvalidInputException("File type is not allowed");
                    }

                    String fileName = generateFileName(fileExtension);

                    copyFile(f, fileName);
                    fileEntities.add(new File(fileName));
                });

        return repository.saveAll(fileEntities);
    }

    private void copyFile(@NonNull MultipartFile file, @NonNull String fileName) {
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

    public File getFile(int fileId) {
        return repository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(File.class, fileId));
    }

    /**
     * Returns all files with the given fileIds.
     *
     * @throws EntityNotFoundException if there's an invalid id in the given fileIds.
     */
    public List<File> getFiles(@NonNull Set<Integer> fileIds) {
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

        return result;
    }

    public List<Integer> getFileIdsThatAreAttachedToAnEntity(List<Integer> fileIds) {
        return repository.getFileIdsThatAreAttachedToAnEntity(fileIds);
    }
}
