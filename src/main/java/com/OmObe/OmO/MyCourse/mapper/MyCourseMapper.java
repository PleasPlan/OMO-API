package com.OmObe.OmO.MyCourse.mapper;

import com.OmObe.OmO.MyCourse.dto.MyCourseDto;
import com.OmObe.OmO.MyCourse.entity.MyCourse;
import com.OmObe.OmO.MyCourse.entity.MyCourseLike;
import com.OmObe.OmO.MyCourse.repository.MyCourseLikeRepository;
import com.OmObe.OmO.Place.service.PlaceService;
import com.OmObe.OmO.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MyCourseMapper {

    private final PlaceService placeService;
    private final MyCourseLikeRepository myCourseLikeRepository;

    public MyCourseMapper(PlaceService placeService, MyCourseLikeRepository myCourseLikeRepository) {
        this.placeService = placeService;
        this.myCourseLikeRepository = myCourseLikeRepository;
    }

    public List<MyCourse> coursePostDtoToCourse(MyCourseDto.Post postDto){
        if(postDto == null){
            log.error("no value");
            return null;
        } else if ((postDto.getTime().size() != postDto.getPlaceName().size())
                        ||
                (postDto.getPlaceName().size() != postDto.getPlaceId().size()))
        {     // 무결성 검사 -> 세 List의 크기가 같다면
            log.error("unbalanced values");
            return null;
        }
        else {
            List<MyCourse> courseList = new ArrayList<>();
            settingNextCoursePost(courseList,postDto);
            /*for(int index = postDto.getPlaceName().size()-1; index>=0; index--) {
                MyCourse course = new MyCourse();
                course.setCourseName(postDto.getCourseName());
                course.setPlaceName(postDto.getPlaceName().get(index));
                log.info(course.getPlaceName());
                course.setPlaceId(postDto.getPlaceId().get(index));
                course.setTimes(postDto.getTime().get(index));
                if(index == 0){
                    course.setNextCourse(null);
                } else {
                    course.setNextCourse(courseList.get(index-1));
                }
                courseList.add(course);
                log.info("added");
            }*/
            Collections.reverse(courseList);
            return courseList;
        }
    }

    public List<MyCourse> coursePatchDtoToCourse(MyCourseDto.Patch patchDto){
        if(patchDto == null){
            return null;
        } else if ((patchDto.getTime().size() != patchDto.getPlaceName().size())
                        ||
                (patchDto.getPlaceName().size() != patchDto.getPlaceId().size())
        ){     // 무결성 검사 -> 세 List의 크기가 같다면
            return null;
        }
        else {
            List<MyCourse> courseList = new ArrayList<>();
            settingNextCoursePatch(courseList,patchDto);
            return courseList;
        }
    }

    public MyCourseDto.Response courseToCourseResponseDto(MyCourse course){
        if(course == null){
            return null;
        } else {
            Long courseId = course.getCourseId();
            String courseName = course.getCourseName();
            List<MyCourseDto.ResponseSmall> contents = new ArrayList<>();
            getNextCourses(contents,course);
            Collections.reverse(contents);
            LocalDateTime createdAt = course.getCreatedAt();
            LocalDateTime modifiedAt = course.getModifiedAt();
            String writerName = course.getMember().getNickname();
            Integer likeCount = course.getLikeCount();

            MyCourseDto.Response response = new MyCourseDto.Response(courseId,courseName,contents,createdAt,modifiedAt,likeCount,writerName);
            return response;
        }
    }

    public MyCourseDto.ResponseDetailPlace courseToCourseResponseDtoDetailPlace(MyCourse course){
        if(course == null){
            return null;
        } else {
            Long courseId = course.getCourseId();
            String courseName = course.getCourseName();
            List<MyCourseDto.ResponseSmallDetailPlace> contents = new ArrayList<>();
            getNextCoursesMoreDetail(contents,course,course.getMember());
            Collections.reverse(contents);
            LocalDateTime createdAt = course.getCreatedAt();
            LocalDateTime modifiedAt = course.getModifiedAt();
            String writerName = course.getMember().getNickname();
            Integer likeCount = course.getLikeCount();

            MyCourseDto.ResponseDetailPlace response = new MyCourseDto.ResponseDetailPlace(courseId,courseName,contents,createdAt,modifiedAt,likeCount,writerName);
            return response;
        }
    }

    public MyCourseDto.ResponseDetailPlaceWithLiked courseToCourseResponseDtoDetailPlace(MyCourse course,Member member){
        if(course == null){
            return null;
        } else {
            Long courseId = course.getCourseId();
            String courseName = course.getCourseName();
            List<MyCourseDto.ResponseSmallDetailPlace> contents = new ArrayList<>();
            getNextCoursesMoreDetail(contents,course,member);
            Collections.reverse(contents);
            LocalDateTime createdAt = course.getCreatedAt();
            LocalDateTime modifiedAt = course.getModifiedAt();
            LocalDateTime sharedAt = course.getSharedAt();
            String writerName = course.getMember().getNickname();
            Integer likeCount = course.getLikeCount();

            Optional<MyCourseLike> myLiked = myCourseLikeRepository.findByMemberAndMyCourse(member,course);

            Boolean shared = course.getShare();
            Boolean writerUserMatch = course.getMember() == member;
            MyCourseDto.ResponseDetailPlaceWithLiked response =
                    new MyCourseDto.ResponseDetailPlaceWithLiked(
                            courseId,
                            courseName,
                            contents,
                            createdAt,
                            modifiedAt,
                            sharedAt,
                            likeCount,
                            writerName,
                            myLiked.isPresent(),
                            shared,
                            writerUserMatch
                            );
            return response;
        }
    }

    private static void settingNextCoursePost(List<MyCourse> courseList, MyCourseDto.Post postDto){
        for(int index = postDto.getPlaceName().size()-1; index>=0; index--) {
            MyCourse course = new MyCourse();
            if(index == 0) {
                course.setCourseName(postDto.getCourseName());
            }
            course.setPlaceName(postDto.getPlaceName().get(index));
            course.setPlaceId(postDto.getPlaceId().get(index));
            course.setTimes(postDto.getTime().get(index));
            courseList.add(course);
        }
    }

    private static void settingNextCoursePatch(List<MyCourse> courseList, MyCourseDto.Patch patchDto){
        for(int index = patchDto.getPlaceName().size()-1; index>=0; index--) {
            MyCourse course = new MyCourse();
            if(index == 0) {
                course.setCourseName(patchDto.getCourseName());
            }
            course.setPlaceName(patchDto.getPlaceName().get(index));
            course.setPlaceId(patchDto.getPlaceId().get(index));
            course.setTimes(patchDto.getTime().get(index));
            courseList.add(course);
        }
    }
    private static void getNextCourses(List<MyCourseDto.ResponseSmall> contents, MyCourse course){
        MyCourseDto.ResponseSmall responseSmall = new MyCourseDto.ResponseSmall(course.getPlaceName(),
                course.getPlaceId(),
                course.getTimes());
        if(course.getNextCourse() != null){
            getNextCourses(contents, course.getNextCourse());
        }
        contents.add(responseSmall);
    }
    private void getNextCoursesMoreDetail(List<MyCourseDto.ResponseSmallDetailPlace> contents, MyCourse course, Member member){
        String placeJsonData = placeService.getPlace(course.getPlaceName(),course.getPlaceId(),member);
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(placeJsonData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        MyCourseDto.ResponseSmallDetailPlace responseSmallDetailPlace = new MyCourseDto.ResponseSmallDetailPlace(jsonNode,course.getTimes());
        if(course.getNextCourse() != null){
            getNextCoursesMoreDetail(contents, course.getNextCourse(),member);
        }
        contents.add(responseSmallDetailPlace);
    }
}
