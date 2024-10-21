package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                .filter(b -> b.getAuthor().equalsIgnoreCase(book.getAuthor()) &&
                        b.getPublisher().equalsIgnoreCase(book.getPublisher()) &&
                        b.getPublicationYear().equals(book.getPublicationYear()))
                .findFirst();

        if (existingBook.isPresent()) {
            throw new AppException("A book already exists with the same author, publisher and publication year.", HttpStatus.CONFLICT);
        }
        return bookRepository.save(book);
    }

    public Book editBook(Long id, Book bookDetails) {
        Optional<Book> existingBookOpt = bookRepository.findById(id);
        if (existingBookOpt.isEmpty()) {
            throw new AppException("Book with id " + id + " not found.", HttpStatus.NOT_FOUND);
        }

        Book existingBook = existingBookOpt.get();

        existingBook.setAuthor(bookDetails.getAuthor());
        existingBook.setPublisher(bookDetails.getPublisher());
        existingBook.setPublicationYear(bookDetails.getPublicationYear());
        existingBook.setDescription(bookDetails.getDescription());
        existingBook.setCost(bookDetails.getCost());
        existingBook.setAmt(bookDetails.getAmt());
        existingBook.setPages(bookDetails.getPages());
        existingBook.setHardcover(bookDetails.getHardcover());
        existingBook.setGenres(bookDetails.getGenres());
        existingBook.setMedia(bookDetails.getMedia());

        return bookRepository.save(existingBook);
    }

    public void deleteBookById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new AppException("Book not found.", HttpStatus.NOT_FOUND);
        }
        bookRepository.deleteById(id);
    }
}