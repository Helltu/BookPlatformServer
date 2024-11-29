package by.bsuir.bookplatform.config;

import by.bsuir.bookplatform.DTO.ErrorDTO;
import by.bsuir.bookplatform.exceptions.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = {AppException.class})
    @ResponseBody
    public ResponseEntity<ErrorDTO> handleException(AppException ex){
        return ResponseEntity.status(ex.getStatus())
                .body(ErrorDTO.builder().message(ex.getMessage()).build());
    }
}
