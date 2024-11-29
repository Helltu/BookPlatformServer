package by.bsuir.bookplatform.repositories;

import by.bsuir.bookplatform.entities.CartBook;
import by.bsuir.bookplatform.entities.CartBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartBookRepository extends JpaRepository<CartBook, CartBookId> {
    List<CartBook> findByIdUserId(Long userId);

    @Modifying
    @Query("DELETE FROM CartBook cb WHERE cb.id.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
