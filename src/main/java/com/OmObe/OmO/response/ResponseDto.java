package com.OmObe.OmO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// api 요청에 대한 status와 message 응답을 위한 dto 클래스
public class ResponseDto {
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Response{
        private int status; // 상태 코드
        private String message; // 상태 메시지
    }
}
