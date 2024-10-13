package by.bsuir.bookplatform.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class CartId implements Serializable {

    private Long userId;
    private Long bookId;
}
