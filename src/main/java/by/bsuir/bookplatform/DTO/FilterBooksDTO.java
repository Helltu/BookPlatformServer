package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FilterBooksDTO implements ValueChecker {
    private String title;
    private List<String> authors;
    private List<String> publishers;
    private Integer minPublicationYear;
    private Integer maxPublicationYear;
    private Float minCost;
    private Float maxCost;
    private Boolean hardcover;
    private Integer minPages;
    private Integer maxPages;
    private Integer minRating;
    private Integer maxRating;
    private List<String> genres;
    private String sortBy = "title";
    private String sortDirection = "desc";

    @Override
    public void checkValues() {
        if (minPublicationYear != null)
            if (minPublicationYear > LocalDate.now().getYear() || minPublicationYear < 1900) {
                throw new AppException("Год публикации должен быть в диапазоне от 1900 до " + LocalDate.now().getYear() + '.', HttpStatus.BAD_REQUEST);
            }

        if (maxPublicationYear != null)
            if (maxPublicationYear > LocalDate.now().getYear() || maxPublicationYear < 1900) {
                throw new AppException("Год публикации должен быть в диапазоне от 1900 до " + LocalDate.now().getYear() + '.', HttpStatus.BAD_REQUEST);
            }

        if (minCost != null)
            if (minCost < 0) {
                throw new AppException("Минимальная стоимость должна быть положительной", HttpStatus.BAD_REQUEST);
            }

        if (maxCost != null)
            if (maxCost < 0) {
                throw new AppException("Максимальная стоимость должна быть положительной", HttpStatus.BAD_REQUEST);
            }

        if (minPages != null)
            if (minPages < 0) {
                throw new AppException("Минимальное количество страниц должно быть положительным", HttpStatus.BAD_REQUEST);
            }

        if (maxPages != null)
            if (maxPages < 0) {
                throw new AppException("Максимальное количество страниц должно быть положительным", HttpStatus.BAD_REQUEST);
            }

        if (minRating != null)
            if (minRating < 1 || minRating > 5) {
                throw new AppException("Минимальный рейтинг должен быть в диапазоне от 1 до 5", HttpStatus.BAD_REQUEST);
            }

        if (maxRating != null)
            if (maxRating < 1 || maxRating > 5) {
                throw new AppException("Максимальный рейтинг должен быть в диапазоне от 1 до 5", HttpStatus.BAD_REQUEST);
            }

        List<String> validSortFields = Arrays.asList("title", "publicationYear", "cost", "rating", "popularity");
        if (sortBy != null && !validSortFields.contains(sortBy)) {
            throw new AppException("Недопустимое поле сортировки: " + sortBy + ". Допустимые поля: " + String.join(", ", validSortFields), HttpStatus.BAD_REQUEST);
        }

        if (sortDirection != null && !(sortDirection.equalsIgnoreCase("asc") || sortDirection.equalsIgnoreCase("desc"))) {
            throw new AppException("Недопустимое направление сортировки: " + sortDirection + ". Допустимые направления: 'asc' или 'desc'.", HttpStatus.BAD_REQUEST);
        }
    }
}
