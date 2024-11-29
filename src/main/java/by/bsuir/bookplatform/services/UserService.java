package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.UserDTO;
import by.bsuir.bookplatform.entities.User;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);

        if (!user.isPresent())
            throw new AppException("Пользователь с ID " + id + " не найден.", HttpStatus.NOT_FOUND);

        return user.get();
    }

    public UserDTO getUserDTOById(Long id) {
        Optional<User> user = userRepository.findById(id);

        if (!user.isPresent())
            throw new AppException("Пользователь с ID " + id + " не найден.", HttpStatus.NOT_FOUND);

        return new UserDTO(user.get());
    }

    public UserDTO getUserDTOByEmail(String email) {
        Optional<User> user = userRepository.findAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();

        if (!user.isPresent())
            throw new AppException("Пользователь с email " + email + " не найден.", HttpStatus.NOT_FOUND);

        return new UserDTO(user.get());
    }

    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();

        if (!user.isPresent())
            throw new AppException("Неправильный логин или пароль.", HttpStatus.FORBIDDEN);

        return user.get();
    }

    public UserDTO login(UserDTO loginCredentials) {
        User user = getUserByEmail(loginCredentials.getEmail());

        if (!passwordEncoder.matches(CharBuffer.wrap(loginCredentials.getPassword()), user.getPassword()))
            throw new AppException("Неправильный логин или пароль.", HttpStatus.FORBIDDEN);

        return new UserDTO(user);
    }

    public UserDTO register(UserDTO signUpCredentials) {
        signUpCredentials.checkValues();

        Optional<User> userOpt = userRepository.findAll().stream().filter(u -> u.getEmail().equals(signUpCredentials.getEmail())).findFirst();

        if (userOpt.isPresent())
            throw new AppException("Пользователь уже существует.", HttpStatus.CONFLICT);

        User user = DTOMapper.getInstance().map(signUpCredentials, User.class);

        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(user.getPassword())));

        user = userRepository.save(user);

        return new UserDTO(user);
    }

    public UserDTO editUser(Long id, UserDTO userDetailsDTO) {
        Optional<User> userOpt = userRepository.findById(id);

        if (!userOpt.isPresent())
            throw new AppException("Пользователь с ID " + id + " не найден.", HttpStatus.NOT_FOUND);

        User user = userOpt.get();

        if(userDetailsDTO.getPassword() != null){
            userDetailsDTO.validatePassword();
            user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDetailsDTO.getPassword())));
        }
        if(userDetailsDTO.getEmail() != null)
            user.setEmail(userDetailsDTO.getEmail());
        if(userDetailsDTO.getPhoneNumber() != null){
            userDetailsDTO.validatePhone();
            user.setPhoneNumber(userDetailsDTO.getPhoneNumber());
        }
        if(userDetailsDTO.getName() != null)
            user.setName(userDetailsDTO.getName());
        if(userDetailsDTO.getSurname() != null)
            user.setSurname(userDetailsDTO.getSurname());

        user = userRepository.save(user);

        return new UserDTO(user);
    }
}
