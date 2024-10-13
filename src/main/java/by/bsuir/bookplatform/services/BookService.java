package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> findBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Book saveBook(Book book) {
        Optional<Book> existingBook = bookRepository.findAll().stream()
                .filter(b -> b.getAuthor().equals(book.getAuthor()) &&
                        b.getPublisher().equals(book.getPublisher()) &&
                        b.getDescription().equals(book.getDescription()))
                .findFirst();

        if (existingBook.isPresent()) {
            throw new IllegalArgumentException("Книга уже существует с тем же автором и издателем.");
        }
        return bookRepository.save(book);
    }

    public Book editBook(Long id, Book bookDetails) {
        Optional<Book> existingBookOpt = bookRepository.findById(id);
        if (existingBookOpt.isEmpty()) {
            throw new IllegalArgumentException("Книга с id " + id + " не найдена.");
        }

        Book existingBook = existingBookOpt.get();

        existingBook.setAuthor(bookDetails.getAuthor());
        existingBook.setPublisher(bookDetails.getPublisher());
        existingBook.setPublicationYear(bookDetails.getPublicationYear());
        existingBook.setDescription(bookDetails.getDescription());
        existingBook.setCost(bookDetails.getCost());
        existingBook.setAmt(bookDetails.getAmt());

        return bookRepository.save(existingBook);
    }

    public void deleteBookById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Книга не найдена.");
        }
        bookRepository.deleteById(id);
    }
}

