package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartBookDTO implements ValueChecker {
    private Long userId;
    private Long bookId;
    private Integer amt;

    public CartBookDTO(CartBook cartBook) {
        bookId = cartBook.getId().getBookId();
        userId = cartBook.getId().getUserId();
        amt = cartBook.getAmt();
    }

    @Override
    public void checkValues() {
        if(getAmt() == null)
            throw new AppException("Количество книг в корзине обязательно.", HttpStatus.BAD_GATEWAY);
    }
}
