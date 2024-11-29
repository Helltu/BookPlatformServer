package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.Media;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MediaDTO implements ValueChecker{
    private Long id;
    private byte[] media;
    private Long bookId;

    public MediaDTO(Media mediaObj) {
        id = mediaObj.getId();
        media = mediaObj.getMedia();
        bookId = mediaObj.getId();
    }

    @Override
    public void checkValues() {
        if (getBookId() == null)
            throw new AppException("Book id is required.", HttpStatus.BAD_REQUEST);
        if (getMedia() == null)
            throw new AppException("Media is required.", HttpStatus.BAD_REQUEST);
    }
}
