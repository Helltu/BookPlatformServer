package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.OrderBookDTO;
import by.bsuir.bookplatform.entities.*;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.BookRepository;
import by.bsuir.bookplatform.repositories.OrderBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderBookService {
    private final OrderBookRepository orderBookRepository;
    private final BookRepository bookRepository;

    public List<OrderBookDTO> getAllCartBooksDTO() {
        return orderBookRepository.findAll().stream().map(OrderBookDTO::new).toList();
    }

    public OrderBookDTO getOrderBookDTOById(Long bookId, Long orderId) {
        OrderBookId id = new OrderBookId(bookId, orderId);

        Optional<OrderBook> orderBookOpt = orderBookRepository.findById(id);
        if (!orderBookOpt.isPresent())
            throw new AppException("In order " + id.getOrderId() + " order book " + id.getBookId() + " not found.", HttpStatus.NOT_FOUND);

        return new OrderBookDTO(orderBookOpt.get());
    }

    public OrderBook getOrderBookById(Long bookId, Long orderId) {
        OrderBookId id = new OrderBookId(bookId, orderId);

        Optional<OrderBook> orderBookOpt = orderBookRepository.findById(id);
        if (!orderBookOpt.isPresent())
            throw new AppException("In order " + id.getOrderId() + " order book " + id.getBookId() + " not found.", HttpStatus.NOT_FOUND);

        return orderBookOpt.get();
    }

    public OrderBookDTO editBookInUserOrder(Long bookId, Long orderId, OrderBookDTO orderBookDTODetails) {
        OrderBook existingOrderBook = getOrderBookById(bookId, orderId);

        if (orderBookDTODetails.getAmt() != null)
            orderBookDTODetails.setAmt(orderBookDTODetails.getAmt());

        existingOrderBook = orderBookRepository.save(existingOrderBook);

        return new OrderBookDTO(existingOrderBook);
    }

    public OrderBookDTO addBookToUserOrder(OrderBookDTO orderBookDTO) {
        orderBookDTO.checkValues();

        Optional<OrderBook> orderBookOpt = orderBookRepository.findById(new OrderBookId(orderBookDTO.getBookId(), orderBookDTO.getOrderId()));

        if (orderBookOpt.isPresent())
            throw new AppException("In order " + orderBookDTO.getOrderId() + " order book " + orderBookDTO.getBookId() + " already exists.", HttpStatus.CONFLICT);

        Long id = orderBookDTO.getBookId();

        Optional<Book> bookOpt = bookRepository.findById(id);
        if (!bookOpt.isPresent())
            throw new AppException("Book with id " + id + " not found.", HttpStatus.NOT_FOUND);

        Book book = bookOpt.get();

        if (orderBookDTO.getAmt() > book.getAmt())
            throw new AppException("Not enough books with id " + book.getId() + ", present: " + book.getAmt() + ",need " + orderBookDTO.getAmt() + '.', HttpStatus.CONFLICT);

        book.setAmt(book.getAmt() - orderBookDTO.getAmt());
        bookRepository.save(book);

        OrderBook orderBook = new OrderBook();
        orderBook = DTOMapper.getInstance().map(orderBookDTO, orderBook.getClass());

        orderBook = orderBookRepository.save(orderBook);

        return new OrderBookDTO(orderBook);
    }

    public void removeBookFromUserOrder(Long bookId, Long orderId) {
        OrderBookId orderBookId = new OrderBookId(bookId, orderId);

        Optional<OrderBook> orderBookOpt = orderBookRepository.findById(orderBookId);

        if (!orderBookOpt.isPresent())
            throw new AppException("In order " + orderBookId.getOrderId() + " order book " + orderBookId.getBookId() + " not found.", HttpStatus.NOT_FOUND);

        orderBookRepository.deleteById(orderBookId);
    }
}
