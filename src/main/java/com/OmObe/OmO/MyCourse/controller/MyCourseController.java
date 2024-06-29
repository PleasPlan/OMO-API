package com.OmObe.OmO.MyCourse.controller;

import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.MyCourse.dto.MyCourseDto;
import com.OmObe.OmO.MyCourse.entity.MyCourse;
import com.OmObe.OmO.MyCourse.mapper.MyCourseMapper;
import com.OmObe.OmO.MyCourse.service.MyCourseService;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
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

    @PutMapping("/rebuild")
    public ResponseEntity patchCourse(@RequestBody MyCourseDto.Patch patchDto,
                                      @RequestHeader("Authorization") String token){
        List<MyCourse> courseList = mapper.coursePatchDtoToCourse(patchDto);
        Long startId = patchDto.getCourseId();
        Member writer = tokenDecryption.getWriterInJWTToken(token);
        MyCourse myCourse = myCourseService.updateCourse(patchDto.getCourseName(),courseList,startId,writer);
        MyCourseDto.ResponseDetailPlace response = mapper.courseToCourseResponseDtoDetailPlace(myCourse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{course-id}")
    public ResponseEntity getCourse(@RequestHeader("Authorization") String token,
                                    @PathVariable("course-id") long startId){
        MyCourse myCourse = myCourseService.getCourse(startId);
        MyCourseDto.ResponseDetailPlace response = mapper.courseToCourseResponseDtoDetailPlace(myCourse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /*
    * sorting 가능 인자들
    * 1. createdAt : 최신순
    * 2. viewCount : 조회수 순
    * 3. likeCount : 좋아요 순
    * */
    @GetMapping("/mbti/{mbti-num}")
    public ResponseEntity getCourses(@PathVariable("mbti-num") int mbti,
                                     @RequestParam(defaultValue = "1") int page,
                                     @Positive @RequestParam(defaultValue = "10") int size,
                                     @RequestParam String sorting){
        Slice<MyCourse> pageMyCourses = null;
        if(mbti>16){
            pageMyCourses = myCourseService.findAllCourses(sorting,page-1,size);
        }else {
            pageMyCourses = myCourseService.findCourses(sorting, mbti, page - 1, size);
        }

        List<MyCourse> courses = pageMyCourses.getContent();
        List<MyCourseDto.ResponseDetailPlace> responses = new ArrayList<>();
        courses.forEach(myCourse -> responses.add(mapper.courseToCourseResponseDtoDetailPlace(myCourse)));
        return new ResponseEntity<>(new MultiResponseDto<>(responses, pageMyCourses), HttpStatus.OK);
    }

    @GetMapping("/myCourse")
    public ResponseEntity getMyCourses(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "1") int page,
                                       @Positive @RequestParam(defaultValue = "10") int size){

        Member member = tokenDecryption.getWriterInJWTToken(token);
        Slice<MyCourse> pageMyCourses = myCourseService.findMyCourses(member,page-1,size);

        List<MyCourse> courses = pageMyCourses.getContent();
        List<MyCourseDto.ResponseDetailPlace> responses = new ArrayList<>();
        courses.forEach(myCourse -> responses.add(mapper.courseToCourseResponseDtoDetailPlace(myCourse)));
        return new ResponseEntity<>(new MultiResponseDto<>(responses, pageMyCourses), HttpStatus.OK);
    }

    @DeleteMapping("/{course-id}")
    public ResponseEntity deleteCourse(@RequestHeader("Authorization") String token,
                                       @PathVariable("course-id") long startId){
        myCourseService.deleteCourse(startId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/like/{course-id}")
    public ResponseEntity postMyCourseLike(@RequestHeader("Authorization") String token,
                                           @PathVariable("course-id") long startId){
        Member member = tokenDecryption.getWriterInJWTToken(token);

        String response = myCourseService.createCourseLike(member,startId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
