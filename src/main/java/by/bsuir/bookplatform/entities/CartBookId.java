package by.bsuir.bookplatform.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartBookId implements Serializable {
    private Long bookId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartBookId)) return false;
        CartBookId that = (CartBookId) o;
        return Objects.equals(bookId, that.bookId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, userId);
    }
}
