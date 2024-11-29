package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.ReviewDTO;
import by.bsuir.bookplatform.DTO.UserDTO;
import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.entities.ReviewId;
import by.bsuir.bookplatform.entities.User;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookService bookService;
    private final UserService userService;

    public List<ReviewDTO> getAllReviewsDTO() {
        return reviewRepository.findAll().stream().map(review -> {
            ReviewDTO reviewDTO = new ReviewDTO(review);
            reviewDTO.setUserName(userService.getUserById(reviewDTO.getUserId()).getSurname() + " " + userService.getUserById(reviewDTO.getUserId()).getName());
            return reviewDTO;
        }).toList();
    }

    public ReviewDTO getReviewDTOById(Long bookId, Long userId) {
        Review review = getReviewById(bookId, userId);
        if(review == null)
            return null;

        ReviewDTO reviewDTO = new ReviewDTO(getReviewById(bookId, userId));
        reviewDTO.setUserName(userService.getUserById(reviewDTO.getUserId()).getSurname() + " " + userService.getUserById(reviewDTO.getUserId()).getName());
        return reviewDTO;
    }

    public List<ReviewDTO> getBookReviewsWithUserNames(Long bookId, Long excludeUserId) {
        List<Review> reviews = bookService.getBookReviews(bookId).stream().toList();
        List<ReviewDTO> reviewDTOs = new ArrayList<>();

        for (Review review : reviews) {
            if (excludeUserId != null && review.getId().getUserId().equals(excludeUserId)) {
                continue;
            }
            UserDTO userDTO = userService.getUserDTOById(review.getId().getUserId());
            ReviewDTO reviewDTO = new ReviewDTO(review);
            reviewDTO.setUserName(userDTO.getName() + " " + userDTO.getSurname());
            reviewDTOs.add(reviewDTO);
        }
        return reviewDTOs;
    }


    public Review getReviewById(Long bookId, Long userId) {
        Book book = bookService.getBookById(bookId);
        User user = userService.getUserById(userId);

        Optional<Review> reviewOpt = book.getReviews().stream().filter(r -> r.getUser().equals(user)).findFirst();

        return reviewOpt.orElse(null);
    }

    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        reviewDTO.checkValues();

        Optional<Review> reviewOpt = reviewRepository.findById(new ReviewId(reviewDTO.getBookId(), reviewDTO.getUserId()));

        if (reviewOpt.isPresent())
            throw new AppException("In book " + reviewDTO.getBookId() + " review by user " + reviewDTO.getUserId() + " already exists.", HttpStatus.NOT_FOUND);

        Review review = new Review();
        review = DTOMapper.getInstance().map(reviewDTO, review.getClass());

        review = reviewRepository.save(review);

        reviewDTO = new ReviewDTO(review);
        reviewDTO.setUserName(userService.getUserById(reviewDTO.getUserId()).getSurname() + " " + userService.getUserById(reviewDTO.getUserId()).getName());
        return reviewDTO;
    }

    public ReviewDTO editReview(ReviewDTO reviewDetailsDTO) {
        reviewDetailsDTO.checkValues();

        Review existingReview = getReviewById(reviewDetailsDTO.getBookId(), reviewDetailsDTO.getUserId());

        if (reviewDetailsDTO.getRating() != null)
            existingReview.setRating(reviewDetailsDTO.getRating());

        if (reviewDetailsDTO.getText() != null)
            existingReview.setText(reviewDetailsDTO.getText());

        existingReview = reviewRepository.save(existingReview);

        ReviewDTO reviewDTO = new ReviewDTO(existingReview);
        reviewDTO.setUserName(userService.getUserById(reviewDTO.getUserId()).getSurname() + " " + userService.getUserById(reviewDTO.getUserId()).getName());
        return reviewDTO;
    }


    public void deleteReviewById(Long bookId, Long userId) {
        Review review = getReviewById(bookId, userId);

        reviewRepository.deleteById(review.getId());
    }
}