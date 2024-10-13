package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.CartBookId;
import by.bsuir.bookplatform.repository.BookRepository;
import by.bsuir.bookplatform.repository.CartRepository;
import by.bsuir.bookplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public List<CartBook> findAllCarts() {
        return cartRepository.findAll();
    }

    public Optional<CartBook> findCartById(CartBookId cartBookId) {
        return cartRepository.findById(cartBookId);
    }

    public CartBook saveCart(CartBook cart) {
        if (!userRepository.existsById(cart.getUser().getId())) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }
        if (!bookRepository.existsById(cart.getBook().getId())) {
            throw new IllegalArgumentException("Книга не найдена.");
        }

        CartBookId cartBookId = new CartBookId(cart.getUser().getId(), cart.getBook().getId());
        if (cartRepository.existsById(cartBookId)) {
            throw new IllegalArgumentException("Книга уже в корзине.");
        }

        return cartRepository.save(cart);
    }

    public void deleteCartById(CartBookId cartBookId) {
        cartRepository.deleteById(cartBookId);
    }

    public void clearUserCart(Long userId) {
        List<CartBook> userCartBooks = cartRepository.findAll().stream()
                .filter(c -> c.getUser().getId().equals(userId))
                .toList();

        if (userCartBooks.isEmpty()) {
            throw new IllegalArgumentException("Корзина пользователя уже пуста.");
        }

        cartRepository.deleteAll(userCartBooks);
    }
}

