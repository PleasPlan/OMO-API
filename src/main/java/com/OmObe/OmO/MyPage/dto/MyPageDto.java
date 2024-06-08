package com.OmObe.OmO.MyPage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class MyPageDto {
    @Getter
    @AllArgsConstructor
    public static class Response{
        private long placeId;
        private String placeName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class PlaceList{
        private List<String> placeNameList;
        private List<Long> placeIdList;
    }

    // 마이페이지 - 내 정보 response Dto
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyInfoResponse{
        private String profileImageUrl;
        private String nickname;
        private String email;
        private LocalDate birth;
        private int mbti;
        private int placeLikeCount; // 내 관심 수
        private int placeRecommendCount; // 내 추천 수
        private int myWrittenBoardCount; // 내가 쓴 글 수
        private int myCourseCount; // 나의 코스 수
    }

    // 마이페이지 - 프로필 이미지 변경 후 respont dto
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class profileImageResponse{
        private String profileImageUrl; // 이미지 파일 이름
    }
}
