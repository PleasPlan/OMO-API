package com.OmObe.OmO.Comment.service;

import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.service.BoardService;
import com.OmObe.OmO.Comment.entity.Comment;
import com.OmObe.OmO.Comment.repository.CommentRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TokenDecryption tokenDecryption;
    private final MemberService memberService;
    private final BoardService boardService;

    public CommentService(CommentRepository commentRepository, TokenDecryption tokenDecryption, MemberService memberService, BoardService boardService) {
        this.commentRepository = commentRepository;
        this.tokenDecryption = tokenDecryption;
        this.memberService = memberService;
        this.boardService = boardService;
    }

    /**
     * <댓글 작성>
     * 1. 토큰 검증
     * 2. 댓글 저장
     * @return
     */
    public Comment createComment(Comment comment, String token){
        // 1. 토큰 검증
        try {
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            comment.setMember(member);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }
        // 2. 댓글 저장
        return commentRepository.save(comment);
    }

    /**
     * <댓글 수정>
     * 1. 댓글 탐색(존재? or 존재X?)
     * 2. 토큰 검증
     * 3. 댓글 내용 수정
     * @return
     */
    public Comment updateComment(Comment comment, String token){
        // 1. 댓글 탐색(존재? or 존재X?)
        Comment findComment = findComment(comment.getCommentId());

        // 2. 토큰 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            // 댓글 작성자의 토큰과 현재 request header로 들어온 토큰 비교하여 검증
            memberService.verifiedAuthenticatedMember(findComment.getMember().getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // 3. 댓글 내용 수정
        Optional.ofNullable(comment.getContent())
                .ifPresent(content -> findComment.setContent(content));
        findComment.setModifiedAt(LocalDateTime.now());
        return commentRepository.save(findComment);
    }

    public Comment findComment(long commentId){
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        Comment comment = optionalComment.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.COMMENT_NOT_FOUND));
        return comment;
    }

    public Slice<Comment> findComments(int page, int size, long boardId){
        Board board = boardService.findBoard(boardId);
        return convertToSlice(commentRepository.findAll(withBoard(board), PageRequest.of(page,size,
                Sort.by("createdAt").descending())));
    }

    /**
     * <댓글 삭제
     * 1. 댓글 존재 유무 파악
     * 2. 토큰 검증
     * 3. 댓글 삭제
     */
    public void deleteComment(long commentId, String token){
        // 1. 댓글 존재 유무 파악
        Comment findComment = findComment(commentId);

        // 2. 토큰 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            // 댓글 작성자의 토큰과 현재 request header에 포함된 토큰은 비교
            memberService.verifiedAuthenticatedMember(findComment.getMember().getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        // 3. 댓글 삭제ㅜ
        commentRepository.delete(findComment);
    }

    public static Slice<Comment> convertToSlice(Page<Comment> page){
        return new SliceImpl<>(page.getContent(), page.getPageable(), page.hasNext());
    }

    public static Specification<Comment> withBoard(Board board){
        return (Specification<Comment>) ((root, query,builder) ->
                builder.equal(root.get("board"),board));
    }
}
