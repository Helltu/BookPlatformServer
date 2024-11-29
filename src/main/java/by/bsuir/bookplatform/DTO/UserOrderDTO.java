package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.OrderStatus;
import by.bsuir.bookplatform.entities.UserOrder;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserOrderDTO implements ValueChecker {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private String deliveryAddress;
    private LocalTime deliveryTime;
    private LocalDate deliveryDate;
    private LocalTime orderTime;
    private LocalDate orderDate;
    private String comment;
    private List<OrderDetailsDTO> orderDetailsDTO = new ArrayList<>();

    public UserOrderDTO(UserOrder order) {
        id = order.getId();
        userId = order.getUser().getId();
        status = order.getStatus();
        deliveryAddress = order.getDeliveryAddress();
        orderTime = order.getOrderTime();
        orderDate = order.getOrderDate();
        deliveryTime = order.getDeliveryTime();
        deliveryDate = order.getDeliveryDate();
        comment = order.getComment();

        if (order.getOrderBooks() != null)
            order.getOrderBooks().forEach(orderBook -> {
                orderDetailsDTO.add(new OrderDetailsDTO(orderBook.getId().getBookId(), orderBook.getAmt()));
            });
    }

    @Override
    public void checkValues() {
        if (orderDetailsDTO == null || orderDetailsDTO.isEmpty())
            throw new AppException("Заказ не может пустым.", HttpStatus.BAD_REQUEST);

        if (deliveryAddress == null)
            throw new AppException("Адрес доставки обязателен.", HttpStatus.BAD_REQUEST);

        if (deliveryTime == null)
            throw new AppException("Время доставки обязтаельно.", HttpStatus.BAD_REQUEST);

        if (deliveryDate == null)
            throw new AppException("Дата доставки обязательна.", HttpStatus.BAD_REQUEST);
    }
}
