package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookDTO implements ValueChecker {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String description;
    private Float cost;
    private Integer amt;
    private Boolean hardcover;
    private Integer pages;
    private Set<Long> genreIds = new HashSet<>();
    private List<byte[]> media = new ArrayList<>();

    public BookDTO(Book book) {
        id = book.getId();
        title = book.getTitle();
        author = book.getAuthor();
        publisher = book.getPublisher();
        publicationYear = book.getPublicationYear();
        description = book.getDescription();
        cost = book.getCost();
        amt = book.getAmt();
        hardcover = book.getHardcover();
        pages = book.getPages();

        if (book.getGenres() != null)
            book.getGenres().forEach(genre -> genreIds.add(genre.getId()));

        if (book.getMedia() != null)
            book.getMedia().forEach(media -> this.media.add(media.getMedia()));
    }

    @Override
    public void checkValues() {
        if (getTitle() == null)
            throw new AppException("Название книги обязательно.", HttpStatus.BAD_REQUEST);

        if (getAuthor() == null)
            throw new AppException("Автор книги обязателен.", HttpStatus.BAD_REQUEST);

        if (getPublisher() == null)
            throw new AppException("Издательство книги обязателен.", HttpStatus.BAD_REQUEST);

        if (getPublicationYear() == null)
            throw new AppException("Год публикации книги обязателен.", HttpStatus.BAD_REQUEST);

        if (getDescription() == null)
            throw new AppException("Описание книги обязательно.", HttpStatus.BAD_REQUEST);

        if (getCost() == null)
            throw new AppException("Стоимость книги обязательна.", HttpStatus.BAD_REQUEST);

        if (getAmt() == null)
            throw new AppException("Количество книг обязательно.", HttpStatus.BAD_REQUEST);

        if (getHardcover() == null)
            throw new AppException("Тип переплёта обязателен.", HttpStatus.BAD_REQUEST);

        if (getPages() == null)
            throw new AppException("Количество страниц обязательно.", HttpStatus.BAD_REQUEST);

        if (publicationYear > LocalDate.now().getYear() || publicationYear < 1900) {
            throw new AppException("Год издания должен быть в диапазоне от 1900 до " + LocalDate.now().getYear() + ".", HttpStatus.BAD_REQUEST);
        }

        if (amt < 0) {
            throw new AppException("Количество книг должно быть положительным.", HttpStatus.BAD_REQUEST);
        }

        if (cost < 0) {
            throw new AppException("Стоимость книги должна быть положительной.", HttpStatus.BAD_REQUEST);
        }

        if (pages < 0) {
            throw new AppException("Количество страниц должно быть положительным.", HttpStatus.BAD_REQUEST);
        }
    }
}
