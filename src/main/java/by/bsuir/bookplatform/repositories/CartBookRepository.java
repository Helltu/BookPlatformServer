package by.bsuir.bookplatform.repository;

import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.CartBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartBookRepository extends JpaRepository<CartBook, CartBookId> {
}
