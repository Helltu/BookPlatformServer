package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.BookDTO;
import by.bsuir.bookplatform.DTO.FilterBooksDTO;
import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.entities.Genre;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private GenreService genreService;
    @Mock
    private MediaService mediaService;
    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBooksDTO() {
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book One");
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book Two");
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));
        List<BookDTO> result = bookService.getAllBooksDTO();
        assertEquals(2, result.size());
        assertEquals("Book One", result.get(0).getTitle());
        assertEquals("Book Two", result.get(1).getTitle());
    }

    @Test
    void testGetBookById_Found() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        Book result = bookService.getBookById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> bookService.getBookById(1L));
        assertEquals("Книга с ID 1 не найдена.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testCreateBook_Success() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setDescription("Description");
        bookDTO.setAmt(1);
        bookDTO.setCost(1f);
        bookDTO.setPages(1);
        bookDTO.setHardcover(true);
        bookDTO.setTitle("New Book");
        bookDTO.setAuthor("Author");
        bookDTO.setPublisher("Publisher");
        bookDTO.setPublicationYear(2020);
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCaseAndPublisherIgnoreCaseAndPublicationYear(
                anyString(), anyString(), anyString(), anyInt())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });
        BookDTO result = bookService.createBook(bookDTO);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Book", result.getTitle());
    }

    @Test
    void testCreateBook_BookExists() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setDescription("Description");
        bookDTO.setAmt(1);
        bookDTO.setCost(1f);
        bookDTO.setPages(1);
        bookDTO.setHardcover(true);
        bookDTO.setTitle("Existing Book");
        bookDTO.setAuthor("Author");
        bookDTO.setPublisher("Publisher");
        bookDTO.setPublicationYear(2020);
        when(bookRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCaseAndPublisherIgnoreCaseAndPublicationYear(
                anyString(), anyString(), anyString(), anyInt())).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> bookService.createBook(bookDTO));
        assertEquals("Книга с такими данными уже существует.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testEditBook_Success() {
        Long bookId = 1L;
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Updated Book");
        bookDTO.setAuthor("Updated Author");
        bookDTO.setPublisher("Updated Publisher");
        bookDTO.setPublicationYear(2021);
        Book existingBook = new Book();
        existingBook.setId(bookId);
        existingBook.setTitle("Old Book");
        existingBook.setAuthor("Old Author");
        existingBook.setPublisher("Old Publisher");
        existingBook.setPublicationYear(2020);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByUniqueFieldsExcludingId(
                anyString(), anyString(), anyString(), anyInt(), eq(bookId))).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BookDTO result = bookService.editBook(bookId, bookDTO);
        assertNotNull(result);
        assertEquals("Updated Book", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        assertEquals("Updated Publisher", result.getPublisher());
        assertEquals(2021, result.getPublicationYear());
    }

    @Test
    void testEditBook_BookExists() {
        Long bookId = 1L;
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Existing Book");
        bookDTO.setAuthor("Author");
        bookDTO.setPublisher("Publisher");
        bookDTO.setPublicationYear(2020);
        Book existingBook = new Book();
        existingBook.setId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByUniqueFieldsExcludingId(
                anyString(), anyString(), anyString(), anyInt(), eq(bookId))).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> bookService.editBook(bookId, bookDTO));
        assertEquals("Книга с такими данными уже существует.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testDeleteBookById_Success() {
        Long bookId = 1L;
        Book book = new Book();
        book.setId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        bookService.deleteBookById(bookId);
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void testDeleteBookById_NotFound() {
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> bookService.deleteBookById(bookId));
        assertEquals("Книга с ID 1 не найдена.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testGetBookAverageRatingById() {
        Long bookId = 1L;
        when(bookRepository.findAverageRatingById(bookId)).thenReturn(Optional.of(4.5f));
        Float averageRating = bookService.getBookAverageRatingById(bookId);
        assertEquals(4.5f, averageRating);
    }

    @Test
    void testGetBookAverageRatingById_NoRating() {
        Long bookId = 1L;
        when(bookRepository.findAverageRatingById(bookId)).thenReturn(Optional.empty());
        Float averageRating = bookService.getBookAverageRatingById(bookId);
        assertEquals(0.0f, averageRating);
    }

    @Test
    void testFilterBooks() {
        FilterBooksDTO filter = new FilterBooksDTO();
        filter.setTitle("Book");
        filter.setAuthors(Arrays.asList("Author1", "Author2"));
        filter.setMinCost(10.0f);
        filter.setMaxCost(50.0f);
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book One");
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book Two");
        when(bookRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(book1, book2));
        List<BookDTO> result = bookService.filterBooks(filter);
        assertEquals(2, result.size());
    }

    @Test
    void testAddGenreToBook_Success() {
        Long bookId = 1L;
        Long genreId = 1L;
        Book book = new Book();
        book.setId(bookId);
        book.setGenres(new HashSet<>());
        Genre genre = new Genre();
        genre.setId(genreId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(genreService.getGenreById(genreId)).thenReturn(genre);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        BookDTO result = bookService.addGenreToBook(bookId, genreId);
        assertNotNull(result);
        assertTrue(book.getGenres().contains(genre));
    }

    @Test
    void testAddGenreToBook_AlreadyExists() {
        Long bookId = 1L;
        Long genreId = 1L;
        Genre genre = new Genre();
        genre.setId(genreId);
        Book book = new Book();
        book.setId(bookId);
        book.setGenres(new HashSet<>(Arrays.asList(genre)));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(genreService.getGenreById(genreId)).thenReturn(genre);
        AppException exception = assertThrows(AppException.class, () -> bookService.addGenreToBook(bookId, genreId));
        assertEquals("Книга уже содержит данный жанр.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }
}
