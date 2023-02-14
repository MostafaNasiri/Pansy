package io.github.mostafanasiri.pansy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mostafanasiri.pansy.app.common.ApiResponse;

import java.io.IOException;

public abstract class BaseControllerTest {
    protected String mapToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }

    protected <T> T mapFromJson(String json, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, clazz);
    }

    protected <T> String createSuccessApiResponse(T data) throws JsonProcessingException {
        return mapToJson(
                new ApiResponse<T>(ApiResponse.Status.SUCCESS, data)
        );
    }

    protected <T> String createFailApiResponse(T data) throws JsonProcessingException {
        return mapToJson(
                new ApiResponse<T>(ApiResponse.Status.FAIL, data)
        );
    }
}
