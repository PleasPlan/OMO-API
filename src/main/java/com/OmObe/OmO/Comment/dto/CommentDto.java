package com.OmObe.OmO.Comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class CommentDto {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Post{
        private long BoardId;
        @NotBlank(message = "내용을 입력해주세요.")
        private String content;
    }

    @Getter
    @Setter
    public static class Patch{
        private long commentId;
        private String content;
    }

    @Getter
    @Setter
    public static class Response{
        private long commentId;
        private String writer;
//        TODO : 프로필 url이 주석 해제되면 다시 해제할 것.
//        private String profileURL;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
