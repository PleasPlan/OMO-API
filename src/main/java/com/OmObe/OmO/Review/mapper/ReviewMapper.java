package com.OmObe.OmO.Review.mapper;

import com.OmObe.OmO.Place.service.PlaceService;
import com.OmObe.OmO.Review.dto.ReviewDto;
import com.OmObe.OmO.Review.entity.Review;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewMapper {
    private final PlaceService placeService;

    public ReviewMapper(PlaceService placeService) {
        this.placeService = placeService;
    }

    public Review reviewPostDtoToReview(ReviewDto.Post postDto){
        if(postDto == null){
            return null;
        }
        else{
            Review review = new Review();
            review.setContent(postDto.getContent());
            review.setPlaceId(postDto.getPlaceId());
            return review;
        }
    }

    public Review reviewPatchDtoToReview(ReviewDto.Patch patchDto){
        if(patchDto == null){
            return null;
        }
        else{
            Review review = new Review();
            review.setContent(patchDto.getContent());
            return review;
        }
    }

    public ReviewDto.Response reviewToReviewResponseDto(Review review){
        if(review == null){
            return null;
        }
        else{
            long reviewId = review.getReviewId();
            String content = review.getContent();
            LocalDateTime createdTime = review.getCreatedAt();
            String writer = review.getMember().getNickname();
            String writerProfile = review.getMember().getProfileImageUrl();
            String imageName = review.getImageName();
            ReviewDto.Response response = new ReviewDto.Response(reviewId,content,writer,writerProfile,imageName,createdTime);

            return response;
        }
    }

    public List<ReviewDto.Response> reviewsToReviewResponseDtos(List<Review> reviews){
        if(reviews == null){
            return null;
        } else {
            List<ReviewDto.Response> responses = new ArrayList<>();
            for(Review review:reviews){
                responses.add(this.reviewToReviewResponseDto(review));
            }
            return responses;
        }
    }
}
