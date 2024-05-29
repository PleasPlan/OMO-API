package com.OmObe.OmO.Board.mapper;

import com.OmObe.OmO.Board.dto.BoardDto;
import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Comment.dto.CommentDto;
import com.OmObe.OmO.Comment.entity.Comment;
import com.OmObe.OmO.Comment.mapper.CommentMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class BoardMapper {

    private final CommentMapper commentMapper;

    public BoardMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public Board boardPostDtoToBoard(BoardDto.Post postDto){
        if(postDto == null){
            return null;
        }
        else{
            Board board = new Board();
            board.setTitle(postDto.getTitle());
            board.setContent(postDto.getContent());
            board.setType(postDto.getType());
            return board;
        }
    }

    public Board boardPatchDtoToBoard(BoardDto.Patch patchDto){
        if(patchDto == null){
            return null;
        } else {
            Board board = new Board();
            board.setBoardId(patchDto.getBoardId());
            board.setTitle(patchDto.getTitle());
            board.setContent(patchDto.getContent());
            return board;
        }
    }

    public BoardDto.Response boardToBoardResponseDto(Board board){
        if(board == null){
            return null;
        } else {
            long boardId = board.getBoardId();
            String type = board.getType();
            String title = board.getTitle();
            String content = board.getContent();
            String writer = board.getMember().getNickname();
//            TODO : 프로필 url이 주석 해제되면 다시 해제할 것.
            String profileURL = board.getMember().getProfileImageUrl();
            LocalDateTime createdTime = board.getCreatedAt();
            int likeCount = board.getLikes().size();
            int viewCount = board.getViewCount();

            boolean myLiked = false;

            List<CommentDto.Response> commentResponseDtos = new ArrayList<>();
            for(Comment c : board.getComments()){
                CommentDto.Response commentDto = commentMapper.commentToCommentResponseDto(c);
                commentResponseDtos.add(commentDto);
            }

            BoardDto.Response response = new BoardDto.Response(boardId,title,content,type,writer,profileURL,createdTime,likeCount,viewCount,myLiked,commentResponseDtos);
            return response;
        }
    }

    public List<BoardDto.Response> boardsToBoardResponseDtos(List<Board> boards){
        if(boards == null){
            return null;
        }  else {
            List<BoardDto.Response> responses = new ArrayList<>();
            /*Iterator iterator = boards.iterator();
            while(iterator.hasNext()){
                Board board = (Board) iterator.next();
                responses.add(this.boardToBoardResponseDto(board));
            }*/
            for(Board board:boards){
                responses.add(this.boardToBoardResponseDto(board));
            }
            return responses;
        }
    }
}
