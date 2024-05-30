package com.OmObe.OmO.auth.oauth.service;

import com.OmObe.OmO.auth.utils.MemberAuthorityUtils;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2MemberService implements OAuth2UserService<OAuth2UserRequest , OAuth2User> {
    @Value("${mail.address.admin}")
    private String adminMail; // 관리자 이메일

    private final MemberRepository memberRepository;
    private final MemberAuthorityUtils authorityUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        log.info("{}", registrationId);

        return oAuth2User;
    }

    //  OAuth2를 통해 얻은 정보를 Member 데이터에 맞게 처리하는 메서드
    public Member createOAuth2Member(OAuth2User oAuth2User) {
        String email = String.valueOf(oAuth2User.getAttributes().get("email")); // Oauth2객체를 통해 이메일 주소 추출
        String profileImageUrl; // 프로필 이미지

        // 클라이언트마다 프로필 이미지를 의미하는 이름이 다르기 때문에 각 케이스 별로 처리
        if (oAuth2User.getAttributes().containsKey("picture")) {
            profileImageUrl = String.valueOf(oAuth2User.getAttributes().get("picture"));
        } else if (oAuth2User.getAttributes().containsKey("profileImage")) {
            profileImageUrl = String.valueOf(oAuth2User.getAttributes().get("profileImage"));
        } else {
            profileImageUrl = null;
        }

        log.info("email : {}", email);

        // OAuth2를 통해 얻은 이메일로 권한 부여
        List<String> authorities = authorityUtils.createRoles(email);
        log.info("role : {}", authorities);

        // 이메일을 통해 db에 존재하는 회원인지 확인
        Member member = memberRepository.findByEmail(email).orElse(null);

        if (member == null) { // db에 없는 회원인 경우 새로 저장
            // <임시 저장 - 회원 정보 수정에서 수정해야 함>
            // db 저장용 비밀번호 생성
            String tmpPassword = UUID.randomUUID().toString().replace("-", "").substring(0,20);
            log.info("tmpPassword : {}", tmpPassword);

            member = new Member();
            member.setEmail(email);

            // 회원 등급에 따른 권한, 닉네임 설정
            log.info("adminMail : {}", adminMail);
            if (member.getEmail().equals(adminMail)) {
                member.setMemberRole(Member.MemberRole.ADMIN); // 관리자 이메일이면 회원의 권한은 ADMIN
                member.setNickname("OmO 관리자"); // 관리자 닉네임은 "OmO 관리자"로 설정
            }else{
                member.setMemberRole(Member.MemberRole.GUEST); // 관리자 이메일이 아니면 최초 로그인 시 회원의 권한은 GUEST
                // 최초 로그인 시 회원 닉네임 임의 지정
                String tmpNickname = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                member.setNickname(tmpNickname);
            }

            member.setPassword(tmpPassword);
            member.setOAuth(true);
            member.setProfileImageUrl(profileImageUrl);
            member.setClause(true);
            member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);

            return memberRepository.save(member);
        }

        // 이미 등록된 회원인 경우 해당 회원 리턴
        return member;

    }
}
