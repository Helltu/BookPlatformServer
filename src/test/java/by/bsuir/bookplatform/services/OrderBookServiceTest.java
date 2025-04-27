package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.OrderBookDTO;
import by.bsuir.bookplatform.entities.*;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.BookRepository;
import by.bsuir.bookplatform.repositories.OrderBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderBookServiceTest {

    @InjectMocks
    private OrderBookService orderBookService;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetOrderBookDTOById_Found() {
        OrderBookId id = new OrderBookId(1L, 1L);
        OrderBook orderBook = new OrderBook();
        orderBook.setId(id);
        when(orderBookRepository.findById(id)).thenReturn(Optional.of(orderBook));
        OrderBookDTO result = orderBookService.getOrderBookDTOById(1L, 1L);
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(1L, result.getOrderId());
    }

    @Test
    void testGetOrderBookDTOById_NotFound() {
        OrderBookId id = new OrderBookId(1L, 1L);
        when(orderBookRepository.findById(id)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> orderBookService.getOrderBookDTOById(1L, 1L));
        assertEquals("In order 1 order book 1 not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testAddBookToUserOrder_Success() {
        OrderBookDTO orderBookDTO = new OrderBookDTO();
        orderBookDTO.setBookId(1L);
        orderBookDTO.setOrderId(1L);
        orderBookDTO.setAmt(1);
        Book book = new Book();
        book.setId(1L);
        book.setAmt(5);
        when(orderBookRepository.findById(any(OrderBookId.class))).thenReturn(Optional.empty());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OrderBookDTO result = orderBookService.addBookToUserOrder(orderBookDTO);
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(1L, result.getOrderId());
        assertEquals(1, result.getAmt());
        assertEquals(4, book.getAmt());
    }

    @Test
    void testAddBookToUserOrder_AlreadyExists() {
        OrderBookDTO orderBookDTO = new OrderBookDTO();
        orderBookDTO.setAmt(1);
        orderBookDTO.setBookId(1L);
        orderBookDTO.setOrderId(1L);
        OrderBook existingOrderBook = new OrderBook();
        existingOrderBook.setId(new OrderBookId(1L, 1L));
        when(orderBookRepository.findById(any(OrderBookId.class))).thenReturn(Optional.of(existingOrderBook));
        AppException exception = assertThrows(AppException.class, () -> orderBookService.addBookToUserOrder(orderBookDTO));
        assertEquals("In order 1 order book 1 already exists.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testAddBookToUserOrder_BookNotFound() {
        OrderBookDTO orderBookDTO = new OrderBookDTO();
        orderBookDTO.setAmt(1);
        orderBookDTO.setBookId(1L);
        orderBookDTO.setOrderId(1L);
        when(orderBookRepository.findById(any(OrderBookId.class))).thenReturn(Optional.empty());
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> orderBookService.addBookToUserOrder(orderBookDTO));
        assertEquals("Book with id 1 not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testAddBookToUserOrder_NotEnoughBooks() {
        OrderBookDTO orderBookDTO = new OrderBookDTO();
        orderBookDTO.setBookId(1L);
        orderBookDTO.setOrderId(1L);
        orderBookDTO.setAmt(10);
        Book book = new Book();
        book.setId(1L);
        book.setAmt(5);
        when(orderBookRepository.findById(any(OrderBookId.class))).thenReturn(Optional.empty());
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        AppException exception = assertThrows(AppException.class, () -> orderBookService.addBookToUserOrder(orderBookDTO));
        assertEquals("Not enough books with id 1, present: 5,need 10.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testRemoveBookFromUserOrder_Success() {
        OrderBookId id = new OrderBookId(1L, 1L);
        OrderBook orderBook = new OrderBook();
        orderBook.setId(id);
        when(orderBookRepository.findById(id)).thenReturn(Optional.of(orderBook));
        orderBookService.removeBookFromUserOrder(1L, 1L);
        verify(orderBookRepository, times(1)).deleteById(id);
    }

    @Test
    void testRemoveBookFromUserOrder_NotFound() {
        OrderBookId id = new OrderBookId(1L, 1L);
        when(orderBookRepository.findById(id)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> orderBookService.removeBookFromUserOrder(1L, 1L));
        assertEquals("In order 1 order book 1 not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
