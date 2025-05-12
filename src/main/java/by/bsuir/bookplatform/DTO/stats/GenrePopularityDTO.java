package by.bsuir.bookplatform.DTO.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenrePopularityDTO {
    private String genreName;
    private Integer orderCount;
}
