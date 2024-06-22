package com.OmObe.OmO.Board.service;

import com.OmObe.OmO.Board.dto.BoardDto;
import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.mapper.BoardMapper;
import com.OmObe.OmO.Board.repository.BoardRepository;
import com.OmObe.OmO.Liked.entity.Liked;
import com.OmObe.OmO.Liked.repository.LikedRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class BoardService {

    private final BoardRepository boardRepository;
    private final LikedRepository likedRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final BoardMapper mapper;
    private final TokenDecryption tokenDecryption;

    public BoardService(BoardRepository boardRepository, LikedRepository likedRepository, MemberService memberService, MemberRepository memberRepository, BoardMapper mapper, TokenDecryption tokenDecryption) {
        this.boardRepository = boardRepository;
        this.likedRepository = likedRepository;
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
        this.tokenDecryption = tokenDecryption;
    }

    public Board createBoard(Board board, String token) {
        try {
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            board.setMember(member);
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }
        return boardRepository.save(board);
    }

    public Board updateBoard(BoardDto.Patch patch, String token){
        Board board = mapper.boardPatchDtoToBoard(patch);
        Board findBoard = findBoard(board.getBoardId());


        // 사용자의 로그인 인증 상태 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
            memberService.verifiedAuthenticatedMember(findBoard.getMember().getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        Optional.ofNullable(board.getTitle())
                .ifPresent(title -> findBoard.setTitle(title));
        Optional.ofNullable(board.getContent())
                .ifPresent(content -> findBoard.setContent(content));
        findBoard.setModifiedAt(LocalDateTime.now());
        return boardRepository.save(findBoard);
    }

    public Board findBoard(long boardId){
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        Board board = optionalBoard.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
        
        return board;
    }

    public Board getBoard(long boardId){
        Board findBoard = findBoard(boardId);
        findBoard.setViewCount(findBoard.getViewCount()+1);
        return boardRepository.save(findBoard);
    }

    public Slice<Board> findBoardsByCreatedAt(String type, int page, int size){
        return convertToSlice(boardRepository.findAll(withType(type),PageRequest.of(page, size,
                        Sort.by("createdAt").descending())));
    }

    public Slice<Board> findBoardsByViewCount(String type, int page, int size){
        return convertToSlice(boardRepository.findAll(withType(type),PageRequest.of(page, size,
                Sort.by("viewCount").descending())));
    }

    public Slice<Board> findBoardsByLikes(String type, int page, int size) {
        return convertToSlice(boardRepository.findAll(withType(type),PageRequest.of(page, size,
                Sort.by("likesCount").descending())));
    }

    public Slice<Board> findBoardsByComments(String type, int page, int size) {
        return convertToSlice(boardRepository.findAll(withType(type),PageRequest.of(page, size,
                Sort.by("commentsCount").descending())));
    }

    public void deleteBoard(long boardId, String token){
        Board findBoard = findBoard(boardId);
        memberService.verifiedAuthenticatedMember(findBoard.getMember().getMemberId());

        // 사용자 인증 상태 검증
        try {
            /*
            서버의 오류 등으로 인해 member 테이블에 데이터가 다시 들어가게 된 상황에서 기존 유효 기간이 남아있는
            토큰으로 접근하면 다른 회원의 정보로 접근할 가능성이 있기 때문에 verifiedAuthenticatedMember를 통해
            회원의 이메일을 검증하여 회원의 정보와 권한을 파악하여 서비스에 접근 허용 및 제한 한다.
             */
            Member member = tokenDecryption.getWriterInJWTToken(token);
            memberService.verifiedAuthenticatedMember(member.getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }

        boardRepository.delete(findBoard);
    }

    public void likesBoard(long boardId,Member member){
        // request header에 포함된 토큰이 정상적인 토큰인지 검증
        try {
            memberService.verifiedAuthenticatedMember(member.getMemberId());
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }
        Optional<Liked> optionalLiked = likedRepository.findByBoardAndMember(findBoard(boardId), member);
        Liked liked = null;
        if(optionalLiked.isEmpty()){
            liked = new Liked();
            Board findBoard = findBoard(boardId);
            liked.addBoard(findBoard);
            liked.addMember(member);
            findBoard.addLike(liked);
            member.addLikes(liked);
            likedRepository.save(liked);
        }
        else{
            likedRepository.delete(optionalLiked.get());
        }
    }

    public static Specification<Board> withType(String type){
        return (Specification<Board>) ((root, query, builder) ->
                builder.equal(root.get("type"),type));
    }

    public static Specification<Liked> withMemberId(long memberId){
        return (Specification<Liked>) ((root, query, builder) ->
                builder.equal(root.get("memberId"),memberId));
    }

    public static Slice<Board> convertToSlice(Page<Board> page){
        return new SliceImpl<>(page.getContent(), page.getPageable(), page.hasNext());
    }
}
