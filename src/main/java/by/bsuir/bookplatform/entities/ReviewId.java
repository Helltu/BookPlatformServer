package by.bsuir.bookplatform.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class ReviewId implements Serializable {

    private Long bookId;
    private Long userId;
}
