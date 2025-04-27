package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.CartBookDTO;
import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.CartBookId;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.CartBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartBookServiceTest {

    @InjectMocks
    private CartBookService cartBookService;

    @Mock
    private CartBookRepository cartBookRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCartBooksDTO() {
        CartBook cartBook1 = new CartBook();
        cartBook1.setId(new CartBookId(1L, 1L));
        CartBook cartBook2 = new CartBook();
        cartBook2.setId(new CartBookId(2L, 1L));
        when(cartBookRepository.findAll()).thenReturn(Arrays.asList(cartBook1, cartBook2));
        List<CartBookDTO> result = cartBookService.getAllCartBooksDTO();
        assertEquals(2, result.size());
    }

    @Test
    void testGetCartBookById_Found() {
        CartBookId id = new CartBookId(1L, 1L);
        CartBook cartBook = new CartBook();
        cartBook.setId(id);
        when(cartBookRepository.findById(id)).thenReturn(Optional.of(cartBook));
        CartBook result = cartBookService.getCartBookById(1L, 1L);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetCartBookById_NotFound() {
        CartBookId id = new CartBookId(1L, 1L);
        when(cartBookRepository.findById(id)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> cartBookService.getCartBookById(1L, 1L));
        assertEquals("В корзине пользователя 1 книга 1 не найдена.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testAddBookToUserCart_Success() {
        CartBookDTO cartBookDTO = new CartBookDTO();
        cartBookDTO.setBookId(1L);
        cartBookDTO.setUserId(1L);
        cartBookDTO.setAmt(1);
        when(cartBookRepository.existsById(any(CartBookId.class))).thenReturn(false);
        when(cartBookRepository.save(any(CartBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CartBookDTO result = cartBookService.addBookToUserCart(cartBookDTO);
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals(1L, result.getUserId());
        assertEquals(1, result.getAmt());
    }

    @Test
    void testAddBookToUserCart_BookAlreadyInCart() {
        CartBookDTO cartBookDTO = new CartBookDTO();
        cartBookDTO.setBookId(1L);
        cartBookDTO.setUserId(1L);
        when(cartBookRepository.existsById(any(CartBookId.class))).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> cartBookService.addBookToUserCart(cartBookDTO));
        assertEquals("Книга 1 уже находится в корзине пользователя 1.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testEditBookInUserCart_Success() {
        CartBookDTO cartBookDTO = new CartBookDTO();
        cartBookDTO.setBookId(1L);
        cartBookDTO.setUserId(1L);
        cartBookDTO.setAmt(2);
        CartBook existingCartBook = new CartBook();
        existingCartBook.setId(new CartBookId(1L, 1L));
        existingCartBook.setAmt(1);
        when(cartBookRepository.findById(any(CartBookId.class))).thenReturn(Optional.of(existingCartBook));
        when(cartBookRepository.save(any(CartBook.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CartBookDTO result = cartBookService.editBookInUserCart(cartBookDTO);
        assertNotNull(result);
        assertEquals(2, result.getAmt());
    }

    @Test
    void testEditBookInUserCart_NotFound() {
        CartBookDTO cartBookDTO = new CartBookDTO();
        cartBookDTO.setBookId(1L);
        cartBookDTO.setUserId(1L);
        when(cartBookRepository.findById(any(CartBookId.class))).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> cartBookService.editBookInUserCart(cartBookDTO));
        assertEquals("В корзине пользователя 1 книга 1 не найдена.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testRemoveBookFromUserCart_Success() {
        CartBookId id = new CartBookId(1L, 1L);
        when(cartBookRepository.existsById(id)).thenReturn(true);
        cartBookService.removeBookFromUserCart(1L, 1L);
        verify(cartBookRepository, times(1)).deleteById(id);
    }

    @Test
    void testRemoveBookFromUserCart_NotFound() {
        CartBookId id = new CartBookId(1L, 1L);
        when(cartBookRepository.existsById(id)).thenReturn(false);
        AppException exception = assertThrows(AppException.class, () -> cartBookService.removeBookFromUserCart(1L, 1L));
        assertEquals("В корзине пользователя 1 книга 1 не найдена.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
