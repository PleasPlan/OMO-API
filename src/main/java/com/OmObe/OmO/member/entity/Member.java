package com.OmObe.OmO.member.entity;

import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Liked.entity.Liked;
import com.OmObe.OmO.MyCourse.entity.MyCourseLike;
import com.OmObe.OmO.MyCourse.entity.MyCourse;
import com.OmObe.OmO.Place.entity.PlaceLike;
import com.OmObe.OmO.Place.entity.PlaceRecommend;
import com.OmObe.OmO.notice.entity.Notice;
import com.OmObe.OmO.report.boardreport.entity.BoardReport;
import com.OmObe.OmO.report.commentreport.entity.CommentReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 프로필 이미지, 사용자 권한은 security 적용 후 구현할 예정
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    @Column(length = 300, nullable = false)
    private String password; // 비밀번호

//    @Column(length = 300, nullable = false)
//    private String checkPassword; // 비밀번호 확인

    @Column
    private int birthYear; // 생년월일 - 년

    @Column
    private int birthMonth; // 생년월일 - 월

    @Column
    private int birthDay; // 생년월일 - 일

    @Column//(nullable = false)
    private LocalDate birth; // 생년월일

    @Column(length = 30, unique = true)
    private String nickname; // 닉네임

    @Column
    private int mbti; // mbti 유형

    @Column
    private int gender; // 성별

    @Column(nullable = false)
    private Boolean clause; // 이용약관 동의 여부

    @Column
    private String profileImageUrl; // 프로필 이미지 url

    @Column
    private boolean isOAuth; // OAuth2 사용 여부

    /* 프로필 이미지 변경 시 기존 이미지 파일을 지워야 하는데 oauth에서 받아온 이미지라면 이미지 파일이 없기 때문에
       이미지 파일 존재 여부를 파악하기 위해 isExistFile을 통해 파악한다.
       isExistFile - true : 이미지 파일 존재
       isExistFile - false : 이미지 파일이 존재하지 않음. oauth 로그인 시 받아온 기본 이미지 url.
     */
    @Column
    private boolean isExistFile; // oauth에서 제공된 기본 이미지가 아닌 사용자가 설정한 이미지 사용 여부 (default : false)

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE; // 회원 상태 -> 기본값은 활동 상태

    // 회원의 권한 정보 테이블과 매핑되는 정보
//    @ElementCollection(fetch = FetchType.EAGER)
//    private List<String> roles = new ArrayList<>();

    // 회원 권한 정보
    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;


    public void setPassword(String password) {
        this.password = password;
    }
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    // TODO : Merge후 주석 해제할 것.

    @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
    private List<Liked> likedList = new ArrayList<>();

    // Member - Notice 일대다 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notice> notices = new ArrayList<>();

    // Member - BoardReport 일대다 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<BoardReport> boardReports = new ArrayList<>();

    // Member - CommentReport 일대다 매핑
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<CommentReport> commentReports = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PlaceLike> placeLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PlaceRecommend> placeRecommends = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "member",cascade = CascadeType.PERSIST)
    private List<MyCourseLike> myCourseLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "member",cascade = CascadeType.ALL)
    private List<MyCourse> myCourses = new ArrayList<>();

    public void addLikes(Liked liked){
        this.likedList.add(liked);
        liked.setMember(this);
    }

    public void addPlaceLikes(PlaceLike placeLike){
        this.placeLikes.add(placeLike);
        placeLike.setMember(this);
    }

    public void deletePlaceLikes(PlaceLike placeLike){
        this.placeLikes.remove(placeLike);
    }

    public void addPlaceRecommend(PlaceRecommend placeRecommend){
        this.placeRecommends.add(placeRecommend);
        placeRecommend.setMember(this);
    }

    public void deletePlaceRecommend(PlaceRecommend placeRecommend){
        this.placeRecommends.remove(placeRecommend);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public void setBirthMonth(int birthMonth) {
        this.birthMonth = birthMonth;
    }

    public void setBirthDay(int birthDay) {
        this.birthDay = birthDay;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setMbti(int mbti) {
        this.mbti = mbti;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setClause(Boolean clause) {
        this.clause = clause;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setOAuth(boolean OAuth) {
        isOAuth = OAuth;
    }

    public void setMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public void setExistFile(boolean existFile) {
        isExistFile = existFile;
    }

    public enum MemberRole{ // 사용자 권한 등급
        GUEST("ROLE_GUEST"), // 최초 로그인 시 부여되는 권한
        USER("ROLE_USER"), // 일반 유저 권한
        ADMIN("ROLE_ADMIN"); // 관리자 권한

        @Getter
        private final String role;

        MemberRole(String role) {
            this.role = role;
        }
    }

    // 회원 상태(활동 / 탈퇴)
    public enum MemberStatus {
        MEMBER_ACTIVE("활동"),
        MEMBER_QUIT("탈퇴"),
        ;

        @Getter
        private String status;

        MemberStatus(String status) {
            this.status = status;
        }
    }
}
