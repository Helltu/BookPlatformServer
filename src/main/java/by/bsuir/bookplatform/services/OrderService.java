package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.UserOrder;
import by.bsuir.bookplatform.repository.BookRepository;
import by.bsuir.bookplatform.repository.OrderRepository;
import by.bsuir.bookplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<UserOrder> findAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<UserOrder> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public UserOrder saveOrder(UserOrder order) {
        if (!userRepository.existsById(order.getUser().getId())) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }

        order.getOrderBooks().forEach(orderBook -> {
            if (!bookRepository.existsById(orderBook.getBook().getId())) {
                throw new IllegalArgumentException("Книга не найдена.");
            }
        });

        if (order.getDeliveryAddress().isEmpty()) {
            throw new IllegalArgumentException("Адрес доставки не может быть пустым.");
        }

        return orderRepository.save(order);
    }

    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }
}
