package com.OmObe.OmO.Comment.controller;

import com.OmObe.OmO.Board.repository.BoardRepository;
import com.OmObe.OmO.Board.response.MultiResponseDto;
import com.OmObe.OmO.Comment.dto.CommentDto;
import com.OmObe.OmO.Comment.entity.Comment;
import com.OmObe.OmO.Comment.mapper.CommentMapper;
import com.OmObe.OmO.Comment.service.CommentService;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Slf4j
@Validated
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper mapper;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public CommentController(CommentService commentService,
                             CommentMapper mapper,
                             BoardRepository boardRepository,
                             MemberRepository memberRepository) {
        this.commentService = commentService;
        this.mapper = mapper;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    // TODO: USER만 가능
    @SneakyThrows
    @PostMapping("/write")
    public ResponseEntity postComment(@Valid @RequestBody CommentDto.Post postDto,
                                      @RequestHeader("Authorization") String token){
        Comment comment = mapper.commentPostToComment(postDto);
//        Member writer = getWriterInJWTToken(Token);
//        comment.setMember(writer);

        Comment response = commentService.createComment(comment, token);
        return new ResponseEntity<>(mapper.commentToCommentResponseDto(response),
                HttpStatus.CREATED);
    }

    // TODO : JWt 서비스 시에 삭제할 것(완)
    /*@PostMapping("/write")
    public ResponseEntity postComment(@Valid @RequestBody CommentDto.Post postDto,
                                      @RequestParam Long memberId){
        Comment comment = mapper.commentPostToComment(postDto);
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member findMember = optionalMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        comment.setMember(findMember);
        Comment response = commentService.createComment(comment);
        return new ResponseEntity<>(mapper.commentToCommentResponseDto(response),
                HttpStatus.CREATED);
    }*/

    // TODO: USER만 가능
    @PatchMapping("/modification/{comment-id}")
    public ResponseEntity patchComment(@Valid @RequestBody CommentDto.Patch patchDto,
                                       @PathVariable("comment-id") @Positive long commentId,
                                       @RequestHeader("Authorization") String token){
        patchDto.setCommentId(commentId);

        Comment comment = mapper.commentPatchDtoToComment(patchDto);
        Comment response = commentService.updateComment(comment, token);

        return new ResponseEntity<>(mapper.commentToCommentResponseDto(response),
                HttpStatus.OK);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity getComments(@RequestParam(defaultValue = "1") int page,
                                      @Positive @RequestParam(defaultValue = "10") int size,
                                      @PathVariable("boardId") long boardId){
        Slice<Comment> pageComments = commentService.findComments(page-1, size, boardId);

        List<Comment> comments = pageComments.getContent();
        return new ResponseEntity<>(
                new MultiResponseDto<>(mapper.commentsToCommentResponseDtos(comments),pageComments),HttpStatus.OK);
    }

    // TODO: USER만 가능
    @DeleteMapping("/{comment-Id}")
    public ResponseEntity deleteComment(@PathVariable("comment-Id") @Positive long commentId,
                                        @RequestHeader("Authorization") String token){
        commentService.deleteComment(commentId, token);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Member getWriterInJWTToken(String token) throws JsonProcessingException {
        String[] chunks = token.split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> returnMap = objectMapper.readValue(payload, Map.class);

        Object objectWriter = returnMap.get("sub");
        String email = objectWriter.toString();

        Optional<Member> member = memberRepository.findByEmail(email);
        Member writer = member.orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return writer;
    }
}
