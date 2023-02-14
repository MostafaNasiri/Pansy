package io.github.mostafanasiri.pansy.app.common;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Api response structure is based on JSend specification: https://github.com/omniti-labs/jsend
 */
public record ApiResponse<T>(Status status, T data) {
    public enum Status {
        SUCCESS,
        FAIL,
        ERROR;

        @JsonValue
        public String toLowerCase() {
            return toString().toLowerCase();
        }
    }
}
