package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "genre")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(mappedBy = "genres", fetch = FetchType.EAGER)
    private Set<Book> books = new HashSet<Book>();
}
