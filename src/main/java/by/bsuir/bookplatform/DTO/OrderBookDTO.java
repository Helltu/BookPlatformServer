package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.OrderBook;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderBookDTO implements ValueChecker{
    private Long orderId;
    private Long bookId;
    private Integer amt;

    public OrderBookDTO(OrderBook orderBook) {
        orderId = orderBook.getId().getOrderId();
        bookId = orderBook.getId().getBookId();
        amt = orderBook.getAmt();
    }

    @Override
    public void checkValues() {
        if(getOrderId() == null)
            throw new AppException("Order id is required.", HttpStatus.BAD_REQUEST);
        if(getBookId() == null)
            throw new AppException("Book id is required.", HttpStatus.BAD_REQUEST);
        if (getAmt() == null)
            throw new AppException("Amt is required.", HttpStatus.BAD_REQUEST);
    }
}
