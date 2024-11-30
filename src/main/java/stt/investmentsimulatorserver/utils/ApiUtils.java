package stt.investmentsimulatorserver.utils;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ApiUtils<T> {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(HttpStatus.OK, data, null);
    }

    public static <T> ApiResult<T> error(HttpStatus httpStatus, String message) {
        return new ApiResult<>(httpStatus, null, message);
    }

    public static <T> ApiResult<T> error(HttpStatus httpStatus) {
        return new ApiResult<>(httpStatus, null, null);
    }

    @Getter
    @AllArgsConstructor
    public static class ApiResult<T> {
        HttpStatus httpStatus;
        T data;
        String message;
    }
}
