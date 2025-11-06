package org.lab.dev.repository;

import org.lab.dev.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("Select p.reviews from Product p where p.id = ?1")
    List<Review> findReviewByProductId(Long id);
}
