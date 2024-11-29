package by.bsuir.bookplatform.repositories;

import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.entities.ReviewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, ReviewId> {
}
