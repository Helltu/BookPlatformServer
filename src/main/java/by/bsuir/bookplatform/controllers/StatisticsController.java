package by.bsuir.bookplatform.controllers;

import by.bsuir.bookplatform.DTO.stats.*;
import by.bsuir.bookplatform.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/stats")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatisticsController {

    private final UserOrderService userOrderService;
    private final GenreService genreService;
    private final ReviewService reviewService;
    private final OrderBookService orderBookService;
    private final BookService bookService;

    @GetMapping("/orders-by-month")
    public List<MonthlyOrderStatsDTO> getOrdersByMonth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return userOrderService.getMonthlyOrderStats(from, to);
    }

    @GetMapping("/orders-by-status")
    public List<OrderStatusCountDTO> getOrdersByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return userOrderService.getOrderStatusCounts(from, to);
    }

    @GetMapping("/books-per-genre")
    public List<GenreBookCountDTO> getBooksPerGenre() {
        return genreService.getBooksPerGenre();
    }

    @GetMapping("/book-ratings")
    public List<BookRatingDTO> getBookRatings() {
        return reviewService.getBookAverageRatings();
    }

    @GetMapping("/genre-ratings")
    public List<GenreRatingDTO> getGenreRatings() {
        return genreService.getGenreAverageRatings();
    }

    @GetMapping("/book-popularity")
    public List<BookPopularityDTO> getBookPopularity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return orderBookService.getBookPopularityStats(from, to);
    }

    @GetMapping("/book-review-counts")
    public List<BookReviewCountDTO> getBookReviewCounts() {
        return reviewService.getBookReviewCounts();
    }

    @GetMapping("/publisher-book-counts")
    public List<PublisherBookCountDTO> getPublisherBookCounts() {
        return bookService.getPublisherBookCounts();
    }

    @GetMapping("/author-book-counts")
    public List<AuthorBookCountDTO> getAuthorBookCounts() {
        return bookService.getAuthorBookCounts();
    }

    @GetMapping("/genre-popularity")
    public List<GenrePopularityDTO> getGenrePopularity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return orderBookService.getGenrePopularityStats(from, to);
    }

    @GetMapping("/author-popularity")
    public List<AuthorPopularityDTO> getAuthorPopularity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return orderBookService.getAuthorPopularityStats(from, to);
    }
}
