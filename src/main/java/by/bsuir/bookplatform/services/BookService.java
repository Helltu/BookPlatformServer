package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.*;
import by.bsuir.bookplatform.DTO.stats.AuthorBookCountDTO;
import by.bsuir.bookplatform.DTO.stats.PublisherBookCountDTO;
import by.bsuir.bookplatform.entities.*;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookService {
    private final BookRepository bookRepository;
    private final GenreService genreService;
    private final MediaService mediaService;
    private final UserService userService;

    public List<BookDTO> getAllBooksDTO() {
        return bookRepository.findAll().stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
    }

    public BookDTO getBookDTOByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new AppException("Название книги не может быть пустым.", HttpStatus.BAD_REQUEST);
        }

        List<Book> books = bookRepository.findAll();

        List<Book> filteredBooks = books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title.trim()))
                .collect(Collectors.toList());

        if (filteredBooks.isEmpty()) {
            throw new AppException("Книга с названием \"" + title + "\" не найдена.", HttpStatus.NOT_FOUND);
        }

        if (filteredBooks.size() > 1) {
            throw new AppException("Найдено несколько книг с названием \"" + title + "\". Уточните запрос.", HttpStatus.BAD_REQUEST);
        }

        return new BookDTO(filteredBooks.get(0));
    }


    public Set<String> getAllAuthors() {
        Set<String> authors = bookRepository.findAllDistinctAuthors();
        return authors != null ? authors : Collections.emptySet();
    }

    public Set<String> getAllPublishers() {
        Set<String> publishers = bookRepository.findAllDistinctPublishers();
        return publishers != null ? publishers : Collections.emptySet();
    }

    public Float getMaxBookCost() {
        Float maxCost = bookRepository.findMaxBookCost();
        return maxCost != null ? maxCost : 0.0f;
    }

    public Float getMinBookCost() {
        Float minCost = bookRepository.findMinBookCost();
        return minCost != null ? minCost : 0.0f;
    }

    public Integer getMaxBookRating() {
        Float maxRating = bookRepository.findMaxAverageRating();
        return maxRating != null ? Math.round(maxRating) : 0;
    }

    public Integer getMinBookRating() {
        Float minRating = bookRepository.findMinAverageRating();
        return minRating != null ? Math.round(minRating) : 0;
    }

    public Integer getMaxBookPublicationYear() {
        Integer maxYear = bookRepository.findMaxPublicationYear();
        return maxYear != null ? maxYear : LocalDate.now().getYear();
    }

    public Integer getMinBookPublicationYear() {
        Integer minYear = bookRepository.findMinPublicationYear();
        return minYear != null ? minYear : 1900;
    }

    public Integer getMaxBookPages() {
        Integer maxPages = bookRepository.findMaxPages();
        return maxPages != null ? maxPages : 0;
    }

    public Integer getMinBookPages() {
        Integer minPages = bookRepository.findMinPages();
        return minPages != null ? minPages : 0;
    }

    public BookDTO getBookDTOById(Long id) {
        Book book = getBookById(id);
        return new BookDTO(book);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new AppException("Книга с ID " + id + " не найдена.", HttpStatus.NOT_FOUND));
    }

    public Set<GenreDTO> getBookGenres(Long id) {
        return getBookById(id).getGenres().stream()
                .map(GenreDTO::new)
                .collect(Collectors.toSet());
    }

    public Set<MediaDTO> getBookMedia(Long id) {
        return getBookById(id).getMedia().stream()
                .map(MediaDTO::new)
                .collect(Collectors.toSet());
    }

    public Set<ReviewDTO> getBookReviewsDTO(Long id) {
        return getBookById(id).getReviews().stream()
                .map(review -> {
                    ReviewDTO reviewDTO = new ReviewDTO(review);
                    User user = userService.getUserById(reviewDTO.getUserId());
                    reviewDTO.setUserName(user.getSurname() + " " + user.getName());
                    return reviewDTO;
                })
                .collect(Collectors.toSet());
    }

    public Set<Review> getBookReviews(Long id) {
        return getBookById(id).getReviews();
    }

    public List<BookDTO> filterBooks(FilterBooksDTO filterBooksDTO) {
        filterBooksDTO.checkValues();

        Specification<Book> spec = Specification.where(BookRepository.hasTitle(filterBooksDTO.getTitle()))
                .and(BookRepository.hasAuthors(filterBooksDTO.getAuthors()))
                .and(BookRepository.hasPublishers(filterBooksDTO.getPublishers()))
                .and(BookRepository.hasPublicationYearLaterThan(filterBooksDTO.getMinPublicationYear()))
                .and(BookRepository.hasPublicationYearBeforeThan(filterBooksDTO.getMaxPublicationYear()))
                .and(BookRepository.hasHardcover(filterBooksDTO.getHardcover()))
                .and(BookRepository.costsLessThan(filterBooksDTO.getMaxCost()))
                .and(BookRepository.costsMoreThan(filterBooksDTO.getMinCost()))
                .and(BookRepository.hasLessPagesThan(filterBooksDTO.getMaxPages()))
                .and(BookRepository.hasMorePagesThan(filterBooksDTO.getMinPages()))
                .and(BookRepository.hasAverageRatingLessThan(filterBooksDTO.getMaxRating()))
                .and(BookRepository.hasAverageRatingGreaterThan(filterBooksDTO.getMinRating()))
                .and(BookRepository.hasGenres(filterBooksDTO.getGenres()));

        if (filterBooksDTO.getSortBy() != null) {
            Sort.Direction direction = "desc".equalsIgnoreCase(filterBooksDTO.getSortDirection())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            switch (filterBooksDTO.getSortBy()) {
                case "title":
                    spec = spec.and((root, query, cb) -> {
                        query.orderBy(direction == Sort.Direction.DESC
                                ? cb.desc(root.get("title"))
                                : cb.asc(root.get("title")));
                        return cb.isTrue(cb.literal(true));
                    });
                    break;
                case "publicationYear":
                    spec = spec.and((root, query, cb) -> {
                        query.orderBy(direction == Sort.Direction.DESC
                                ? cb.desc(root.get("publicationYear"))
                                : cb.asc(root.get("publicationYear")));
                        return cb.isTrue(cb.literal(true));
                    });
                    break;
                case "cost":
                    spec = spec.and((root, query, cb) -> {
                        query.orderBy(direction == Sort.Direction.DESC
                                ? cb.desc(root.get("cost"))
                                : cb.asc(root.get("cost")));
                        return cb.isTrue(cb.literal(true));
                    });
                    break;
                case "rating":
                    spec = spec.and(BookRepository.orderByAverageRating(direction));
                    break;
                case "popularity":
                    spec = spec.and(BookRepository.orderByPopularity(direction));
                    break;
                default:
                    throw new AppException("Некорректный параметр сортировки.", HttpStatus.BAD_REQUEST);
            }
        }

        return bookRepository.findAll(spec).stream()
                .map(BookDTO::new)
                .collect(Collectors.toList());
    }

    public BookDTO createBook(BookDTO bookDTO) {
        bookDTO.checkValues();

        if (bookExists(bookDTO, null)) {
            throw new AppException("Книга с такими данными уже существует.", HttpStatus.CONFLICT);
        }

        Book book = DTOMapper.getInstance().map(bookDTO, Book.class);
        book = bookRepository.save(book);

        Book finalBook = book;
        if (bookDTO.getGenreIds() != null) {
            bookDTO.getGenreIds().forEach(genreId -> addGenreToBook(finalBook.getId(), genreId));
        }

        if (bookDTO.getMedia() != null) {
            bookDTO.getMedia().forEach(media -> {
                MediaDTO mediaDTO = new MediaDTO();
                mediaDTO.setMedia(media);
                mediaDTO.setBookId(finalBook.getId());
                mediaService.createMedia(mediaDTO);
            });
        }

        return new BookDTO(book);
    }

    public BookDTO addMediaToBook(Long bookId, MediaDTO mediaDTO) {
        mediaDTO.checkValues();

        Book book = getBookById(bookId);
        mediaDTO.setBookId(bookId);
        mediaService.createMedia(mediaDTO);

        return new BookDTO(book);
    }

    public BookDTO addGenreToBook(Long bookId, Long genreId) {
        Book book = getBookById(bookId);
        Genre genre = genreService.getGenreById(genreId);

        if (book.getGenres().contains(genre)) {
            throw new AppException("Книга уже содержит данный жанр.", HttpStatus.CONFLICT);
        }

        book.getGenres().add(genre);
        bookRepository.save(book);

        return new BookDTO(book);
    }

    public BookDTO deleteGenreFromBook(Long bookId, Long genreId) {
        Book book = getBookById(bookId);
        Genre genre = genreService.getGenreById(genreId);

        if (!book.getGenres().contains(genre)) {
            throw new AppException("Книга не содержит данный жанр.", HttpStatus.CONFLICT);
        }

        book.getGenres().remove(genre);
        bookRepository.save(book);

        return new BookDTO(book);
    }

    public BookDTO editBook(Long id, BookDTO bookDetailsDTO) {
        Book existingBook = getBookById(id);

        checkBookData(bookDetailsDTO);

        if (bookExists(bookDetailsDTO, id)) {
            throw new AppException("Книга с такими данными уже существует.", HttpStatus.CONFLICT);
        }

        DTOMapper.getInstance().map(bookDetailsDTO, existingBook);
        existingBook = bookRepository.save(existingBook);

        return new BookDTO(existingBook);
    }

    public void deleteBookById(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }

    private boolean bookExists(BookDTO bookDTO, Long currentBookId) {
        if (currentBookId != null) {
            return bookRepository.existsByUniqueFieldsExcludingId(
                    bookDTO.getTitle(), bookDTO.getAuthor(), bookDTO.getPublisher(), bookDTO.getPublicationYear(), currentBookId);
        } else {
            return bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCaseAndPublisherIgnoreCaseAndPublicationYear(
                    bookDTO.getTitle(), bookDTO.getAuthor(), bookDTO.getPublisher(), bookDTO.getPublicationYear());
        }
    }

    public Float getBookAverageRatingById(Long id) {
        return bookRepository.findAverageRatingById(id)
                .orElse(0.0f);
    }

    private void checkBookData(BookDTO bookDTO) {
        int currentYear = LocalDate.now().getYear();

        if (bookDTO.getPublicationYear() != null &&
                (bookDTO.getPublicationYear() > currentYear || bookDTO.getPublicationYear() < 1900)) {
            throw new AppException("Год издания должен быть в диапазоне от 1900 до " + currentYear + ".", HttpStatus.BAD_REQUEST);
        }
        if (bookDTO.getAmt() != null && bookDTO.getAmt() < 0) {
            throw new AppException("Количество книг должно быть положительным.", HttpStatus.BAD_REQUEST);
        }
        if (bookDTO.getCost() != null && bookDTO.getCost() < 0) {
            throw new AppException("Стоимость книги должна быть положительной.", HttpStatus.BAD_REQUEST);
        }
        if (bookDTO.getPages() != null && bookDTO.getPages() < 0) {
            throw new AppException("Количество страниц должно быть положительным.", HttpStatus.BAD_REQUEST);
        }
    }

    public List<PublisherBookCountDTO> getPublisherBookCounts() {
        return bookRepository.findAll().stream()
                .collect(Collectors.groupingBy(Book::getPublisher, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new PublisherBookCountDTO(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    public List<AuthorBookCountDTO> getAuthorBookCounts() {
        return bookRepository.findAll().stream()
                .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new AuthorBookCountDTO(e.getKey(), e.getValue().intValue()))
                .toList();
    }
}
