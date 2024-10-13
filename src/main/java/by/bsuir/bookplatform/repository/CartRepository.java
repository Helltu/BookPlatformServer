package by.bsuir.bookplatform.repository;

import by.bsuir.bookplatform.entities.Cart;
import by.bsuir.bookplatform.entities.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
}
