package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.entities.ReviewId;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.BookRepository;
import by.bsuir.bookplatform.repository.ReviewRepository;
import by.bsuir.bookplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findReviewById(ReviewId reviewId) {
        return reviewRepository.findById(reviewId);
    }

    public Review saveReview(Review review) {
        if (!userRepository.existsById(review.getUser().getId())) {
            throw new AppException("User not found.", HttpStatus.NOT_FOUND);
        }
        if (!bookRepository.existsById(review.getBook().getId())) {
            throw new AppException("Book not found.", HttpStatus.NOT_FOUND);
        }

        ReviewId reviewId = new ReviewId(review.getBook().getId(), review.getUser().getId());
        if (reviewRepository.existsById(reviewId)) {
            throw new AppException("A review for this book has already been submitted by the user.", HttpStatus.CONFLICT);
        }

        return reviewRepository.save(review);
    }

    public Review editReview(ReviewId id, Review reviewDetails) {
        Optional<Review> existingReviewOpt = reviewRepository.findById(id);
        if (existingReviewOpt.isEmpty()) {
            throw new AppException("Review with id " + id + " not found.", HttpStatus.NOT_FOUND);
        }

        Review existingReview = existingReviewOpt.get();

        existingReview.setRating(reviewDetails.getRating());
        existingReview.setText(reviewDetails.getText());

        return reviewRepository.save(existingReview);
    }

    public void deleteReviewById(ReviewId reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}