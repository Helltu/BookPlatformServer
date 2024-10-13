package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Float cost;

    @Column(nullable = false)
    private Integer amt;

    @ManyToMany
    @JoinTable(
            name = "book_genre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres;

    @OneToMany(mappedBy = "book")
    private Set<CartBook> cartBooks;

    @OneToMany(mappedBy = "book")
    private Set<OrderBook> orderBooks;

    @OneToMany(mappedBy = "book")
    private Set<Review> reviews;
}

