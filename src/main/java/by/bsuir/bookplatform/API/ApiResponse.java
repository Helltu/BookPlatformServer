package by.bsuir.bookplatform.API;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ApiResponse<T> {

    private T data;
    private boolean status;
    private String message;

}
