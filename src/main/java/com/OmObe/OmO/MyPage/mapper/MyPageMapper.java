package com.OmObe.OmO.MyPage.mapper;

import com.OmObe.OmO.MyPage.dto.MyPageDto;
import com.OmObe.OmO.member.entity.Member;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface MyPageMapper {
    // Member -> profileImageName
    MyPageDto.profileImageResponse memberToProfileImageName(Member member);
}
