package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_book")
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
