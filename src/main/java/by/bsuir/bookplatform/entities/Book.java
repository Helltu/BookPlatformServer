package by.bsuir.bookplatform.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Float cost;

    @Column(nullable = false)
    private Integer amt;

    @Column(nullable = false)
    private Boolean hardcover;

    @Column(nullable = false)
    private Integer pages;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "book_genre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private List<Media> media = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private Set<CartBook> cartBooks = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private Set<OrderBook> orderBooks = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    private Set<Review> reviews = new HashSet<>();
}

