package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_books")
public class OrderBook {

    @EmbeddedId
    private OrderBookId id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private UserOrder order;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private Integer amt;
}
