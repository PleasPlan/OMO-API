package com.OmObe.OmO.MyCourse.controller;

import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.MyCourse.dto.MyCourseDto;
import com.OmObe.OmO.MyCourse.entity.MyCourse;
import com.OmObe.OmO.MyCourse.mapper.MyCourseMapper;
import com.OmObe.OmO.MyCourse.service.MyCourseService;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Slf4j
@Validated
@RequestMapping("/mycourse")
public class MyCourseController {

    private final MyCourseService myCourseService;
    private final MyCourseMapper mapper;
    private final TokenDecryption tokenDecryption;

    public MyCourseController(MyCourseService myCourseService,
                              MyCourseMapper mapper,
                              TokenDecryption tokenDecryption) {
        this.myCourseService = myCourseService;
        this.mapper = mapper;
        this.tokenDecryption = tokenDecryption;
    }

    // TODO: USER만 가능
    @PostMapping("/new")
    public ResponseEntity postCourse(@RequestBody MyCourseDto.Post postDto,
                                     @RequestHeader("Authorization") String token){
        List<MyCourse> courseList = mapper.coursePostDtoToCourse(postDto);
        Member writer = tokenDecryption.getWriterInJWTToken(token);


        MyCourse myCourse = myCourseService.createCourse(courseList,writer);

//        MyCourseDto.Response response = mapper.courseToCourseResponseDto(myCourse);
        MyCourseDto.ResponseDetailPlace response = mapper.courseToCourseResponseDtoDetailPlace(myCourse);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // TODO: USER만 가능
    @PutMapping("/rebuild")
    public ResponseEntity patchCourse(@RequestBody MyCourseDto.Patch patchDto,
                                      @RequestHeader("Authorization") String token){
        List<MyCourse> courseList = mapper.coursePatchDtoToCourse(patchDto);
        Long startId = patchDto.getCourseId();
        Member writer = tokenDecryption.getWriterInJWTToken(token);
        MyCourse myCourse = myCourseService.updateCourse(patchDto.getCourseName(),courseList,startId,writer, token);
        MyCourseDto.ResponseDetailPlace response = mapper.courseToCourseResponseDtoDetailPlace(myCourse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // TODO: USER만 가능
    @PatchMapping("/share/{course-id}")
    public ResponseEntity shareCourse(@PathVariable("course-id") long courseId,
                                      @RequestHeader("Authorization") String token){
        Member member = tokenDecryption.getWriterInJWTToken(token);
        MyCourse myCourse = myCourseService.shareCourse(courseId,member);
        MyCourseDto.ResponseDetailPlaceWithLiked response = mapper.courseToCourseResponseDtoDetailPlace(myCourse,member);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{course-id}")
    public ResponseEntity getCourse(@RequestHeader("Authorization") String token,
                                    @PathVariable("course-id") long startId){
        MyCourse myCourse = myCourseService.getCourse(startId);
        Member member = tokenDecryption.getWriterInJWTToken(token);
        MyCourseDto.ResponseDetailPlaceWithLiked response = mapper.courseToCourseResponseDtoDetailPlace(myCourse,member);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /*
    * sorting 가능 인자들
    * 1. createdAt : 최신순
    * 2. viewCount : 조회수 순
    * 3. likeCount : 좋아요 순
    * */

    @GetMapping("/mbti")
    public ResponseEntity getCourses(@RequestParam String IorE,
                                     @RequestParam String PorJ,
                                     @RequestParam(defaultValue = "1") int page,
                                     @Positive @RequestParam(defaultValue = "10") int size,
                                     @RequestParam String sorting,
                                     @Nullable @RequestHeader(value = "Authorization") String token){
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Boolean IE = null;
        Boolean PJ = null;
        switch (IorE) {
            case "I":
                IE = false;
                break;
            case "E":
                IE = true;
                break;
            default:
                log.info("IorE null");
                break;
        }
        switch (PorJ) {
            case "P":
                PJ = false;
                break;
            case "J":
                PJ = true;
                break;
            default:
                log.info("PorJ null");
                break;
        }
        Slice<MyCourse> pageMyCourses = myCourseService.findCoursesWithFilter(sorting, IE, PJ, page - 1, size);

        List<MyCourse> courses = pageMyCourses.getContent();
        if(token == null) {
            List<MyCourseDto.ResponseDetailPlace> responses = new ArrayList<>();
            courses.forEach(myCourse -> responses.add(mapper.courseToCourseResponseDtoDetailPlace(myCourse)));
            return new ResponseEntity<>(new MultiResponseDto<>(responses, pageMyCourses), HttpStatus.OK);
        }
        else{
            Member member = tokenDecryption.getWriterInJWTToken(token);
            List<MyCourseDto.ResponseDetailPlaceWithLiked> responses = new ArrayList<>();
            courses.forEach(myCourse -> responses.add(mapper.courseToCourseResponseDtoDetailPlace(myCourse,member)));
            return new ResponseEntity<>(new MultiResponseDto<>(responses, pageMyCourses), HttpStatus.OK);
        }
    }

    @GetMapping("/myCourse")
    public ResponseEntity getMyCourses(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "1") int page,
                                       @Positive @RequestParam(defaultValue = "10") int size){
        if(page <= 0){
            throw new BusinessLogicException(ExceptionCode.PAGE_NOT_IN_RANGE);
        }
        if(size <= 0){
            throw new BusinessLogicException(ExceptionCode.SIZE_NOT_IN_RANGE);
        }
        Member member = tokenDecryption.getWriterInJWTToken(token);
        Slice<MyCourse> pageMyCourses = myCourseService.findMyCourses(member,page-1,size);

        List<MyCourse> courses = pageMyCourses.getContent();
        List<MyCourseDto.ResponseDetailPlaceWithLiked> responses = new ArrayList<>();
        courses.forEach(myCourse -> responses.add(mapper.courseToCourseResponseDtoDetailPlace(myCourse,member)));
        return new ResponseEntity<>(new MultiResponseDto<>(responses, pageMyCourses), HttpStatus.OK);
    }

    // TODO: USER만 가능
    @DeleteMapping("/{course-id}")
    public ResponseEntity deleteCourse(@RequestHeader("Authorization") String token,
                                       @PathVariable("course-id") long startId){
        myCourseService.deleteCourse(startId, token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // TODO: USER만 가능
    @PutMapping("/like/{course-id}")
    public ResponseEntity postMyCourseLike(@RequestHeader("Authorization") String token,
                                           @PathVariable("course-id") long startId){
        Member member = tokenDecryption.getWriterInJWTToken(token);

        String response = myCourseService.createCourseLike(member,startId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
