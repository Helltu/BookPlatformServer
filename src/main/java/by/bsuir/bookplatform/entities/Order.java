package by.bsuir.bookplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "del_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "del_time", nullable = false)
    private LocalTime deliveryTime;

    @Column(name = "del_date", nullable = false)
    private LocalDate deliveryDate;

    private String comment;
}
