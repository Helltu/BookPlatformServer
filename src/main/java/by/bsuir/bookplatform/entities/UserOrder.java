package by.bsuir.bookplatform.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_order")
public class UserOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "del_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "del_time", nullable = false)
    private LocalTime deliveryTime;

    @Column(name = "del_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "order_time", nullable = false)
    private LocalTime orderTime;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    private String comment;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    private Set<OrderBook> orderBooks;
}

