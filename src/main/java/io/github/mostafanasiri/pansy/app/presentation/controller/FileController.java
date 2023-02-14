package io.github.mostafanasiri.pansy.app.presentation.controller;

import io.github.mostafanasiri.pansy.app.common.ApiResponse;
import io.github.mostafanasiri.pansy.app.domain.service.FileService;
import io.github.mostafanasiri.pansy.app.presentation.FileUtils;
import io.github.mostafanasiri.pansy.app.presentation.response.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "File")
@RestController
public class FileController {
    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private FileService service;

    @PostMapping("/files")
    @Operation(summary = "Uploads a list of files")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> save(
            @RequestParam(name = "files[]") MultipartFile[] files
    ) {
        var uploadedFiles = service.save(files);
        var result = uploadedFiles.stream()
                .map(f -> new FileUploadResponse(f.id(), fileUtils.createFileUrl(f.name())))
                .toList();

        return new ResponseEntity<>(new ApiResponse<>(ApiResponse.Status.SUCCESS, result), HttpStatus.CREATED);
    }

    @GetMapping("/files/{file_name}")
    @Operation(summary = "Returns a file")
    public ResponseEntity<Resource> get(@PathVariable(name = "file_name") String fileName) {
        var file = service.getFileResource(fileName);
        var fileExtension = StringUtils.getFilenameExtension(fileName);

        return ResponseEntity.ok()
                .contentType(fileUtils.getMediaType(fileExtension))
                .body(file);
    }
}
