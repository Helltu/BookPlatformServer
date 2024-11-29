package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.Genre;
import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReviewDTO implements ValueChecker{
    private Long bookId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String text;

    public ReviewDTO(Review review) {
        bookId = review.getId().getBookId();
        userId = review.getId().getUserId();
        rating = review.getRating();
        text = review.getText();
    }

    @Override
    public void checkValues() {
        if(bookId == null)
            throw new AppException("ID книги обязательно.", HttpStatus.BAD_REQUEST);

        if(userId == null)
            throw new AppException("ID пользователя обязательно.", HttpStatus.BAD_REQUEST);

        if (rating == null)
            throw new AppException("Оценка обязательна.", HttpStatus.BAD_REQUEST);

        if(rating < 1 || rating > 5)
            throw new AppException("Оценка должна быть между 1 и 5", HttpStatus.BAD_REQUEST);
    }
}
