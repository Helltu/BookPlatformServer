package by.bsuir.bookplatform.controllers;

import by.bsuir.bookplatform.DTO.*;
import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.entities.OrderStatus;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.services.BookService;
import by.bsuir.bookplatform.services.GenreService;
import by.bsuir.bookplatform.services.MediaService;
import by.bsuir.bookplatform.services.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("api/manager")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManagerController {

    private final BookService bookService;
    private final GenreService genreService;
    private final MediaService mediaService;
    private final UserOrderService userOrderService;

    @GetMapping("/orders")
    public List<UserOrderDTO> getAllOrders() {
        return userOrderService.getAllUserOrdersDTO();
    }

    @PostMapping("/books")
    public BookDTO createBook(@RequestBody BookDTO bookDTO) {
        return bookService.createBook(bookDTO);
    }

    @PostMapping("/genres")
    public GenreDTO createGenre(@RequestBody GenreDTO genreDTO) {
        return genreService.createGenre(genreDTO);
    }

    @PostMapping("/media")
    public MediaDTO createMedia(@RequestBody MediaDTO mediaDTO) {
        return mediaService.createMedia(mediaDTO);
    }

    @PutMapping("/books/{bookId}/genres/{genreId}")
    public BookDTO addGenreToBook(@PathVariable Long bookId, @PathVariable Long genreId) {
        return bookService.addGenreToBook(bookId, genreId);
    }

    @PutMapping("/books/{bookId}/media")
    public BookDTO addMediaToBook(@PathVariable Long bookId, @RequestParam("media") MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            MediaDTO mediaDTO = new MediaDTO();
            mediaDTO.setMedia(fileBytes);
            mediaDTO.setBookId(bookId);
            return bookService.addMediaToBook(bookId, mediaDTO);
        } catch (IOException e) {
            throw new AppException("Размер файла слишком велик.",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/books/{id}")
    public BookDTO editBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        return bookService.editBook(id, bookDTO);
    }

    @PutMapping("/genres/{id}")
    public GenreDTO editGenre(@PathVariable Long id, @RequestBody GenreDTO genreDTO) {
        return genreService.editGenre(id, genreDTO);
    }

    @PutMapping("/orders/{id}")
    public UserOrderDTO editUserOrderStatus(@PathVariable Long id, @RequestBody UserOrderDTO userOrderDTO) {
        return userOrderService.editUserOrderStatus(id, userOrderDTO);
    }

    @DeleteMapping("/books/{id}")
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }

    @DeleteMapping("/genres/{id}")
    public void deleteGenreById(@PathVariable Long id) {
        genreService.deleteGenreById(id);
    }

    @DeleteMapping("/media/{id}")
    public void deleteMediaById(@PathVariable Long id) {
        mediaService.deleteMediaById(id);
    }

    @DeleteMapping("/books/{bookId}/genres/{genreId}")
    public BookDTO deleteGenreFromBook(@PathVariable Long bookId, @PathVariable Long genreId) {
        return bookService.deleteGenreFromBook(bookId, genreId);
    }

    @DeleteMapping("/orders/{id}")
    public void deleteOrderById(@PathVariable Long id) {
        userOrderService.deleteUserOrderById(id);
    }
}

