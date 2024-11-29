package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailsDTO {
    private BookDTO book;
    private List<GenreDTO> genres;
    private List<MediaDTO> media;
    private Float rating;
    private List<ReviewDTO> reviews;
    private ReviewDTO userReview;
}
