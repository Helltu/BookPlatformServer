package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;

import java.util.Set;

import lombok.Data;

@Entity
@Data
@Table(name = "genre")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "genres")
    private Set<Book> books;
}
