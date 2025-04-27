package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.OrderBookDTO;
import by.bsuir.bookplatform.DTO.OrderDetailsDTO;
import by.bsuir.bookplatform.DTO.UserOrderDTO;
import by.bsuir.bookplatform.entities.*;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.UserOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserOrderService {

    private final UserOrderRepository userOrderRepository;
    private final OrderBookService orderBookService;
    private final CartBookService cartBookService;

    public List<UserOrderDTO> getAllUserOrdersDTO() {
        return userOrderRepository.findAll().stream().map(UserOrderDTO::new).toList();
    }

    public List<UserOrderDTO> getUserOrdersDTO(Long userId) {
        return getAllUserOrdersDTO().stream().filter(o -> o.getUserId().equals(userId)).toList();
    }

    public UserOrderDTO getUserOrderDTOById(Long id) {
        Optional<UserOrder> userOrderOpt = userOrderRepository.findById(id);
        if (!userOrderOpt.isPresent())
            throw new AppException("User order with id " + id + " not found.", HttpStatus.NOT_FOUND);

        return new UserOrderDTO(userOrderOpt.get());
    }

    public UserOrder getUserOrderById(Long id) {
        Optional<UserOrder> userOrderOpt = userOrderRepository.findById(id);
        if (!userOrderOpt.isPresent())
            throw new AppException("User order with id " + id + " not found.", HttpStatus.NOT_FOUND);

        return userOrderOpt.get();
    }

    public UserOrderDTO createUserOrder(UserOrderDTO userOrderDTO) {
        userOrderDTO.checkValues();

        if(userOrderDTO.getStatus() == null)
            userOrderDTO.setStatus(OrderStatus.PENDING);

        List<OrderDetailsDTO> orderDetailsDTOs = userOrderDTO.getOrderDetailsDTO();
        userOrderDTO.setOrderDetailsDTO(new ArrayList<OrderDetailsDTO>());

        UserOrder userOrder = new UserOrder();
        userOrder = DTOMapper.getInstance().map(userOrderDTO, userOrder.getClass());
        userOrder.setOrderTime(LocalTime.now());
        userOrder.setOrderDate(LocalDate.now());
        userOrder = userOrderRepository.save(userOrder);

        UserOrder finalUserOrder = userOrder;
        orderDetailsDTOs.forEach(orderDetailsDTO -> {
            orderBookService.addBookToUserOrder(new OrderBookDTO(finalUserOrder.getId(), orderDetailsDTO.getBookId(), orderDetailsDTO.getAmt()));
        });

        cartBookService.clearUserCartBooks(userOrderDTO.getUserId());

        return getUserOrderDTOById(userOrder.getId());
    }

    public UserOrderDTO cancelUserOrder(Long id, Long userId) {
        UserOrder userOrder = getUserOrderById(id);

        if (!userOrder.getUser().getId().equals(userId)) {
            throw new AppException("You can only cancel your own orders.", HttpStatus.FORBIDDEN);
        }

        if (userOrder.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Only orders with PENDING status can be cancelled.", HttpStatus.BAD_REQUEST);
        }

        userOrder.setStatus(OrderStatus.CANCELLED);
        return new UserOrderDTO(userOrderRepository.save(userOrder));
    }

    public UserOrderDTO editUserOrderStatus(Long id, UserOrderDTO userOrderDTO) {
        UserOrder userOrder = getUserOrderById(id);

        if (userOrderDTO.getStatus() == null) {
            throw new AppException("Order status is required.", HttpStatus.BAD_REQUEST);
        }

        userOrder.setStatus(userOrderDTO.getStatus());
        return new UserOrderDTO(userOrderRepository.save(userOrder));
    }

    public void deleteUserOrderById(Long id) {
        if (!userOrderRepository.existsById(id)) {
            throw new AppException("User order with id " + id + " not found.", HttpStatus.NOT_FOUND);
        }
        userOrderRepository.deleteById(id);
    }
}