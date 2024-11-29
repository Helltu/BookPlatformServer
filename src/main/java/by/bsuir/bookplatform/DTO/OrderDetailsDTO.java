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
public class OrderDetailsDTO implements ValueChecker{
    private Long bookId;
    private Integer amt;

    @Override
    public void checkValues() {
        if(getBookId() == null)
            throw new AppException("Book id is required.", HttpStatus.BAD_REQUEST);
        if (getAmt() == null)
            throw new AppException("Amt is required.", HttpStatus.BAD_REQUEST);
    }
}
