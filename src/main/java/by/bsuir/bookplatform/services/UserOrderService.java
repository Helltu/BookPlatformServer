package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.UserOrder;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.BookRepository;
import by.bsuir.bookplatform.repository.UserOrderRepository;
import by.bsuir.bookplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserOrderService {

    private final UserOrderRepository userOrderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<UserOrder> findAllOrders() {
        return userOrderRepository.findAll();
    }

    public Optional<UserOrder> findOrderById(Long id) {
        return userOrderRepository.findById(id);
    }

    public UserOrder placeOrder(UserOrder order) {
        if (!userRepository.existsById(order.getUser().getId())) {
            throw new AppException("User not found.", HttpStatus.NOT_FOUND);
        }

        order.getOrderBooks().forEach(orderBook -> {
            if (!bookRepository.existsById(orderBook.getBook().getId())) {
                throw new AppException("Book not found.", HttpStatus.NOT_FOUND);
            }
        });

        if (order.getDeliveryAddress().isEmpty()) {
            throw new AppException("Delivery address cannot be empty.", HttpStatus.BAD_REQUEST);
        }

        return userOrderRepository.save(order);
    }

    public void deleteOrderById(Long id) {
        userOrderRepository.deleteById(id);
    }
}