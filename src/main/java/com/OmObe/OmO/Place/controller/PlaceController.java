package com.OmObe.OmO.Place.controller;

import com.OmObe.OmO.Place.dto.PlaceRecent;
import com.OmObe.OmO.Place.service.PlaceService;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.service.MemberService;
import com.OmObe.OmO.util.PairJ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.util.annotation.Nullable;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/place")
public class PlaceController {
    private final PlaceService placeService;
    private final TokenDecryption tokenDecryption;

    public PlaceController(PlaceService placeService, TokenDecryption tokenDecryption) {
        this.placeService = placeService;
        this.tokenDecryption = tokenDecryption;
    }

    @GetMapping("/list/{category}")
    public ResponseEntity getPlaces(@PathVariable("category") String category,
                                    @RequestHeader @Range(min = 0,max = 90) double y,
                                    @RequestHeader @Range(min = -180,max = 180) double x,
                                    @RequestParam("page") int page,
                                    @RequestHeader("Authorization") @Nullable String token){
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        PairJ<Double, Double> middle = new PairJ<>();
        middle.setFirst(y);    // y (위도)
        middle.setSecond(x);   // x (경도)

        String response = placeService.getPlaces(category, middle, page, token);
        // json 응답 중 "is_end":false 가 되어있다면 다음 페이지가 존재하는 것이다.
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // TODO: USER만 가능
    @PutMapping("/{placeName}")
    public ResponseEntity MineOrRecommend(@PathVariable("placeName") String placeName,
                                          @RequestHeader("placeId") long placeId,
                                          @RequestHeader("Authorization") String Token,
                                          @RequestHeader boolean LR){
                                          // Like = true, Recommend = false
        Member member = null;
        if(Token != null){
            member = tokenDecryption.getWriterInJWTToken(Token);
        }

        String response = placeService.putMineOrRecommend(placeId,placeName, member, LR);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{placeName}")
    public ResponseEntity getPlace(@PathVariable("placeName") String placeName,
                                   @RequestHeader("placeId") long placeId,
                                   @Nullable @RequestHeader("Authorization") String token){
        Member member = null;
        if(token != null){
            member = tokenDecryption.getWriterInJWTToken(token);
        }

        String response = placeService.getPlace(placeName,placeId,member);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/recent")
    public ResponseEntity getRecentPlace(@RequestBody PlaceRecent.Request request,
                                         @Nullable @RequestHeader("Authorization") String token){

        List<String> placeNameList = request.getPlaceNameList();
        List<Long> placeIdList = request.getPlaceIdList();

        Member member = null;
        if(token != null){
            member = tokenDecryption.getWriterInJWTToken(token);
        }

        String response = placeService.getRecentPlace(placeNameList,placeIdList,member);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/name/{placeName}")
    public ResponseEntity getPlaceWithoutId(@PathVariable("placeName") String placeName,
                                            @RequestParam("page") int page,
                                            @Nullable @RequestHeader("Authorization") String token){
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        String response = placeService.getPlace(placeName,page,token);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
