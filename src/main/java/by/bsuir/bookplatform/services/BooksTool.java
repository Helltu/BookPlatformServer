package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.BookDTO;
import by.bsuir.bookplatform.DTO.FilterBooksDTO;
import by.bsuir.bookplatform.DTO.GenreDTO;
import by.bsuir.bookplatform.DTO.ReviewDTO;
import by.bsuir.bookplatform.exceptions.AppException;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BooksTool {

    private static final Logger logger = LoggerFactory.getLogger(BooksTool.class);
    private final BookService bookService;
    private final GenreService genreService;

    @Tool("Получить список всех доступных книг в магазине. Возвращает список книг с названием, автором, жанрами, ценой, типом обложки и рейтингом. Используй для запросов вроде 'покажи все книги' или 'какие книги есть в магазине?'.")
    public String getAllBooks() {
        try {
            List<BookDTO> books = bookService.getAllBooksDTO();
            if (books.isEmpty()) {
                return "В магазине пока нет доступных книг.";
            }

            StringBuilder response = new StringBuilder("**Доступные книги в магазине**:\n");
            for (BookDTO book : books) {
                Set<String> genres = bookService.getBookGenres(book.getId()).stream()
                        .map(GenreDTO::getName)
                        .collect(Collectors.toSet());
                String genresString = genres.isEmpty() ? "не указаны" : String.join(", ", genres);

                response.append("- **").append(book.getTitle())
                        .append("** (*").append(book.getAuthor())
                        .append("*, ").append(genresString)
                        .append(", ").append(book.getCost()).append(" BYN")
                        .append(", ").append(book.getHardcover() ? "твёрдая обложка" : "мягкая обложка")
                        .append(", рейтинг: **").append(bookService.getBookAverageRatingById(book.getId()))
                        .append("**)\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching all books: {}", e.getMessage());
            return "Ошибка при получении списка книг: " + e.getMessage();
        }
    }

    @Tool("Только для внутреннего использования (например, если пользователь просит положить книгу с конерктным названием в корзину). Пользователь никогда не должен видеть id книг")
    public String getBookIdByTitle(String bookName) {
        return bookService.getBookDTOByTitle(bookName).getId().toString();
    }

    @Tool("Только для внутреннего использования. Пользователь никогда не должен видеть id жанров")
    public String getGenreIdByName(String genreName) {
        return genreService.getGenreDTOByName(genreName).getId().toString();
    }

    @Tool("Получить подробную информацию о книге по её названию. Возвращает название, автора, жанры, издателя, год издания, описание, стоимость, тип обложки, количество страниц, количество в наличии и рейтинг. Используй для запросов вроде 'расскажи о книге Властелин колец'.")
    public String getBookDetails(String bookTitle) {
        try {
            BookDTO book = bookService.getBookDTOByTitle(bookTitle);
            StringBuilder response = new StringBuilder("**Информация о книге**:\n");
            response.append("- **Название**: ").append(book.getTitle()).append("\n")
                    .append("- **Автор**: ").append(book.getAuthor()).append("\n")
                    .append("- **Издатель**: ").append(book.getPublisher()).append("\n")
                    .append("- **Год издания**: ").append(book.getPublicationYear()).append("\n")
                    .append("- **Описание**: ").append(book.getDescription()).append("\n")
                    .append("- **Стоимость**: ").append(book.getCost()).append(" BYN\n")
                    .append("- **Тип обложки**: ").append(book.getHardcover() ? "твёрдая" : "мягкая").append("\n")
                    .append("- **Количество страниц**: ").append(book.getPages()).append("\n")
                    .append("- **Количество в наличии**: ").append(book.getAmt()).append("\n")
                    .append("- **Средний рейтинг**: ").append(bookService.getBookAverageRatingById(book.getId())).append("\n");

            Set<String> genres = bookService.getBookGenres(book.getId()).stream()
                    .map(GenreDTO::getName)
                    .collect(Collectors.toSet());
            response.append("- **Жанры**: ").append(genres.isEmpty() ? "не указаны" : String.join(", ", genres)).append("\n");

            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching book details for title {}: {}", bookTitle, e.getMessage());
            return "К сожалению, книга с названием \"" + bookTitle + "\" не найдена. Пожалуйста, проверьте название.";
        }
    }

    @Tool("Получить список всех жанров в магазине. Возвращает названия жанров. Используй для запросов вроде 'какие жанры есть в магазине?' или 'покажи все жанры'.")
    public String getAllGenres() {
        try {
            List<GenreDTO> genres = genreService.getAllGenresDTO();
            if (genres.isEmpty()) {
                return "В магазине пока нет жанров.";
            }

            StringBuilder response = new StringBuilder("**Доступные жанры в магазине**:\n");
            for (GenreDTO genre : genres) {
                response.append("- ").append(genre.getName()).append("\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching genres: {}", e.getMessage());
            return "Ошибка при получении списка жанров: " + e.getMessage();
        }
    }

    @Tool("Получить список всех авторов книг в магазине. Возвращает уникальные имена авторов. Используй для запросов вроде 'кто авторы книг в магазине?'.")
    public String getAllAuthors() {
        try {
            Set<String> authors = bookService.getAllAuthors();
            if (authors.isEmpty()) {
                return "Авторы в магазине пока отсутствуют.";
            }

            StringBuilder response = new StringBuilder("**Авторы книг в магазине**:\n");
            for (String author : authors) {
                response.append("- ").append(author).append("\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching authors: {}", e.getMessage());
            return "Ошибка при получении списка авторов: " + e.getMessage();
        }
    }

    @Tool("Получить самую популярную книгу на основе среднего рейтинга. Возвращает название книги, автора, жанры, цену, тип обложки и рейтинг. Используй для запросов вроде 'какая самая популярная книга?'.")
    public String getMostPopularBook() {
        try {
            List<BookDTO> books = bookService.getAllBooksDTO();
            if (books.isEmpty()) {
                return "В магазине пока нет доступных книг.";
            }

            BookDTO mostPopularBook = null;
            double highestRating = -1;

            for (BookDTO book : books) {
                double rating = bookService.getBookAverageRatingById(book.getId());
                if (rating > highestRating) {
                    highestRating = rating;
                    mostPopularBook = book;
                }
            }

            if (mostPopularBook == null) {
                return "Не удалось определить популярную книгу, так как нет книг с рейтингами.";
            }

            Set<String> genres = bookService.getBookGenres(mostPopularBook.getId()).stream()
                    .map(GenreDTO::getName)
                    .collect(Collectors.toSet());
            String genresString = genres.isEmpty() ? "не указаны" : String.join(", ", genres);

            return String.format("**Самая популярная книга**:\n- **%s** (*%s*, %s, %.2f BYN, %s, рейтинг: **%.2f**)",
                    mostPopularBook.getTitle(),
                    mostPopularBook.getAuthor(),
                    genresString,
                    mostPopularBook.getCost(),
                    mostPopularBook.getHardcover() ? "твёрдая обложка" : "мягкая обложка",
                    highestRating);
        } catch (AppException e) {
            logger.error("Error fetching most popular book: {}", e.getMessage());
            return "Ошибка при получении популярной книги: " + e.getMessage();
        }
    }

    @Tool("Получить самого популярного автора на основе среднего рейтинга их книг. Возвращает имя автора и средний рейтинг. Используй для запросов вроде 'кто самый популярный автор?'.")
    public String getMostPopularAuthor() {
        try {
            Set<String> authors = bookService.getAllAuthors();
            if (authors.isEmpty()) {
                return "Авторы в магазине пока отсутствуют.";
            }

            String mostPopularAuthor = null;
            double highestAverageRating = -1;

            for (String author : authors) {
                FilterBooksDTO filter = new FilterBooksDTO();
                filter.setAuthors(Collections.singletonList(author));
                List<BookDTO> books = bookService.filterBooks(filter);
                if (books.isEmpty()) continue;

                double averageRating = books.stream()
                        .mapToDouble(book -> bookService.getBookAverageRatingById(book.getId()))
                        .average()
                        .orElse(0);
                logger.debug("Author: {}, Average rating: {}", author, averageRating);
                if (averageRating > highestAverageRating) {
                    highestAverageRating = averageRating;
                    mostPopularAuthor = author;
                }
            }

            if (mostPopularAuthor == null) {
                return "Не удалось определить популярного автора, так как нет книг с рейтингами.";
            }

            return String.format("**Самый популярный автор**: %s (средний рейтинг книг: **%.2f**)", mostPopularAuthor, highestAverageRating);
        } catch (AppException e) {
            logger.error("Error fetching popular author: {}", e.getMessage());
            return "Ошибка при получении популярного автора: " + e.getMessage();
        }
    }

    @Tool("Получить самый популярный жанр на основе среднего рейтинга книг этого жанра. Возвращает название жанра и средний рейтинг. Используй для запросов вроде 'какой самый популярный жанр?'.")
    public String getMostPopularGenre() {
        try {
            List<GenreDTO> genres = genreService.getAllGenresDTO();
            if (genres.isEmpty()) {
                return "Жанры в магазине пока отсутствуют.";
            }

            String mostPopularGenre = null;
            double highestAverageRating = -1;

            for (GenreDTO genre : genres) {
                FilterBooksDTO filter = new FilterBooksDTO();
                filter.setGenres(Collections.singletonList(genre.getName()));
                List<BookDTO> books = bookService.filterBooks(filter);
                if (books.isEmpty()) continue;

                double averageRating = books.stream()
                        .mapToDouble(book -> bookService.getBookAverageRatingById(book.getId()))
                        .average()
                        .orElse(0);
                logger.debug("Genre: {}, Average rating: {}", genre.getName(), averageRating);
                if (averageRating > highestAverageRating) {
                    highestAverageRating = averageRating;
                    mostPopularGenre = genre.getName();
                }
            }

            if (mostPopularGenre == null) {
                return "Не удалось определить популярный жанр, так как нет книг с рейтингами.";
            }

            return String.format("**Самый популярный жанр**: %s (средний рейтинг книг: **%.2f**)", mostPopularGenre, highestAverageRating);
        } catch (AppException e) {
            logger.error("Error fetching popular genre: {}", e.getMessage());
            return "Ошибка при получении популярного жанра: " + e.getMessage();
        }
    }

    @Tool("Получить список всех издателей книг в магазине. Возвращает уникальные названия издателей. Используй для запросов вроде 'какие издатели есть в магазине?'.")
    public String getAllPublishers() {
        try {
            Set<String> publishers = bookService.getAllPublishers();
            if (publishers.isEmpty()) {
                return "Издатели в магазине пока отсутствуют.";
            }

            StringBuilder response = new StringBuilder("**Издатели книг в магазине**:\n");
            for (String publisher : publishers) {
                response.append("- ").append(publisher).append("\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching publishers: {}", e.getMessage());
            return "Ошибка при получении списка издателей: " + e.getMessage();
        }
    }

    @Tool("Получить максимальную стоимость книги в каталоге. Используй для запросов вроде 'какая самая дорогая книга?'.")
    public String getMaxBookCost() {
        try {
            Float maxCost = bookService.getMaxBookCost();
            return String.format("**Максимальная стоимость книги в каталоге**: %.2f BYN", maxCost);
        } catch (AppException e) {
            logger.error("Error fetching max book cost: {}", e.getMessage());
            return "Ошибка при получении максимальной стоимости: " + e.getMessage();
        }
    }

    @Tool("Получить минимальную стоимость книги в каталоге. Используй для запросов вроде 'какая самая дешёвая книга?'.")
    public String getMinBookCost() {
        try {
            Float minCost = bookService.getMinBookCost();
            return String.format("**Минимальная стоимость книги в каталоге**: %.2f BYN", minCost);
        } catch (AppException e) {
            logger.error("Error fetching min book cost: {}", e.getMessage());
            return "Ошибка при получении минимальной стоимости: " + e.getMessage();
        }
    }

    @Tool("Получить максимальный рейтинг книги в каталоге. Возвращает книги с наивысшим рейтингом. Используй для запросов вроде 'какой самый высокий рейтинг у книг?' или 'какие книги самые популярные?'.")
    public String getMaxBookRating() {
        try {
            Integer maxRating = bookService.getMaxBookRating();
            FilterBooksDTO filter = new FilterBooksDTO();
            filter.setMinRating(maxRating);
            filter.setMaxRating(maxRating);
            List<BookDTO> topBooks = bookService.filterBooks(filter);

            if (topBooks.isEmpty()) {
                return String.format("Книги с рейтингом %.2f отсутствуют.", maxRating);
            }

            StringBuilder response = new StringBuilder(String.format("**Книги с наивысшим рейтингом (%.2f)**:\n", maxRating));
            for (BookDTO book : topBooks) {
                Set<String> genres = bookService.getBookGenres(book.getId()).stream()
                        .map(GenreDTO::getName)
                        .collect(Collectors.toSet());
                String genresString = genres.isEmpty() ? "не указаны" : String.join(", ", genres);

                response.append("- **").append(book.getTitle())
                        .append("** (*").append(book.getAuthor())
                        .append("*, ").append(genresString)
                        .append(", ").append(book.getCost()).append(" BYN")
                        .append(")\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching max book rating: {}", e.getMessage());
            return "Ошибка при получении максимального рейтинга: " + e.getMessage();
        }
    }

    @Tool("Получить минимальный рейтинг книги в каталоге. Используй для запросов вроде 'какой самый низкий рейтинг у книг?'.")
    public String getMinBookRating() {
        try {
            Integer minRating = bookService.getMinBookRating();
            return String.format("**Минимальный рейтинг книги в каталоге**: %.2f", minRating);
        } catch (AppException e) {
            logger.error("Error fetching min book rating: {}", e.getMessage());
            return "Ошибка при получении минимального рейтинга: " + e.getMessage();
        }
    }

    @Tool("Получить максимальный год издания книги в каталоге. Используй для запросов вроде 'какая самая новая книга?'.")
    public String getMaxBookPublicationYear() {
        try {
            Integer maxYear = bookService.getMaxBookPublicationYear();
            return String.format("**Максимальный год издания книги в каталоге**: %d", maxYear);
        } catch (AppException e) {
            logger.error("Error fetching max publication year: {}", e.getMessage());
            return "Ошибка при получении максимального года издания: " + e.getMessage();
        }
    }

    @Tool("Получить минимальный год издания книги в каталоге. Используй для запросов вроде 'какая самая старая книга?'.")
    public String getMinBookPublicationYear() {
        try {
            Integer minYear = bookService.getMinBookPublicationYear();
            return String.format("**Минимальный год издания книги в каталоге**: %d", minYear);
        } catch (AppException e) {
            logger.error("Error fetching min publication year: {}", e.getMessage());
            return "Ошибка при получении минимального года издания: " + e.getMessage();
        }
    }

    @Tool("Получить максимальное количество страниц в книге в каталоге. Используй для запросов вроде 'какая книга с наибольшим количеством страниц?'.")
    public String getMaxBookPages() {
        try {
            Integer maxPages = bookService.getMaxBookPages();
            return String.format("**Максимальное количество страниц в книге**: %d", maxPages);
        } catch (AppException e) {
            logger.error("Error fetching max book pages: {}", e.getMessage());
            return "Ошибка при получении максимального количества страниц: " + e.getMessage();
        }
    }

    @Tool("Получить минимальное количество страниц в книге в каталоге. Используй для запросов вроде 'какая книга с наименьшим количеством страниц?'.")
    public String getMinBookPages() {
        try {
            Integer minPages = bookService.getMaxBookPages();
            return String.format("**Минимальное количество страниц в книге**: %d", minPages);
        } catch (AppException e) {
            logger.error("Error fetching min book pages: {}", e.getMessage());
            return "Ошибка при получении минимального количества страниц: " + e.getMessage();
        }
    }

    @Tool("Получить отзывы о книге по её названию. Возвращает список отзывов с именем пользователя, текстом отзыва и рейтингом. Используй для запросов вроде 'покажи отзывы на книгу Властелин колец'.")
    public String getBookReviews(String bookTitle) {
        try {
            BookDTO book = bookService.getBookDTOByTitle(bookTitle);
            Set<ReviewDTO> reviews = bookService.getBookReviewsDTO(book.getId());
            if (reviews.isEmpty()) {
                return "Отзывы на книгу \"" + bookTitle + "\" отсутствуют.";
            }

            StringBuilder response = new StringBuilder("**Отзывы на книгу \"" + bookTitle + "\"**:\n");
            for (ReviewDTO review : reviews) {
                response.append("- **").append(review.getUserName())
                        .append("**: ").append(review.getText() != null ? review.getText() : "(без текста)")
                        .append(" (рейтинг: **").append(review.getRating()).append("**)\n");
            }
            return response.toString();
        } catch (AppException e) {
            logger.error("Error fetching reviews for book {}: {}", bookTitle, e.getMessage());
            return "Ошибка при получении отзывов: " + e.getMessage();
        }
    }
}