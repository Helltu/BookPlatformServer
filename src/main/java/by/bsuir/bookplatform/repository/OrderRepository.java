package by.bsuir.bookplatform.repository;

import by.bsuir.bookplatform.entities.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<UserOrder, Long> {
}
