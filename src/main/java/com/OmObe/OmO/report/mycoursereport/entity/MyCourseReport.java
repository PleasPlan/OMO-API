package com.OmObe.OmO.report.mycoursereport.entity;

import com.OmObe.OmO.MyCourse.entity.MyCourse;
import com.OmObe.OmO.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MyCourseReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    /*
    <신고 유형>
    1. 부적절한 주제
    2. 부정확한 정보
    3. 중복 게시물 도배
    4. 주제와 맞지 않음
    5. 욕설 및 비방
    6. 기타(신고 사유 작성 필수
     */
    @Column(nullable = false)
    private int reportType; // 신고 유형

    @Column(nullable = true)
    private String reason; // 신고 사유(신고 유형 6(기타)인 경우 필수 작성)

    @Column(updatable = false, name = "CREATED_AT")
    private LocalDateTime createdAt = LocalDateTime.now(); // 신고 작성 시간

    // MyCourseReport - Member 다대일 매핑
    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member; // 신고자

    // MyCourseReport - MyCourse 다대일 매핑
    @ManyToOne
    @JoinColumn(name = "MYCOURSE_ID")
    private MyCourse myCourse;

    public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setMyCourse(MyCourse myCourse) {
        this.myCourse = myCourse;
    }
}
