package com.OmObe.OmO.Comment.mapper;

import com.OmObe.OmO.Board.dto.BoardDto;
import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Board.repository.BoardRepository;
import com.OmObe.OmO.Comment.dto.CommentDto;
import com.OmObe.OmO.Comment.entity.Comment;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
public class CommentMapper {

    private final BoardRepository boardRepository;

    public CommentMapper(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Comment commentPostToComment(CommentDto.Post postDto){
        if (postDto == null){
            return null;
        } else {
            Comment comment = new Comment();
            Optional<Board> boardOptional = boardRepository.findById(postDto.getBoardId());
            Board board = boardOptional.orElseThrow(() ->
                    new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
            String content = postDto.getContent();

            comment.setContent(content);
            board.addComment(comment);
            return comment;
        }
    }

    public Comment commentPatchDtoToComment(CommentDto.Patch patchDto){
        if(patchDto == null){
            return null;
        } else {
            Comment comment = new Comment();
            comment.setCommentId(patchDto.getCommentId());
            comment.setContent(patchDto.getContent());
            return comment;
        }
    }

    public CommentDto.Response commentToCommentResponseDto(Comment comment){
        if(comment == null){
            return null;
        } else {
            long commentId = comment.getCommentId();
            String writer = comment.getMember().getNickname();
//            TODO : 프로필 url이 주석 해제되면 다시 해제할 것.
//            String profileURL = comment.getMember().getProfileURL();
            String content = comment.getContent();
            CommentDto.Response response = new CommentDto.Response();
            response.setCommentId(commentId);
            response.setWriter(writer);
            response.setContent(content);
//            TODO : 프로필 url이 주석 해제되면 다시 해제할 것.
//            response.setProfileURL(profileURL);
            response.setCreatedAt(comment.getCreatedAt());
            response.setModifiedAt(comment.getModifiedAt());
            return response;
        }
    }

    public List<CommentDto.Response> commentsToCommentResponseDtos(List<Comment> comments){
        if(comments == null){
            return null;
        } else {
            List<CommentDto.Response> responses = new ArrayList<>();
            /*Iterator iterator = comments.iterator();
            while(iterator.hasNext()){
                Comment comment = (Comment) iterator.next();
                responses.add(this.commentToCommentResponseDto(comment));
            }*/
            for(Comment comment:comments){
                responses.add(this.commentToCommentResponseDto(comment));
            }
            return responses;
        }
    }
}
