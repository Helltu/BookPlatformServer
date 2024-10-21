package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cart_book")
public class CartBook {

    @EmbeddedId
    private CartBookId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private Integer amt; 
}

