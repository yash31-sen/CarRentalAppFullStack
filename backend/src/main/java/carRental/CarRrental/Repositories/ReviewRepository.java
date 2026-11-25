package carRental.CarRrental.Repositories;

import carRental.CarRrental.Models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer WHERE r.targetUser.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByTargetUserIdWithReviewer(@Param("userId") Long userId);
}
