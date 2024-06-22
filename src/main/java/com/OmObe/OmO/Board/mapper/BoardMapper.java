package com.OmObe.OmO.Board.mapper;

import com.OmObe.OmO.Board.dto.BoardDto;
import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Comment.dto.CommentDto;
import com.OmObe.OmO.Comment.entity.Comment;
import com.OmObe.OmO.Comment.mapper.CommentMapper;
import com.OmObe.OmO.Liked.entity.Liked;
import com.OmObe.OmO.Liked.repository.LikedRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
public class BoardMapper {

    private final CommentMapper commentMapper;
    private final TokenDecryption tokenDecryption;
    private final LikedRepository likedRepository;

    public BoardMapper(CommentMapper commentMapper,
                       TokenDecryption tokenDecryption,
                       LikedRepository likedRepository) {
        this.commentMapper = commentMapper;
        this.tokenDecryption = tokenDecryption;
        this.likedRepository = likedRepository;
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

    public BoardDto.Response boardToBoardResponseDto(Board board,String token){
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
            if(token != null){
                Member loginMember = null;

                loginMember = tokenDecryption.getWriterInJWTToken(token);

                Optional<Liked> optionalLiked = likedRepository.findByBoardAndMember(board,loginMember);
                if(optionalLiked.isPresent()) {
                    myLiked = board.getLikes().contains(optionalLiked.get());
                }
            }


            List<CommentDto.Response> commentResponseDtos = new ArrayList<>();
            for(Comment c : board.getComments()){
                CommentDto.Response commentDto = commentMapper.commentToCommentResponseDto(c);
                commentResponseDtos.add(commentDto);
            }

            BoardDto.Response response = new BoardDto.Response(boardId,title,content,type,writer,profileURL,createdTime,likeCount,viewCount,myLiked,commentResponseDtos);
            return response;
        }
    }

    public List<BoardDto.Response> boardsToBoardResponseDtos(List<Board> boards,String token){
        if(boards == null){
            return null;
        } else {
            List<BoardDto.Response> responses = new ArrayList<>();
            /*Iterator iterator = boards.iterator();
            while(iterator.hasNext()){
                Board board = (Board) iterator.next();
                responses.add(this.boardToBoardResponseDto(board));
            }*/
            for(Board board:boards){
                responses.add(this.boardToBoardResponseDto(board,token));
            }
            return responses;
        }
    }
}
