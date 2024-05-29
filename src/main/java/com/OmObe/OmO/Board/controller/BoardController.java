package com.OmObe.OmO.Board.controller;

import com.OmObe.OmO.Board.dto.BoardDto;
import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.mapper.BoardMapper;
import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.Board.service.BoardService;
import com.OmObe.OmO.Liked.repository.LikedRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.OmObe.OmO.member.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final BoardMapper mapper;
    private final MemberRepository memberRepository;
    private final LikedRepository likedRepository;
    private final TokenDecryption tokenDecryption;

    public BoardController(BoardService boardService, BoardMapper mapper, MemberRepository memberRepository, LikedRepository likedRepository, TokenDecryption tokenDecryption) {
        this.boardService = boardService;
        this.mapper = mapper;
        this.memberRepository = memberRepository;
        this.likedRepository = likedRepository;
        this.tokenDecryption = tokenDecryption;
    }

    /*
*   Subject : email
*   Claim : email
*
*
* */

    // TODO : JWT 서비스 시에 실행할 것.
    @SneakyThrows
    @PostMapping("/write")
    public ResponseEntity postBoard(@Valid @RequestBody BoardDto.Post postDto,
                                    @RequestHeader("Authorization") String token){
        Board board = mapper.boardPostDtoToBoard(postDto);
//        Member writer = tokenDecryption.getWriterInJWTToken(token);
//        board.setMember(writer);

        Board createdBoard = boardService.createBoard(board, token);
        BoardDto.Response response = mapper.boardToBoardResponseDto(createdBoard);

        // 게시판 글 작성 시 사용자의 프로필 이미지 설정
        response.setProfileURL(createdBoard.getMember().getProfileImageUrl());
        return new ResponseEntity<>(response,
                HttpStatus.CREATED);
    }

    @SneakyThrows
    @PatchMapping("/modification/{board-id}")
    public ResponseEntity patchBoard(@Valid @RequestBody BoardDto.Patch patchDto,
                                     @PathVariable("board-id") @Positive long boardId,
                                     @RequestHeader("Authorization") String token){
        patchDto.setBoardId(boardId);

        Board board = mapper.boardPatchDtoToBoard(patchDto);
        Board response = boardService.updateBoard(patchDto, token);

        return new ResponseEntity<>(mapper.boardToBoardResponseDto(response),
                HttpStatus.OK);
    }

    @GetMapping("/{board-id}")
    public ResponseEntity getBoard(@PathVariable("board-id") @Positive long boardId,
                                   @RequestHeader("Authorization") @Nullable String token){
        Board board = boardService.getBoard(boardId);

        return new ResponseEntity<>(mapper.boardToBoardResponseDto(board,token),
                HttpStatus.OK);
    }

    // TODO : size를 고정하고 싶은데 한번 로드할 때 얼마나 할지로 나중에 회의로 정해야겠당.

    // 다음 페이지가 있을 때 다음 페이지 로드 가능
    // 좋아요 순, 최신 순, 댓글 많은 순, 조회수 많은 순
    @GetMapping("/Trouble")
    public ResponseEntity getTroubleBoards(@RequestParam(defaultValue = "1") int page,
                                           @Positive @RequestParam(defaultValue = "10") int size,
                                           @RequestParam String sorting,
                                           @RequestHeader("Authorization") @Nullable String token){
        Slice<Board> pageBoards = null;
            switch (sorting){
                case "createdAt":
                    pageBoards = boardService.findBoardsByCreatedAt("TROUBLE",page-1, size);
                    break;
                case "viewCount":
                    pageBoards = boardService.findBoardsByViewCount("TROUBLE", page-1, size);
                    break;
                case "likes":
                    pageBoards = boardService.findBoardsByLikes("TROUBLE", page-1, size);
                    break;
                case "comments":
                    pageBoards = boardService.findBoardsByComments("TROUBLE", page-1, size);
                    break;
            }
        List<Board> boards = pageBoards.getContent();
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.boardsToBoardResponseDtos(boards,token),pageBoards),HttpStatus.OK);
    }

    @GetMapping("/Free")
    public ResponseEntity getFreeBoards(@RequestParam(defaultValue = "1") int page,
                                           @Positive @RequestParam(defaultValue = "10") int size,
                                           @RequestParam String sorting,
                                        @RequestHeader("Authorization") @Nullable String token){
        Slice<Board> pageBoards = null;
        switch (sorting){
            case "createdAt":
                pageBoards = boardService.findBoardsByCreatedAt("FREE",page-1, size);
                break;
            case "viewCount":
                pageBoards = boardService.findBoardsByViewCount("FREE", page-1, size);
                break;
            case "likes":
                pageBoards = boardService.findBoardsByLikes("FREE", page-1, size);
                break;
            case "comments":
                pageBoards = boardService.findBoardsByComments("FREE", page-1, size);
                break;
        }
        List<Board> boards = pageBoards.getContent();
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.boardsToBoardResponseDtos(boards,token),pageBoards),HttpStatus.OK);
    }

    @GetMapping("/Qna")
    public ResponseEntity getQnaBoards(@RequestParam(defaultValue = "1") int page,
                                        @Positive @RequestParam(defaultValue = "10") int size,
                                        @RequestParam String sorting,
                                       @RequestHeader("Authorization") @Nullable String token){
        Slice<Board> pageBoards = null;
        switch (sorting){
            case "createdAt":
                pageBoards = boardService.findBoardsByCreatedAt("QNA",page-1, size);
                break;
            case "viewCount":
                pageBoards = boardService.findBoardsByViewCount("QNA", page-1, size);
                break;
            case "likes":
                pageBoards = boardService.findBoardsByLikes("QNA", page-1, size);
                break;
            case "comments":
                pageBoards = boardService.findBoardsByComments("QNA", page-1, size);
                break;
        }
        List<Board> boards = pageBoards.getContent();
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.boardsToBoardResponseDtos(boards,token),pageBoards),HttpStatus.OK);
    }

    @GetMapping("/Qna/FAQ")
    public ResponseEntity getFaqBoards(@RequestParam(defaultValue = "1") int page,
                                        @Positive @RequestParam(defaultValue = "10") int size,
                                        @RequestParam String sorting,
                                       @RequestHeader("Authorization") @Nullable String token){
        Slice<Board> pageBoards = null;
        switch (sorting){
            case "createdAt":
                pageBoards = boardService.findBoardsByCreatedAt("FAQ",page-1, size);
                break;
            case "viewCount":
                pageBoards = boardService.findBoardsByViewCount("FAQ", page-1, size);
                break;
            case "likes":
                pageBoards = boardService.findBoardsByLikes("FAQ", page-1, size);
                break;
            case "comments":
                pageBoards = boardService.findBoardsByComments("FAQ", page-1, size);
                break;
        }
        List<Board> boards = pageBoards.getContent();
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.boardsToBoardResponseDtos(boards,token),pageBoards),HttpStatus.OK);
    }

    @DeleteMapping("/{board-id}")
    public ResponseEntity deleteBoard(@PathVariable("board-id") @Positive long boardId,
                                      @RequestHeader("Authorization") String token){
        boardService.deleteBoard(boardId, token);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/like/{boardId}")
    public ResponseEntity likeBoard(@PathVariable("boardId") @Positive long boardId,
                                    @RequestHeader("Authorization") String Token) throws JsonProcessingException {
        Member writer = tokenDecryption.getWriterInJWTToken(Token);

        boardService.likesBoard(boardId, writer);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
