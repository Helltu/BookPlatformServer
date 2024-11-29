package by.bsuir.bookplatform.controllers;

import by.bsuir.bookplatform.DTO.*;
import by.bsuir.bookplatform.config.UserAuthProvider;
import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/general")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneralController {
    private final UserService userService;
    private final BookService bookService;
    private final GenreService genreService;
    private final UserAuthProvider userAuthProvider;
    private final ReviewService reviewService;

    @GetMapping("/login")
    public UserDTO login(@RequestParam String email, @RequestParam String password) {
        UserDTO credentials = new UserDTO();
        credentials.setEmail(email);
        credentials.setPassword(password);
        UserDTO user = userService.login(credentials);
        user.setToken(userAuthProvider.createToken(user.getEmail(), user.getIsAdmin()));
        return user;
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody UserDTO credentials) {
        UserDTO user = userService.register(credentials);
        user.setToken(userAuthProvider.createToken(user.getEmail(), user.getIsAdmin()));
        return user;
    }

    @GetMapping("/user/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserDTOById(id);
        userDTO.setPassword(null);
        return userDTO;
    }

    @PutMapping("/user/{id}")
    public UserDTO editUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.editUser(id, userDTO);
    }

    @GetMapping("/books")
    public List<BookDTO> getAllBooks() {
        return bookService.getAllBooksDTO();
    }

    @GetMapping("/books/{id}")
    public BookDTO getBookById(@PathVariable Long id) {
        return bookService.getBookDTOById(id);
    }

    @GetMapping("books/filtered")
    public List<BookDTO> getFilteredBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) List<String> authors,
            @RequestParam(required = false) List<String> publishers,
            @RequestParam(required = false) Integer minPublicationYear,
            @RequestParam(required = false) Integer maxPublicationYear,
            @RequestParam(required = false) Float minCost,
            @RequestParam(required = false) Float maxCost,
            @RequestParam(required = false) Boolean hardcover,
            @RequestParam(required = false) Integer minPages,
            @RequestParam(required = false) Integer maxPages,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection
    ) {

        FilterBooksDTO filterBooksDTO = new FilterBooksDTO(title, authors, publishers, minPublicationYear, maxPublicationYear, minCost, maxCost, hardcover, minPages, maxPages, minRating, maxRating, genres, sortBy, sortDirection);

        return bookService.filterBooks(filterBooksDTO);
    }

    @GetMapping("/books/filters")
    public FilterBooksDTO getFilters() {
        FilterBooksDTO filterDataDTO = new FilterBooksDTO();

        filterDataDTO.setMinCost(bookService.getMinBookCost());
        filterDataDTO.setMaxCost(bookService.getMaxBookCost());
        filterDataDTO.setMinPublicationYear(bookService.getMinBookPublicationYear());
        filterDataDTO.setMaxPublicationYear(bookService.getMaxBookPublicationYear());
        filterDataDTO.setMinPages(bookService.getMinBookPages());
        filterDataDTO.setMaxPages(bookService.getMaxBookPages());
        filterDataDTO.setMinRating(bookService.getMinBookRating());
        filterDataDTO.setMaxRating(bookService.getMaxBookRating());

        filterDataDTO.setAuthors(bookService.getAllAuthors().stream().toList());
        filterDataDTO.setPublishers(bookService.getAllPublishers().stream().toList());
        filterDataDTO.setGenres(genreService.getAllGenresDTO().stream().map(GenreDTO::getName).collect(Collectors.toList()));

        return filterDataDTO;
    }

    @GetMapping("/books/{id}/details")
    public BookDetailsDTO getBookDetails(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        Book book = bookService.getBookById(id);
        List<GenreDTO> genres = bookService.getBookGenres(id).stream().toList();
        List<MediaDTO> media = bookService.getBookMedia(id).stream().toList();
        Float rating = bookService.getBookAverageRatingById(id);

        List<ReviewDTO> reviews = reviewService.getBookReviewsWithUserNames(id, userId);

        ReviewDTO userReview = null;
        if (userId != null) {
            userReview = reviewService.getReviewDTOById(id, userId);
        }

        return new BookDetailsDTO(new BookDTO(book), genres, media, rating, reviews, userReview);
    }

    @GetMapping("/genres")
    public List<GenreDTO> getAllGenres() {
        return genreService.getAllGenresDTO();
    }

    @GetMapping("/books/{id}/genres")
    public Set<GenreDTO> getBookGenres(@PathVariable Long id) {
        return bookService.getBookGenres(id);
    }

    @GetMapping("/books/{id}/media")
    public Set<MediaDTO> getBookMedia(@PathVariable Long id) {
        return bookService.getBookMedia(id);
    }

    @GetMapping("/books/{id}/rating")
    public Float getBookRating(@PathVariable Long id) {
        return bookService.getBookAverageRatingById(id);
    }

    @GetMapping("/books/{id}/reviews")
    public Set<ReviewDTO> getBookReviews(@PathVariable Long id) {
        return bookService.getBookReviewsDTO(id);
    }

    @DeleteMapping("/reviews")
    public void deleteBookReview(@RequestParam Long bookId, @RequestParam Long userId) {
        reviewService.deleteReviewById(bookId, userId);
    }
}
