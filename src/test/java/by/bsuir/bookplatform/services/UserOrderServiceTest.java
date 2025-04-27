package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.OrderBookDTO;
import by.bsuir.bookplatform.DTO.OrderDetailsDTO;
import by.bsuir.bookplatform.DTO.UserOrderDTO;
import by.bsuir.bookplatform.entities.*;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repositories.UserOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserOrderServiceTest {

    @InjectMocks
    private UserOrderService userOrderService;

    @Mock
    private UserOrderRepository userOrderRepository;

    @Mock
    private OrderBookService orderBookService;

    @Mock
    private CartBookService cartBookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllUserOrdersDTO() {
        User user = new User();
        user.setId(1L);

        UserOrder order1 = new UserOrder();
        order1.setId(1L);
        order1.setUser(user);

        UserOrder order2 = new UserOrder();
        order2.setId(2L);
        order2.setUser(user);

        when(userOrderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        List<UserOrderDTO> result = userOrderService.getAllUserOrdersDTO();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void testGetUserOrderDTOById_Found() {
        User user = new User();
        user.setId(1L);

        UserOrder order = new UserOrder();
        order.setId(1L);
        order.setUser(user);

        when(userOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        UserOrderDTO result = userOrderService.getUserOrderDTOById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }


    @Test
    void testGetUserOrderDTOById_NotFound() {
        when(userOrderRepository.findById(1L)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> userOrderService.getUserOrderDTOById(1L));
        assertEquals("User order with id 1 not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testCreateUserOrder_Success() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserOrderDTO userOrderDTO = new UserOrderDTO();
        userOrderDTO.setUserId(userId);
        userOrderDTO.setDeliveryAddress("Address");
        userOrderDTO.setDeliveryTime(LocalTime.now());
        userOrderDTO.setDeliveryDate(LocalDate.now());
        userOrderDTO.setStatus(OrderStatus.PENDING);

        OrderDetailsDTO orderDetails = new OrderDetailsDTO();
        orderDetails.setBookId(1L);
        orderDetails.setAmt(1);
        userOrderDTO.setOrderDetailsDTO(Collections.singletonList(orderDetails));

        UserOrder userOrder = new UserOrder();
        userOrder.setId(1L);
        userOrder.setUser(user);

        when(userOrderRepository.save(any(UserOrder.class))).thenAnswer(invocation -> {
            UserOrder order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        when(userOrderRepository.findById(1L)).thenReturn(Optional.of(userOrder));

        UserOrderDTO result = userOrderService.createUserOrder(userOrderDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderBookService, times(1)).addBookToUserOrder(any(OrderBookDTO.class));
        verify(cartBookService, times(1)).clearUserCartBooks(userId);
    }

    @Test
    void testEditUserOrderStatus_Success() {
        Long orderId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        UserOrder order = new UserOrder();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        UserOrderDTO userOrderDTO = new UserOrderDTO();
        userOrderDTO.setStatus(OrderStatus.SHIPPED);

        when(userOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userOrderRepository.save(any(UserOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserOrderDTO result = userOrderService.editUserOrderStatus(orderId, userOrderDTO);

        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
    }

    @Test
    void testEditUserOrderStatus_NoStatus() {
        Long orderId = 1L;

        UserOrder existingOrder = new UserOrder();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.PENDING);

        when(userOrderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        UserOrderDTO userOrderDTO = new UserOrderDTO();
        AppException exception = assertThrows(AppException.class, () -> userOrderService.editUserOrderStatus(orderId, userOrderDTO));

        assertEquals("Order status is required.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void testDeleteUserOrderById_Success() {
        when(userOrderRepository.existsById(1L)).thenReturn(true);
        userOrderService.deleteUserOrderById(1L);
        verify(userOrderRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUserOrderById_NotFound() {
        when(userOrderRepository.existsById(1L)).thenReturn(false);
        AppException exception = assertThrows(AppException.class, () -> userOrderService.deleteUserOrderById(1L));
        assertEquals("User order with id 1 not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
