package by.bsuir.bookplatform.DTO.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCountDTO {
    private String status;
    private int count;
}
