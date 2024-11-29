package by.bsuir.bookplatform.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewId implements Serializable {

    private Long bookId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReviewId)) return false;
        ReviewId that = (ReviewId) o;
        return Objects.equals(bookId, that.bookId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, userId);
    }
}
