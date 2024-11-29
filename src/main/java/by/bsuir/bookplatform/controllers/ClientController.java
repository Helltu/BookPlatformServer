package by.bsuir.bookplatform.controllers;

import by.bsuir.bookplatform.DTO.CartBookDTO;
import by.bsuir.bookplatform.DTO.ReviewDTO;
import by.bsuir.bookplatform.DTO.UserOrderDTO;
import by.bsuir.bookplatform.services.CartBookService;
import by.bsuir.bookplatform.services.ReviewService;
import by.bsuir.bookplatform.services.UserOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/client")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClientController {
    private final CartBookService cartBookService;
    private final UserOrderService userOrderService;
    private final ReviewService reviewService;

    @GetMapping("{userId}/cart")
    public List<CartBookDTO> getUserCart(@PathVariable Long userId) {
        return cartBookService.getUserCartBooks(userId);
    }

    @PutMapping("/cart")
    public CartBookDTO editBookInCart(@RequestBody CartBookDTO cartBookDTODetails) {
        return cartBookService.editBookInUserCart(cartBookDTODetails);
    }

    @PostMapping("/cart")
    public CartBookDTO placeBookInCart(@RequestBody CartBookDTO cartBookDTO) {
        return cartBookService.addBookToUserCart(cartBookDTO);
    }

    @DeleteMapping("/cart")
    public void removeBookFromCart(@RequestBody CartBookDTO cartBookDTO) {
        cartBookService.removeBookFromUserCart(cartBookDTO.getBookId(), cartBookDTO.getUserId());
    }

    @PostMapping("/orders")
    public UserOrderDTO makeOrder(@RequestBody UserOrderDTO userOrderDTO) {
        return userOrderService.createUserOrder(userOrderDTO);
    }

    @PostMapping("/reviews")
    public ReviewDTO writeReview(@RequestBody ReviewDTO reviewDTO) {
        return  reviewService.createReview(reviewDTO);
    }

    @PutMapping("/reviews")
    public ReviewDTO editReview(@RequestBody ReviewDTO reviewDTO) {
        return reviewService.editReview(reviewDTO);
    }

    @GetMapping("/reviews")
    public ReviewDTO getReviewById(@RequestParam Long userId, @RequestParam Long bookId) {
        return reviewService.getReviewDTOById(bookId, userId);
    }
}
