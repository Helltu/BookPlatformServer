package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.CartBookDTO;
import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.CartBookId;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.CartBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CartBookService {

    private final CartBookRepository cartBookRepository;

    public List<CartBookDTO> getAllCartBooksDTO() {
        return cartBookRepository.findAll().stream()
                .map(CartBookDTO::new)
                .toList();
    }

    public List<CartBook> getAllCartBooks() {
        return cartBookRepository.findAll();
    }

    public CartBookDTO getCartBookDTOById(Long bookId, Long userId) {
        CartBook cartBook = getCartBookById(bookId, userId);
        return new CartBookDTO(cartBook);
    }

    public CartBook getCartBookById(Long bookId, Long userId) {
        CartBookId id = new CartBookId(bookId, userId);
        return cartBookRepository.findById(id)
                .orElseThrow(() -> new AppException("В корзине пользователя " + userId + " книга " + bookId + " не найдена.", HttpStatus.NOT_FOUND));
    }

    public List<CartBookDTO> getUserCartBooks(Long userId) {
        List<CartBook> cartBooks = cartBookRepository.findByIdUserId(userId);
        return cartBooks.stream()
                .map(CartBookDTO::new)
                .toList();
    }

    public void clearUserCartBooks(Long userId) {
        getAllCartBooks().stream().filter(cb -> cb.getId().getUserId().equals(userId)).forEach(cartBookDTO -> cartBookRepository.deleteById(cartBookDTO.getId()));
    }

    public CartBookDTO addBookToUserCart(CartBookDTO cartBookDTO) {
        if (bookInUserCartExists(cartBookDTO.getBookId(), cartBookDTO.getUserId()))
            throw new AppException("Книга " + cartBookDTO.getBookId() + " уже находится в корзине пользователя " + cartBookDTO.getUserId() + ".", HttpStatus.CONFLICT);

        CartBook cartBook = DTOMapper.getInstance().map(cartBookDTO, CartBook.class);
        cartBook = cartBookRepository.save(cartBook);

        return new CartBookDTO(cartBook);
    }

    public CartBookDTO editBookInUserCart(CartBookDTO cartBookDTODetails) {
        CartBookId cartBookId = new CartBookId(cartBookDTODetails.getBookId(), cartBookDTODetails.getUserId());

        CartBook existingCartBook = cartBookRepository.findById(cartBookId)
                .orElseThrow(() -> new AppException("В корзине пользователя " + cartBookId.getUserId() + " книга " + cartBookId.getBookId() + " не найдена.", HttpStatus.NOT_FOUND));

        if (cartBookDTODetails.getAmt() != null) {
            existingCartBook.setAmt(cartBookDTODetails.getAmt());
        }

        existingCartBook = cartBookRepository.save(existingCartBook);

        return new CartBookDTO(existingCartBook);
    }

    public void removeBookFromUserCart(Long bookId, Long userId) {
        CartBookId cartBookId = new CartBookId(bookId, userId);

        if (!cartBookRepository.existsById(cartBookId))
            throw new AppException("В корзине пользователя " + userId + " книга " + bookId + " не найдена.", HttpStatus.NOT_FOUND);

        cartBookRepository.deleteById(cartBookId);
    }

    private boolean bookInUserCartExists(Long bookId, Long userId) {
        return cartBookRepository.existsById(new CartBookId(bookId, userId));
    }
}
