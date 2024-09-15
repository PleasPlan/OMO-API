package com.OmObe.OmO.MyPage.controller;

import com.OmObe.OmO.Board.dto.BoardDto;
import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.mapper.BoardMapper;
import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.Board.response.PageInfo;
import com.OmObe.OmO.MyPage.dto.MyPageDto;
import com.OmObe.OmO.MyPage.mapper.MyPageMapper;
import com.OmObe.OmO.MyPage.service.MyPageService;
import com.OmObe.OmO.Place.entity.Place;
import com.OmObe.OmO.Place.entity.PlaceLike;
import com.OmObe.OmO.Place.service.PlaceService;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.dto.MemberDto;
import com.OmObe.OmO.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/myPage")
public class MyPageController {
    private final MyPageService myPageService;
    private final TokenDecryption tokenDecryption;
    private final BoardMapper boardMapper;
    private final MyPageMapper mapper;

    public MyPageController(MyPageService myPageService, TokenDecryption tokenDecryption, BoardMapper boardMapper, MyPageMapper mapper) {
        this.myPageService = myPageService;
        this.tokenDecryption = tokenDecryption;
        this.boardMapper = boardMapper;
        this.mapper = mapper;
    }

    @GetMapping("/likes")
    public ResponseEntity getLikes(@RequestHeader("Authorization") String token,
                                   @RequestParam(defaultValue = "1") int page,
                                   @Positive @RequestParam(defaultValue = "10") int size) throws JsonProcessingException {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Member member = tokenDecryption.getWriterInJWTToken(token);

        String placeList = myPageService.findPlaceLikedByMember(member, page - 1, size);

        return new ResponseEntity<>(placeList, HttpStatus.OK);
    }

    @GetMapping("/recommend")
    public ResponseEntity getRecommend(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "1") int page,
                                       @Positive @RequestParam(defaultValue = "10") int size) throws JsonProcessingException {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Member member = tokenDecryption.getWriterInJWTToken(token);

        String placeList = myPageService.findPlaceRecommendByMember(member, page - 1, size);

        return new ResponseEntity<>(placeList, HttpStatus.OK);
    }

    // No Content가 나오면 더이상 페이지가 없다는 뜻이다.
    @GetMapping("/boards")
    public ResponseEntity getMyBoard(@RequestHeader("Authorization") String token,
                                     @RequestParam(defaultValue = "1") int page,
                                     @Positive @RequestParam(defaultValue = "10") int size) throws JsonProcessingException {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Member member = tokenDecryption.getWriterInJWTToken(token);

        List<Board> boardList = myPageService.getMyBoard(member, page - 1, size);
        if (boardList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(boardMapper.boardsToBoardResponseDtos(boardList), HttpStatus.OK);
        }
    }

    @GetMapping("/lastVisited")
    public ResponseEntity getLastPlace(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "!") int page,
                                       @Positive @RequestParam(defaultValue = "10") int size,
                                       @RequestBody MyPageDto.PlaceList placeList) throws JsonProcessingException {
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Member member = tokenDecryption.getWriterInJWTToken(token);

        List<String> placeNameList = placeList.getPlaceNameList();
        List<Long> placeIdList = placeList.getPlaceIdList();
        placeIdList.forEach(it -> log.info("placeId : " + it));

        String response = myPageService.findLastPlace(member, page - 1, size, placeNameList, placeIdList);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 프로필 이미지 수정
    @PatchMapping("/profileImage")
    public ResponseEntity patchProfileImage(@RequestHeader("Authorization") String token,
                                            @Nullable @RequestParam("image")MultipartFile file) {
        Member findMember = tokenDecryption.getWriterInJWTToken(token); // token으로 Member 추출
        Member member = myPageService.updateProfileImage(findMember.getMemberId(), token, file);

        // 수정한 프로필 이미지의 파일명을 응답으로 제공
        MyPageDto.profileImageResponse profileImageResponse = mapper.memberToProfileImageName(member);
        return new ResponseEntity<>(profileImageResponse,HttpStatus.OK);
    }

    // 닉네임 수정
    @PatchMapping("/nickname")
    public ResponseEntity patchNickname(@RequestHeader("Authorization") String token,
                                        @Valid @RequestBody MemberDto.NicknamePatch dto) {
        Member member = tokenDecryption.getWriterInJWTToken(token); // token으로 Member 추출
        myPageService.updateNickname(member.getMemberId(), dto, token);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // MBTI 수정
    @PatchMapping("/mbti")
    public ResponseEntity patchMbti(@RequestHeader("Authorization") String token,
                                    @Valid @RequestBody MemberDto.MbtiPatch dto) {
        Member member = tokenDecryption.getWriterInJWTToken(token); // token으로 Member 추출
        myPageService.updateMbti(member.getMemberId(), dto, token);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 내 정보 조회
    @GetMapping("/myInfo")
    public ResponseEntity getMyInfo(@RequestHeader("Authorization") String token) {
        Member member = tokenDecryption.getWriterInJWTToken(token);
        MyPageDto.MyInfoResponse myInfo = myPageService.findMyInfo(member.getMemberId(), token); // token으로 Member 추출

        return new ResponseEntity<>(myInfo, HttpStatus.OK);
    }
}
