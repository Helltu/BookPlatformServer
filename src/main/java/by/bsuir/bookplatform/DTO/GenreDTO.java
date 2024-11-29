package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.Genre;
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
public class GenreDTO implements ValueChecker {
    private Long id;
    private String name;
    private Set<Long> bookIds = new HashSet<>();

    public GenreDTO(Genre genre) {
        id = genre.getId();
        name = genre.getName();

        if(genre.getBooks() != null)
            genre.getBooks().forEach(book -> bookIds.add(book.getId()));
    }

    @Override
    public void checkValues() {
        if (getName() == null)
            throw new AppException("Название жанра обязательно.", HttpStatus.BAD_REQUEST);
    }
}
