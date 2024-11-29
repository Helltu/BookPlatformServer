package by.bsuir.bookplatform.repositories;

import by.bsuir.bookplatform.entities.OrderBook;
import by.bsuir.bookplatform.entities.OrderBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook, OrderBookId> {
}
