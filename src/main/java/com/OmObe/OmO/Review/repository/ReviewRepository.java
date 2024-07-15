package com.OmObe.OmO.Review.repository;

import com.OmObe.OmO.Review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review>{
    Optional<List<Review>> findByPlaceId(long placeId);
    Optional<Review> findByMemberAndPlaceId(long memberId, long placeId);
}
