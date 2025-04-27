package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.ReviewDTO;
import by.bsuir.bookplatform.DTO.UserDTO;
import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.entities.ReviewId;
import by.bsuir.bookplatform.entities.User;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repositories.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllReviewsDTO() {
        Review review1 = new Review();
        review1.setId(new ReviewId(1L, 1L));
        review1.setRating(5);
        Review review2 = new Review();
        review2.setId(new ReviewId(2L, 1L));
        review2.setRating(4);
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review1, review2));
        when(userService.getUserById(anyLong())).thenReturn(new User());
        List<ReviewDTO> result = reviewService.getAllReviewsDTO();
        assertEquals(2, result.size());
    }

    @Test
    void testGetReviewById_Found() {
        Long bookId = 1L;
        Long userId = 1L;

        Book book = new Book();
        book.setId(bookId);
        book.setReviews(new HashSet<>());

        User user = new User();
        user.setId(userId);

        ReviewId reviewId = new ReviewId(bookId, userId);
        Review review = new Review();
        review.setId(reviewId);
        review.setBook(book);
        review.setUser(user);

        book.getReviews().add(review);

        when(bookService.getBookById(bookId)).thenReturn(book);
        when(userService.getUserById(userId)).thenReturn(user);

        Review result = reviewService.getReviewById(bookId, userId);
        assertNotNull(result);
        assertEquals(reviewId, result.getId());
    }

    @Test
    void testGetReviewById_NotFound() {
        Long bookId = 1L;
        Long userId = 1L;

        Book book = new Book();
        book.setId(bookId);
        book.setReviews(new HashSet<>());

        User user = new User();
        user.setId(userId);

        when(bookService.getBookById(bookId)).thenReturn(book);
        when(userService.getUserById(userId)).thenReturn(user);

        Review result = reviewService.getReviewById(bookId, userId);
        assertNull(result);
    }

    @Test
    void testCreateReview_Success() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBookId(1L);
        reviewDTO.setUserId(1L);
        reviewDTO.setRating(5);

        Review review = new Review();
        review.setId(new ReviewId(1L, 1L));
        review.setRating(5);

        when(reviewRepository.findById(any(ReviewId.class))).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(userService.getUserById(anyLong())).thenReturn(new User());

        ReviewDTO result = reviewService.createReview(reviewDTO);
        assertNotNull(result);
        assertEquals(5, result.getRating());
    }

    @Test
    void testCreateReview_AlreadyExists() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBookId(1L);
        reviewDTO.setUserId(1L);
        reviewDTO.setRating(5);

        Review existingReview = new Review();
        existingReview.setId(new ReviewId(1L, 1L));

        when(reviewRepository.findById(any(ReviewId.class))).thenReturn(Optional.of(existingReview));

        AppException exception = assertThrows(AppException.class, () -> reviewService.createReview(reviewDTO));
        assertEquals("In book 1 review by user 1 already exists.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testEditReview_Success() {
        Long bookId = 1L;
        Long userId = 1L;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBookId(bookId);
        reviewDTO.setUserId(userId);
        reviewDTO.setRating(4);
        reviewDTO.setText("Updated review text");

        Book book = new Book();
        book.setId(bookId);
        book.setReviews(new HashSet<>());

        User user = new User();
        user.setId(userId);

        ReviewId reviewId = new ReviewId(bookId, userId);
        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setBook(book);
        existingReview.setUser(user);
        existingReview.setRating(5);
        existingReview.setText("Old review text");

        book.getReviews().add(existingReview);

        when(bookService.getBookById(bookId)).thenReturn(book);
        when(userService.getUserById(userId)).thenReturn(user);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDTO result = reviewService.editReview(reviewDTO);

        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Updated review text", result.getText());
    }

    @Test
    void testDeleteReviewById_Success() {
        Long bookId = 1L;
        Long userId = 1L;

        Book book = new Book();
        book.setId(bookId);
        book.setReviews(new HashSet<>());

        User user = new User();
        user.setId(userId);

        ReviewId reviewId = new ReviewId(bookId, userId);
        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setBook(book);
        existingReview.setUser(user);

        book.getReviews().add(existingReview);

        when(bookService.getBookById(bookId)).thenReturn(book);
        when(userService.getUserById(userId)).thenReturn(user);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        reviewService.deleteReviewById(bookId, userId);

        verify(reviewRepository, times(1)).deleteById(reviewId);
    }

}
