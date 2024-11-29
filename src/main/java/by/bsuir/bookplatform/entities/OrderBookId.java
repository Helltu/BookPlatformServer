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
public class OrderBookId implements Serializable {

    private Long bookId;
    private Long orderId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderBookId)) return false;
        OrderBookId that = (OrderBookId) o;
        return Objects.equals(bookId, that.bookId) && Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, orderId);
    }
}
