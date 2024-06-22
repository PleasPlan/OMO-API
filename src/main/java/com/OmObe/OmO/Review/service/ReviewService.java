package com.OmObe.OmO.Review.service;

import com.OmObe.OmO.ImageManager.ImageManager;
import com.OmObe.OmO.Review.entity.FileData;
import com.OmObe.OmO.Review.entity.Review;
import com.OmObe.OmO.Review.repository.FileDataRepository;
import com.OmObe.OmO.Review.repository.ReviewRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    @Value("${image.file-path}")
    private String FILE_PATH;

    private final ReviewRepository reviewRepository;
    private final MemberService memberService;
    private final TokenDecryption tokenDecryption;
    private final FileDataRepository fileDataRepository;
    private final ImageManager imageManager;

    public ReviewService(ReviewRepository reviewRepository,
                         MemberService memberService,
                         TokenDecryption tokenDecryption,
                         FileDataRepository fileDataRepository,
                         ImageManager imageManager) {
        this.reviewRepository = reviewRepository;
        this.memberService = memberService;
        this.tokenDecryption = tokenDecryption;
        this.fileDataRepository = fileDataRepository;
        this.imageManager = imageManager;
    }

    /**
     * <리뷰 작성>
     * 1. 토큰 검증
     * 2. 리뷰 저장
     */
    public Review createReview(Review review, String token, MultipartFile file){
        // 1. 토큰 검증
        try{
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            review.setMember(member);
            if(file != null) {
                review.setImageName(imageManager.uploadImageToFileSystem(file));
            }
        }catch (JsonProcessingException je) {
            throw new RuntimeException(je);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // 2. 리뷰 저장
        return reviewRepository.save(review);
    }

    /**
     * <리뷰 수정>
     * 1. 수정하려는 리뷰의 존재 여부 파악
     * 2. 토큰 검증
     * 3. 리뷰 내용 수정
     * 4. 변경 사항 저장
     */
    public Review updateReview(Review review,long reviewId, String token,MultipartFile file){
        // 1. 수정하려는 리뷰의 존재 여부 파악
        Review findReview = findReview(reviewId);

        // 2. 토큰 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            // 리뷰 작성자의 토큰과 현재 request header로 들어온 토큰 비교하여 검증
            memberService.verifiedAuthenticatedMember(findReview.getMember().getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // 3. 리뷰 내용 수정
        Optional.ofNullable(review.getContent())
                .ifPresent(content -> findReview.setContent(content));
        Optional.ofNullable(file)
                .ifPresent(image -> {
                    try {
                        imageManager.deleteImage(findReview.getImageName());
                        findReview.setImageName(null);
                        if(file != null) {
                            findReview.setImageName(imageManager.uploadImageToFileSystem(file));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        findReview.setModifiedAt(LocalDateTime.now());

        // 4. 변경 사항 저장
        return reviewRepository.save(findReview);
    }

    public Review getReview(long reviewId){
        Review findReview = findReview(reviewId);
        return findReview;
    }

    public Page<Review> findReviewsByCreatedAt(long placeId, int page, int size){
        return reviewRepository.findAll(withPlaceId(placeId), PageRequest.of(page,size,
                Sort.by("createdAt").descending()));
    }

    /**
     * <리뷰 삭제>
     * 1. 삭제하려는 리뷰 존재 여부 파악
     * 2. 토큰 검증
     * 3. 리뷰 삭제
     */
    public void deleteReview(long reviewId, String token){
        // 1. 삭제하려는 리뷰 존재 여부 파악
        Review findReview = findReview(reviewId);

        // 2. 토큰 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            // 리뷰 작성자의 토큰과 현재 request header로 들어온 토큰 비교하여 검증
            memberService.verifiedAuthenticatedMember(findReview.getMember().getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }
        imageManager.deleteImage(findReview.getImageName());
        // 3. 리뷰 삭제
        reviewRepository.delete(findReview);
    }

    public Review findReview(long reviewId){
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        Review review = optionalReview.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.REVIEW_NOT_FOUND));
        return review;
    }

    public static Specification<Review> withPlaceId(long placeId){
        return (Specification<Review>) ((root, query, builder) ->
                builder.equal(root.get("placeId"),placeId));
    }

/*    public String uploadImageToFileSystem(MultipartFile file) throws IOException {
        String filePath = FILE_PATH+file.getOriginalFilename();
        FileData fileData = fileDataRepository.save(FileData.builder()
                        .name(file.getOriginalFilename())
                        .type(file.getContentType())
                        .filePath(filePath)
                .build());

        file.transferTo(new File(filePath));

        if(fileData != null){
            return file.getOriginalFilename();
        }
        return null;
    }

    public byte[] downloadImageFromFileSystem(String fileName) throws IOException {
        String keyword;
        try{
            keyword = URLEncoder.encode(fileName, "UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException("Encoding Failed",e);
        }


        Optional<FileData> optionalFileData = fileDataRepository.findByName(keyword);
        String filePath = optionalFileData.get().getFilePath();
        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;
    }

    public void deleteImage(String fileName){
        Optional<FileData> optionalFileData = fileDataRepository.findByName(fileName);
        String filePath = optionalFileData.get().getFilePath();
        File image = new File(filePath);
        image.delete();
        fileDataRepository.delete(optionalFileData.get());
    }*/
}
