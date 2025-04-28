package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.BookDTO;
import by.bsuir.bookplatform.DTO.CartBookDTO;
import by.bsuir.bookplatform.exceptions.AppException;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CartBooksTool {

    private static final Logger logger = LoggerFactory.getLogger(CartBooksTool.class);
    private final CartBookService cartBookService;
    private final BookService bookService;

    private static final int MAX_BOOKS_IN_CART = 9;

    @Tool("Получить список книг в корзине пользователя. Возвращает книги с названием, автором, ценой за единицу, количеством и общей стоимостью. Используй для запросов вроде 'покажи мою корзину' или 'что в корзине?'. Требуется ID пользователя.")
    public String getUserCartBooks(Long userId) {
        try {
            List<CartBookDTO> cartBooks = cartBookService.getUserCartBooks(userId);
            if (cartBooks.isEmpty()) {
                return "Ваша корзина пуста.";
            }

            StringBuilder response = new StringBuilder("**Книги в вашей корзине**:\n");
            for (CartBookDTO cartBook : cartBooks) {
                BookDTO book = bookService.getBookDTOById(cartBook.getBookId());
                double totalCost = book.getCost() * cartBook.getAmt();

                response.append("- **").append(book.getTitle())
                        .append("** (*").append(book.getAuthor())
                        .append("*, ").append(book.getCost()).append(" BYN за шт., ")
                        .append(cartBook.getAmt()).append(" шт., итого: **")
                        .append(String.format("%.2f", totalCost)).append(" BYN**)\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching cart for user {}: {}", userId, e.getMessage());
            return "Ошибка при получении корзины: " + e.getMessage();
        }
    }

    @Tool("Добавить книгу в корзину пользователя. Требуется название книги, ID пользователя и количество (по умолчанию 1). Максимальное количество одинаковых книг в корзине - 9. Если такая книга уже была в корзине, уведомить об этом пользователя. Используй для запросов вроде 'добавить книгу в корзину' или 'положить Властелин колец в корзину'.")
    public String addBookToUserCart(String bookTitle, Long userId, Integer amount) {
        try {
            if (amount == null || amount <= 0) {
                amount = 1;
            }

            BookDTO book = bookService.getBookDTOByTitle(bookTitle);
            if (book.getAmt() < amount) {
                return String.format("Книга **%s** недоступна в нужном количестве. В наличии: %d шт.", book.getTitle(), book.getAmt());
            }

            CartBookDTO cartBookDTO = new CartBookDTO();
            cartBookDTO.setBookId(book.getId());
            cartBookDTO.setUserId(userId);
            cartBookDTO.setAmt(amount);

            try {
                // Проверяем, есть ли книга в корзине
                CartBookDTO existingCartBook = cartBookService.getCartBookDTOById(book.getId(), userId);
                // Если книга есть, обновляем количество
                int newAmount = Math.min(existingCartBook.getAmt() + amount, MAX_BOOKS_IN_CART);
                String result = editBookInUserCart(bookTitle, userId, newAmount);
                return String.format("Книга **%s** уже была в корзине. %s", book.getTitle(), result);
            } catch (AppException e) {
                // Если книга не найдена в корзине, проверяем, что amount не превышает лимит
                if (amount > MAX_BOOKS_IN_CART) {
                    cartBookDTO.setAmt(MAX_BOOKS_IN_CART);
                }
                CartBookDTO addedBook = cartBookService.addBookToUserCart(cartBookDTO);
                return String.format("Книга **%s** (*%s*, %d шт.) успешно добавлена в вашу корзину.",
                        book.getTitle(), book.getAuthor(), addedBook.getAmt());
            }
        } catch (AppException e) {
            logger.error("Error adding book {} to cart for user {}: {}", bookTitle, userId, e.getMessage());
            return "Ошибка при добавлении книги в корзину: " + e.getMessage();
        }
    }

    @Tool("Изменить количество книги в корзине пользователя. Требуется название книги, ID пользователя и новое количество. Максимальное количество одинаковых книг в корзине - 9. Используй для запросов вроде 'изменить количество книги в корзине' или 'поменять на 2 штуки Властелин колец'.")
    public String editBookInUserCart(String bookTitle, Long userId, Integer amount) {
        try {
            if (amount == null || amount <= 0) {
                return "Количество должно быть больше 0.";
            }

            BookDTO book = bookService.getBookDTOByTitle(bookTitle);
            if (book.getAmt() < amount) {
                return String.format("Книга **%s** недоступна в нужном количестве. В наличии: %d шт.", book.getTitle(), book.getAmt());
            }

            int finalAmount = Math.min(amount, MAX_BOOKS_IN_CART);
            CartBookDTO cartBookDTO = new CartBookDTO();
            cartBookDTO.setBookId(book.getId());
            cartBookDTO.setUserId(userId);
            cartBookDTO.setAmt(finalAmount);

            CartBookDTO updatedBook = cartBookService.editBookInUserCart(cartBookDTO);
            String response = String.format("Количество книги **%s** (*%s*) в вашей корзине изменено на **%d** шт.",
                    book.getTitle(), book.getAuthor(), updatedBook.getAmt());
            if (amount > MAX_BOOKS_IN_CART) {
                response += String.format(" Максимальное количество (%d шт.) было установлено, так как запрошенное количество превышает лимит.", MAX_BOOKS_IN_CART);
            }
            return response;
        } catch (AppException e) {
            logger.error("Error editing book {} in cart for user {}: {}", bookTitle, userId, e.getMessage());
            return "Ошибка при изменении количества книги: " + e.getMessage();
        }
    }

    @Tool("Удалить книгу из корзины пользователя. Требуется ID книги и ID пользователя. Используй для запросов вроде 'удалить книгу из корзины' или 'убрать Властелин колец из корзины'.")
    public String removeBookFromUserCart(Long bookId, Long userId) {
        try {
            BookDTO book = bookService.getBookDTOById(bookId);
            cartBookService.removeBookFromUserCart(bookId, userId);

            return String.format("Книга **%s** (*%s*) успешно удалена из вашей корзины.",
                    book.getTitle(), book.getAuthor());
        } catch (AppException e) {
            logger.error("Error removing book {} from cart for user {}: {}", bookId, userId, e.getMessage());
            return "Ошибка при удалении книги из корзины: " + e.getMessage();
        }
    }

    @Tool("Очистить корзину пользователя. Требуется ID пользователя. Используй для запросов вроде 'очистить корзину' или 'удалить все из корзины'.")
    public String clearUserCartBooks(Long userId) {
        try {
            cartBookService.clearUserCartBooks(userId);
            return "Ваша корзина успешно очищена.";
        } catch (AppException e) {
            logger.error("Error clearing cart for user {}: {}", userId, e.getMessage());
            return "Ошибка при очистке корзины: " + e.getMessage();
        }
    }

    @Tool("Получить общую стоимость всех книг в корзине пользователя. Требуется ID пользователя. Используй для запросов вроде 'сколько стоит моя корзина?' или 'какая общая стоимость корзины?'.")
    public String getCartTotalCost(Long userId) {
        try {
            List<CartBookDTO> cartBooks = cartBookService.getUserCartBooks(userId);
            if (cartBooks.isEmpty()) {
                return "Ваша корзина пуста, общая стоимость: **0.00 BYN**.";
            }

            double totalCost = 0;
            for (CartBookDTO cartBook : cartBooks) {
                BookDTO book = bookService.getBookDTOById(cartBook.getBookId());
                totalCost += book.getCost() * cartBook.getAmt();
            }

            return String.format("**Общая стоимость вашей корзины**: %.2f BYN", totalCost);
        } catch (AppException e) {
            logger.error("Error calculating cart total for user {}: {}", userId, e.getMessage());
            return "Ошибка при подсчёте общей стоимости корзины: " + e.getMessage();
        }
    }
}