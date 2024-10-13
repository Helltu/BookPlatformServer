package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @OneToMany(mappedBy = "user")
    private Set<CartBook> cartBooks;

    @OneToMany(mappedBy = "user")
    private Set<UserOrder> orders;

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews;
}
