package com.OmObe.OmO.advice;

import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice // 전역 예외 처리
public class GlobalExceptionAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException( // 유효성 검증에 실패한 필드에 대한 예외 처리
            MethodArgumentNotValidException e
    ) {
        final ErrorResponse response = ErrorResponse.of(e.getBindingResult());

        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException( // 제약 조건 위반과 관련된 예외 처리
            ConstraintViolationException e
    ) {
        final ErrorResponse response = ErrorResponse.of(e.getConstraintViolations());

        return response;
    }

    @ExceptionHandler
    public ErrorResponse handleBusinessLogicException
            (BusinessLogicException e) { // BusinessLogicException에 있는 예외 처리
        final ErrorResponse response = ErrorResponse.of(e.getExceptionCode());

        return response;
    }

    @ExceptionHandler
    public ErrorResponse handleHttpRequestMethodNotSupportedException // 요청된 HTTP 메서드가 허용되지 않는 경우의 예외 처리
            (HttpRequestMethodNotSupportedException e) {
        final ErrorResponse response = ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED);

        return response;
    }

    // 구현 상 오류에 대한 오류는 로그 없이 Internal server error만 리턴하는 문제 때문에 주석 처리
//    @ExceptionHandler
//    public ErrorResponse handleException(Exception e) { // 구현 상의 오류로 인해 발생하는 예외 처리
//        final ErrorResponse response = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR);
//
//        return response;
//    }
}
