package com.OmObe.OmO.Review.controller;

import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.Review.dto.ReviewDto;
import com.OmObe.OmO.Review.entity.Review;
import com.OmObe.OmO.Review.mapper.ReviewMapper;
import com.OmObe.OmO.Review.repository.ReviewRepository;
import com.OmObe.OmO.Review.service.ReviewService;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper mapper;
    private final ReviewRepository reviewRepository;

    private final TokenDecryption tokenDecryption;

    public ReviewController(ReviewService reviewService, ReviewMapper mapper, ReviewRepository reviewRepository, TokenDecryption tokenDecryption) {
        this.reviewService = reviewService;
        this.mapper = mapper;
        this.reviewRepository = reviewRepository;
        this.tokenDecryption = tokenDecryption;
    }

    @SneakyThrows
    @PostMapping("/write")
    public ResponseEntity postReview(//@Valid @RequestBody ReviewDto.Post postDto,
                                     @RequestHeader("Authorization") String token,
                                     @RequestParam("content") String content,
                                     @RequestParam("placeId") long placeId,
                                     @Nullable @RequestParam("image")MultipartFile file){
//        Member writer = tokenDecryption.getWriterInJWTToken(Token);

        ReviewDto.Post postDto = new ReviewDto.Post(content,placeId);

        Review review = mapper.reviewPostDtoToReview(postDto);
//        review.setMember(writer);
        Review response = reviewService.createReview(review, token, file);
        return new ResponseEntity<>(mapper.reviewToReviewResponseDto(response),
                HttpStatus.CREATED);
    }

    @PatchMapping("/modification")
    public ResponseEntity patchReview(@RequestParam("content") String content,
                                      @RequestHeader("review-id") long reviewId,
                                      @RequestHeader("Authorization") String token,
                                      @Nullable @RequestParam("image") MultipartFile file){
        // patchDto.setReviewId(reviewId);

        ReviewDto.Patch patchDto = new ReviewDto.Patch(reviewId,content);

        Review review = mapper.reviewPatchDtoToReview(patchDto);
        Review response = reviewService.updateReview(review,reviewId, token, file);

        return new ResponseEntity<>(mapper.reviewToReviewResponseDto(response),
                HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity getReview(@RequestHeader("review-id") long reviewId){
        Review response = reviewService.getReview(reviewId);

        return new ResponseEntity<>(mapper.reviewToReviewResponseDto(response),
                HttpStatus.OK);
    }

    @GetMapping("/{place-Id}")
    public ResponseEntity getReviews(@RequestParam(defaultValue = "1") int page,
                                     @Positive @RequestParam(defaultValue = "5") int size,
                                     @PathVariable("place-Id") long placeId){
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Page<Review> pageReviews = reviewService.findReviewsByCreatedAt(placeId,page-1,size);
        List<Review> reviews = pageReviews.getContent();
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.reviewsToReviewResponseDtos(reviews),pageReviews),
                        HttpStatus.OK);
    }

    @DeleteMapping("/{review-Id}")
    public ResponseEntity deleteReview(@PathVariable("review-Id") @Positive long reviewId,
                                       @RequestHeader("Authorization") String token){
        reviewService.deleteReview(reviewId, token);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
