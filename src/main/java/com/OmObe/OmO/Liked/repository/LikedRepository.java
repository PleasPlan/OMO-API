package com.OmObe.OmO.Liked.repository;

import com.OmObe.OmO.Board.entity.Board;
import com.OmObe.OmO.Liked.entity.Liked;
import com.OmObe.OmO.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikedRepository extends JpaRepository<Liked, Long> {
    Optional<Liked> findByBoardAndMember(Board board, Member member);
}
