package io.github.mostafanasiri.pansy.common;

import io.github.mostafanasiri.pansy.common.exception.EntityNotFoundException;
import io.github.mostafanasiri.pansy.common.exception.InternalErrorException;
import io.github.mostafanasiri.pansy.common.exception.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = InternalErrorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ApiResponse<String> handleException(InternalErrorException e) {
        return new ApiResponse<>(ApiResponse.Status.ERROR, e.getMessage());
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ApiResponse<String> handleException(EntityNotFoundException e) {
        return new ApiResponse<>(ApiResponse.Status.FAIL, e.getMessage());
    }

    @ExceptionHandler(value = InvalidInputException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public @ResponseBody ApiResponse<String> handleException(InvalidInputException e) {
        return new ApiResponse<>(ApiResponse.Status.FAIL, e.getMessage());
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public @ResponseBody ApiResponse<String> handleException(MaxUploadSizeExceededException e) {
        return new ApiResponse<>(ApiResponse.Status.ERROR, "The file is too large.");
    }

    // Handle Spring validation exceptions
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody ApiResponse<Map<String, String>> handleException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ApiResponse<>(ApiResponse.Status.FAIL, errors);
    }
}
