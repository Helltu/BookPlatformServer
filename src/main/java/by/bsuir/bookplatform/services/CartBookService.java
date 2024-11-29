package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.CartBookId;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.BookRepository;
import by.bsuir.bookplatform.repository.CartBookRepository;
import by.bsuir.bookplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CartService {

    private final CartBookRepository cartBookRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public List<CartBook> findAllCarts() {
        return cartBookRepository.findAll();
    }

    public Optional<CartBook> findCartById(CartBookId cartBookId) {
        return cartBookRepository.findById(cartBookId);
    }

    public CartBook addBookToCart(CartBook cartBook) {
        if (!userRepository.existsById(cartBook.getUser().getId())) {
            throw new AppException("User not found.", HttpStatus.NOT_FOUND);
        }
        if (!bookRepository.existsById(cartBook.getBook().getId())) {
            throw new AppException("Book not found.", HttpStatus.NOT_FOUND);
        }

        CartBookId cartBookId = new CartBookId(cartBook.getUser().getId(), cartBook.getBook().getId());
        if (cartBookRepository.existsById(cartBookId)) {
            throw new AppException("Book is already in the cart.", HttpStatus.CONFLICT);
        }

        return cartBookRepository.save(cartBook);
    }

    public void deleteBookFromCart(CartBookId cartBookId) {
        cartBookRepository.deleteById(cartBookId);
    }

    public void clearUserCart(Long userId) {
        List<CartBook> userCartBooks = cartBookRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(userId))
                .toList();

        if (userCartBooks.isEmpty()) {
            throw new AppException("User's cart is already empty.", HttpStatus.CONFLICT);
        }

        cartBookRepository.deleteAll(userCartBooks);
    }
}