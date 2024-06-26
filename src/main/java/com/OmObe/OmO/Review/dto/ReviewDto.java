package com.OmObe.OmO.Review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class ReviewDto {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Post{
        @NotBlank(message = "내용을 입력해주십시오.")
        private String content;

        private long placeId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Patch{
        private long reviewId;
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public static class Response{
        private long ReviewId;
        private String content;
        private String writer;
        private String writerProfileImage;
        private String imageName;
        private LocalDateTime createdDate;
    }
}
