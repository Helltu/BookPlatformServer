package by.bsuir.bookplatform.DTO;

import by.bsuir.bookplatform.entities.CartBookId;
import by.bsuir.bookplatform.entities.ReviewId;
import by.bsuir.bookplatform.entities.User;
import by.bsuir.bookplatform.exceptions.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDTO implements ValueChecker {
    private Long id;
    private String email;
    private String phoneNumber;
    private String password;
    private String name;
    private String surname;
    private String token;
    private Boolean isAdmin;
    private Set<CartBookId> cartBookIds = new HashSet<>();
    private Set<Long> orderIds = new HashSet<>();
    private Set<ReviewId> reviewIds = new HashSet<>();

    public UserDTO(User user) {
        id = user.getId();
        email = user.getEmail();
        phoneNumber = user.getPhoneNumber();
        password = user.getPassword();
        name = user.getName();
        surname = user.getSurname();
        isAdmin = user.getIsAdmin();

        if (user.getCartBooks() != null)
            user.getCartBooks().forEach(cartBook -> cartBookIds.add(cartBook.getId()));

        if (user.getOrders() != null)
            user.getOrders().forEach(order -> orderIds.add(order.getId()));

        if (user.getReviews() != null)
            user.getReviews().forEach(review -> reviewIds.add(review.getId()));
    }

    @Override
    public void checkValues() {
        if (getEmail() == null)
            throw new AppException("Поле 'Email' обязательно для заполнения.", HttpStatus.BAD_REQUEST);
        if (getPhoneNumber() == null)
            throw new AppException("Поле 'Номер телефона' обязательно для заполнения.", HttpStatus.BAD_REQUEST);
        else
            validatePhone();
        if (getPassword() == null)
            throw new AppException("Поле 'Пароль' обязательно для заполнения.", HttpStatus.BAD_REQUEST);
        else
            validatePassword();
        if (getName() == null)
            throw new AppException("Поле 'Имя' обязательно для заполнения.", HttpStatus.BAD_REQUEST);
        if (getSurname() == null)
            throw new AppException("Поле 'Фамилия' обязательно для заполнения.", HttpStatus.BAD_REQUEST);
    }

    public void validatePassword(){
        if (password == null || password.isEmpty()) {
            throw new AppException("Пароль не может быть пустым", HttpStatus.BAD_REQUEST);
        }
        if(password.contains(" ")) {
            throw new AppException("Пароль не может содержать пробелы", HttpStatus.BAD_REQUEST);
        }
        if (password.length() < 8) {
            throw new AppException("Пароль должен быть не менее 8 символов", HttpStatus.BAD_REQUEST);
        }
        if (!password.matches(".*\\d.*")) {
            throw new AppException("Пароль должен содержать хотя бы одну цифру", HttpStatus.BAD_REQUEST);
        }
        if (!password.matches(".*[a-zA-Zа-яА-Я].*")) {
            throw new AppException("Пароль должен содержать хотя бы одну букву", HttpStatus.BAD_REQUEST);
        }
    }

    public boolean validatePhone(){
        return phoneNumber != null
                && phoneNumber.matches("^\\+[0-9]{12}$");
    }
}
