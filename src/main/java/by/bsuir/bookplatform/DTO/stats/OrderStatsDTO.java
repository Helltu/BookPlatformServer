package by.bsuir.bookplatform.DTO.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsDTO {
    private LocalDate date;
    private Integer orderCount;
}
