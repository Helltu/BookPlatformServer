package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.UserDTO;
import by.bsuir.bookplatform.entities.User;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.CharBuffer;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById_Found() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> userService.getUserById(1L));
        assertEquals("Пользователь с ID 1 не найден.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testLogin_Success() {
        UserDTO loginCredentials = new UserDTO();
        loginCredentials.setEmail("test@example.com");
        loginCredentials.setPassword("password");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(passwordEncoder.matches(any(CharSequence.class), eq("encodedPassword"))).thenReturn(true);
        UserDTO result = userService.login(loginCredentials);
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testLogin_WrongPassword() {
        UserDTO loginCredentials = new UserDTO();
        loginCredentials.setEmail("test@example.com");
        loginCredentials.setPassword("wrongPassword");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(passwordEncoder.matches(any(CharSequence.class), eq("encodedPassword"))).thenReturn(false);
        AppException exception = assertThrows(AppException.class, () -> userService.login(loginCredentials));
        assertEquals("Неправильный логин или пароль.", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void testRegister_Success() {
        UserDTO signUpCredentials = new UserDTO();
        signUpCredentials.setEmail("newuser@example.com");
        signUpCredentials.setName("Name");
        signUpCredentials.setSurname("Surname");
        signUpCredentials.setPhoneNumber("+111111111111");
        signUpCredentials.setPassword("password1");
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        UserDTO result = userService.register(signUpCredentials);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newuser@example.com", result.getEmail());
    }

    @Test
    void testRegister_UserExists() {
        UserDTO signUpCredentials = new UserDTO();
        signUpCredentials.setEmail("existinguser@example.com");
        signUpCredentials.setName("Name");
        signUpCredentials.setSurname("Surname");
        signUpCredentials.setPhoneNumber("+111111111111");
        signUpCredentials.setPassword("password1");
        User existingUser = new User();
        existingUser.setEmail("existinguser@example.com");
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        AppException exception = assertThrows(AppException.class, () -> userService.register(signUpCredentials));
        assertEquals("Пользователь уже существует.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testEditUser_Success() {
        Long userId = 1L;
        UserDTO userDetailsDTO = new UserDTO();
        userDetailsDTO.setEmail("updated@example.com");
        userDetailsDTO.setName("Updated Name");
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");
        existingUser.setName("Old Name");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserDTO result = userService.editUser(userId, userDetailsDTO);
        assertNotNull(result);
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void testEditUser_NotFound() {
        Long userId = 1L;
        UserDTO userDetailsDTO = new UserDTO();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> userService.editUser(userId, userDetailsDTO));
        assertEquals("Пользователь с ID 1 не найден.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
