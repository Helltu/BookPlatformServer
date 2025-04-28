package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.BookDTO;
import by.bsuir.bookplatform.DTO.CartBookDTO;
import by.bsuir.bookplatform.DTO.OrderDetailsDTO;
import by.bsuir.bookplatform.DTO.UserOrderDTO;
import by.bsuir.bookplatform.entities.OrderStatus;
import by.bsuir.bookplatform.exceptions.AppException;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserOrdersTool {

    private static final Logger logger = LoggerFactory.getLogger(UserOrdersTool.class);
    private final UserOrderService userOrderService;
    private final BookService bookService;
    private final CartBookService cartBookService;

    @Tool("Получить список всех заказов пользователя. Возвращает краткую информацию: номер заказа, дата заказа, статус, общая стоимость. Требуется ID пользователя. Используй для запросов вроде 'покажи мои заказы' или 'какие у меня заказы?'.")
    public String getUserOrders(Long userId) {
        try {
            List<UserOrderDTO> orders = userOrderService.getUserOrdersDTO(userId);
            if (orders.isEmpty()) {
                return "У вас пока нет заказов.";
            }

            StringBuilder response = new StringBuilder("**Ваши заказы**:\n");
            for (UserOrderDTO order : orders) {
                double totalCost = calculateOrderTotalCost(order);
                String orderNumber = "ORD-" + order.getId();
                String orderDate = order.getOrderDate() != null
                        ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        : "не указана";

                response.append("- **").append(orderNumber)
                        .append("** (дата: ").append(orderDate)
                        .append(", статус: ").append(order.getStatus().toString())
                        .append(", общая стоимость: **").append(String.format("%.2f BYN", totalCost))
                        .append("**)\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching orders for user {}: {}", userId, e.getMessage());
            return "Ошибка при получении списка заказов: " + e.getMessage();
        }
    }

    @Tool("Получить подробную информацию о заказе пользователя по номеру заказа. Возвращает номер заказа, дату и время заказа, статус, адрес доставки, дату и время доставки, комментарий, список книг с количеством и стоимостью, общую стоимость. Требуется ID пользователя и номер заказа (например, 'ORD-123'). Используй для запросов вроде 'расскажи о моём заказе ORD-123'.")
    public String getOrderDetails(Long userId, String orderNumber) {
        try {
            Long orderId = parseOrderNumber(orderNumber);
            UserOrderDTO order = userOrderService.getUserOrderDTOById(orderId);

            if (!order.getUserId().equals(userId)) {
                return "Вы можете просматривать только свои заказы.";
            }

            StringBuilder response = new StringBuilder("**Информация о заказе ").append(orderNumber).append("**:\n");
            String orderDate = order.getOrderDate() != null
                    ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    : "не указана";
            String orderTime = order.getOrderTime() != null
                    ? order.getOrderTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "не указано";
            String deliveryDate = order.getDeliveryDate() != null
                    ? order.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    : "не указана";
            String deliveryTime = order.getDeliveryTime() != null
                    ? order.getDeliveryTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "не указано";
            String comment = order.getComment() != null ? order.getComment() : "отсутствует";

            response.append("- **Дата и время заказа**: ").append(orderDate).append(" ").append(orderTime).append("\n")
                    .append("- **Статус**: ").append(order.getStatus().toString()).append("\n")
                    .append("- **Адрес доставки**: ").append(order.getDeliveryAddress()).append("\n")
                    .append("- **Дата и время доставки**: ").append(deliveryDate).append(" ").append(deliveryTime).append("\n")
                    .append("- **Комментарий**: ").append(comment).append("\n")
                    .append("- **Книги в заказе**:\n");

            double totalCost = 0;
            for (OrderDetailsDTO detail : order.getOrderDetailsDTO()) {
                BookDTO book = bookService.getBookDTOById(detail.getBookId());
                double bookTotal = book.getCost() * detail.getAmt();
                totalCost += bookTotal;

                response.append("  - **").append(book.getTitle())
                        .append("** (*").append(book.getAuthor())
                        .append("*, ").append(detail.getAmt()).append(" шт., ")
                        .append(String.format("%.2f BYN", bookTotal)).append(")\n");
            }

            response.append("- **Общая стоимость**: ").append(String.format("%.2f BYN", totalCost)).append("\n");
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching order details for user {} and order {}: {}", userId, orderNumber, e.getMessage());
            return "Ошибка при получении информации о заказе: " + e.getMessage();
        }
    }

    @Tool("Оформить заказ из корзины пользователя. Требуется ID пользователя, адрес доставки, дата доставки (формат 'dd.MM.yyyy'), время доставки (формат 'HH:mm'), комментарий (опционально) и подтверждение (true/false). Каждый раз перед оформлением заказа УТОЧНЯЙ заново все данные о доставке. Используй для запросов вроде 'оформить заказ из корзины' или 'создать заказ с доставкой на адрес X'. Проверяет наличие книг в корзине и запрашивает подтверждение.")
    public String createOrder(Long userId, String deliveryAddress, String deliveryDate, String deliveryTime, String comment, Boolean confirm) {
        try {
            List<CartBookDTO> cartBooks = cartBookService.getUserCartBooks(userId);
            if (cartBooks.isEmpty()) {
                return "Ваша корзина пуста. Добавьте книги в корзину перед оформлением заказа.";
            }

            if (confirm == null || !confirm) {
                StringBuilder response = new StringBuilder("Вы собираетесь оформить заказ с доставкой по адресу: ")
                        .append(deliveryAddress).append(", на дату: ").append(deliveryDate)
                        .append(", время: ").append(deliveryTime);
                if (comment != null && !comment.trim().isEmpty()) {
                    response.append(", с комментарием: ").append(comment);
                }
                response.append(".\n**Книги в заказе**:\n");
                double totalCost = 0;
                for (CartBookDTO cartBook : cartBooks) {
                    BookDTO book = bookService.getBookDTOById(cartBook.getBookId());
                    double bookTotal = book.getCost() * cartBook.getAmt();
                    totalCost += bookTotal;
                    response.append("- **").append(book.getTitle())
                            .append("** (*").append(book.getAuthor())
                            .append("*, ").append(cartBook.getAmt()).append(" шт., ")
                            .append(String.format("%.2f BYN", bookTotal)).append(")\n");
                }
                response.append("**Общая стоимость**: ").append(String.format("%.2f BYN", totalCost))
                        .append("\nПодтвердите оформление заказа, указав confirm=true.");
                return response.toString();
            }

            UserOrderDTO orderDTO = new UserOrderDTO();
            orderDTO.setUserId(userId);
            orderDTO.setDeliveryAddress(deliveryAddress);
            orderDTO.setDeliveryDate(LocalDate.parse(deliveryDate, DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            orderDTO.setDeliveryTime(LocalTime.parse(deliveryTime, DateTimeFormatter.ofPattern("HH:mm")));
            orderDTO.setComment(comment);
            orderDTO.setStatus(OrderStatus.PENDING);

            for (CartBookDTO cartBook : cartBooks) {
                OrderDetailsDTO detail = new OrderDetailsDTO();
                detail.setBookId(cartBook.getBookId());
                detail.setAmt(cartBook.getAmt());
                orderDTO.getOrderDetailsDTO().add(detail);
            }

            UserOrderDTO createdOrder = userOrderService.createUserOrder(orderDTO);
            String orderNumber = "ORD-" + createdOrder.getId();

            return String.format("Заказ **%s** успешно оформлен! Ожидайте доставку по адресу %s, %s в %s.",
                    orderNumber, deliveryAddress, deliveryDate, deliveryTime);
        } catch (AppException e) {
            logger.error("Error creating order for user {}: {}", userId, e.getMessage());
            return "Ошибка при оформлении заказа: " + e.getMessage();
        } catch (Exception e) {
            logger.error("Invalid date or time format for user {}: {}", userId, e.getMessage());
            return "Ошибка: неверный формат даты (ожидается dd.MM.yyyy) или времени (ожидается HH:mm).";
        }
    }

    @Tool("Отменить заказ пользователя по номеру заказа. Требуется ID пользователя, номер заказа (например, 'ORD-123') и подтверждение (true/false). Работает только для заказов со статусом PENDING. Используй для запросов вроде 'отменить заказ ORD-123'.")
    public String cancelOrder(Long userId, String orderNumber, Boolean confirm) {
        try {
            Long orderId = parseOrderNumber(orderNumber);
            UserOrderDTO order = userOrderService.getUserOrderDTOById(orderId);

            if (!order.getUserId().equals(userId)) {
                return "Вы можете отменять только свои заказы.";
            }

            if (order.getStatus() != OrderStatus.PENDING) {
                return "Заказ " + orderNumber + " не может быть отменён, так как его статус: " + order.getStatus();
            }

            if (confirm == null || !confirm) {
                double totalCost = calculateOrderTotalCost(order);
                StringBuilder response = new StringBuilder("Вы собираетесь отменить заказ **").append(orderNumber)
                        .append("** (статус: ").append(order.getStatus())
                        .append(", общая стоимость: ").append(String.format("%.2f BYN", totalCost))
                        .append(").\n**Книги в заказе**:\n");
                for (OrderDetailsDTO detail : order.getOrderDetailsDTO()) {
                    BookDTO book = bookService.getBookDTOById(detail.getBookId());
                    double bookTotal = book.getCost() * detail.getAmt();
                    response.append("- **").append(book.getTitle())
                            .append("** (*").append(book.getAuthor())
                            .append("*, ").append(detail.getAmt()).append(" шт., ")
                            .append(String.format("%.2f BYN", bookTotal)).append(")\n");
                }
                response.append("Подтвердите отмену заказа, указав confirm=true.");
                return response.toString();
            }

            userOrderService.cancelUserOrder(orderId, userId);
            return "Заказ **" + orderNumber + "** успешно отменён.";
        } catch (AppException e) {
            logger.error("Error cancelling order {} for user {}: {}", orderNumber, userId, e.getMessage());
            return "Ошибка при отмене заказа: " + e.getMessage();
        }
    }

    private Long parseOrderNumber(String orderNumber) {
        if (orderNumber == null || !orderNumber.startsWith("ORD-")) {
            throw new AppException("Неверный формат номера заказа. Ожидается 'ORD-123'.", HttpStatus.BAD_REQUEST);
        }
        try {
            return Long.parseLong(orderNumber.replace("ORD-", ""));
        } catch (NumberFormatException e) {
            throw new AppException("Неверный номер заказа: " + orderNumber, HttpStatus.BAD_REQUEST);
        }
    }

    private double calculateOrderTotalCost(UserOrderDTO order) {
        double totalCost = 0;
        for (OrderDetailsDTO detail : order.getOrderDetailsDTO()) {
            BookDTO book = bookService.getBookDTOById(detail.getBookId());
            totalCost += book.getCost() * detail.getAmt();
        }
        return totalCost;
    }
}