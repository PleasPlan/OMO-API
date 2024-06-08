package com.OmObe.OmO.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_NOT_FOUND(404, "Member not found. Please check the member ID and try again."),
    EMAIL_ALREADY_EXIST(404, "This Email is already exist. Please check the email"),
    NICKNAME_ALREADY_EXIST(404, "This Nickname is already exist. Please check the nickname"),
    BOARD_NOT_FOUND(404,"Missing Board"),
    COMMENT_NOT_FOUND(404,"Missing Comment"),
    PASSWORD_NOT_CORRECT(404, "Password is not correct. Pleas check the password"),
    INVALID_TOKEN(403, "This Token is invalid token. Please check the token"),
    REVIEW_NOT_FOUND(404, "Missing Review"),
    NOTICE_NOT_FOUND(404, "Notice not found, Please check the Notice Id and try again"),
    NOTICE_TYPE_ERROR(404, "Notice type is not invalid. Please check the Notice Type.(default/NOR/CHK"),
    REPORT_REASON_NOT_EXIST(404, "Report reason is not exist. Please check the report reason"),
    COURSE_NOT_FOUND(404, "Missing Course");


    @Getter
    private int status;

    @Getter
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}

