package by.bsuir.bookplatform.repositories;

import by.bsuir.bookplatform.entities.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {
}
