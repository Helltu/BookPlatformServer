package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.User;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        Optional<User> existingUser = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst();

        if (existingUser.isPresent()) {
            throw new AppException("A user with this email already exists.", HttpStatus.CONFLICT);
        }

        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}