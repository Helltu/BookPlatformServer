package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "review")
public class Review {

    @EmbeddedId
    private ReviewId id;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user__id")
    private User user;

    @Column(nullable = false)
    private Integer rating;

    private String text;
}
