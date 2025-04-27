//package by.bsuir.bookplatform.services;
//
//import by.bsuir.bookplatform.DTO.BookDTO;
//import by.bsuir.bookplatform.DTO.FilterBooksDTO;
//import by.bsuir.bookplatform.DTO.GenreDTO;
//import by.bsuir.bookplatform.DTO.ReviewDTO;
//import by.bsuir.bookplatform.exceptions.AppException;
//import dev.langchain4j.agent.tool.Tool;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor(onConstructor = @__(@Autowired))
//public class BooksTool {
//    private final BookService bookService;
//    private final GenreService genreService;
//
//    @Tool("Получить список всех доступных книг в магазине. Возвращает список книг с названием, автором, жанрами, ценой, типом обложки и рейтингом. Используй для запросов вроде 'покажи все книги' или 'какие книги есть в магазине?'.")
//    public String getAllBooks() {
//        try {
//            List<BookDTO> books = bookService.getAllBooksDTO();
//            if (books.isEmpty()) {
//                return "В магазине пока нет доступных книг.";
//            }
//
//            StringBuilder response = new StringBuilder("Доступные книги в магазине:\n");
//            for (BookDTO book : books) {
//                Set<String> genres = bookService.getBookGenres(book.getId()).stream()
//                        .map(GenreDTO::getName)
//                        .collect(Collectors.toSet());
//                String genresString = genres.isEmpty() ? "не указаны" : String.join(", ", genres);
//
//                response.append("- ").append(book.getTitle())
//                        .append(" (").append(book.getAuthor())
//                        .append(", ").append(genresString)
//                        .append(", $").append(book.getCost())
//                        .append(", ").append(book.getHardcover() ? "твёрдая обложка" : "мягкая обложка")
//                        .append(", рейтинг: ").append(bookService.getBookAverageRatingById(book.getId()))
//                        .append(")\n");
//            }
//            return response.toString();
//        } catch (AppException e) {
//            return "Ошибка при получении списка книг: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить подробную информацию о книге по её названию. Возвращает название, автора, жанры, издателя, год издания, описание, стоимость, тип обложки, количество страниц, количество в наличии и рейтинг. Используй для запросов вроде 'расскажи о книге Властелин колец'.")
//    public String getBookDetails(String bookTitle) {
//        try {
//            BookDTO book = bookService.getBookDTOByTitle(bookTitle);
//            StringBuilder response = new StringBuilder("Информация о книге:\n");
//            response.append("- Название: ").append(book.getTitle()).append("\n")
//                    .append("- Автор: ").append(book.getAuthor()).append("\n")
//                    .append("- Издатель: ").append(book.getPublisher()).append("\n")
//                    .append("- Год издания: ").append(book.getPublicationYear()).append("\n")
//                    .append("- Описание: ").append(book.getDescription()).append("\n")
//                    .append("- Стоимость: $").append(book.getCost()).append("\n")
//                    .append("- Тип обложки: ").append(book.getHardcover() ? "твёрдая" : "мягкая").append("\n")
//                    .append("- Количество страниц: ").append(book.getPages()).append("\n")
//                    .append("- Количество в наличии: ").append(book.getAmt()).append("\n")
//                    .append("- Средний рейтинг: ").append(bookService.getBookAverageRatingById(book.getId())).append("\n");
//
//            Set<String> genres = bookService.getBookGenres(book.getId()).stream()
//                    .map(GenreDTO::getName)
//                    .collect(Collectors.toSet());
//            response.append("- Жанры: ").append(genres.isEmpty() ? "не указаны" : String.join(", ", genres)).append("\n");
//
//            return response.toString();
//        } catch (AppException e) {
//            return "К сожалению, книга с названием \"" + bookTitle + "\" не найдена. Пожалуйста, проверьте название.";
//        }
//    }
//
//    @Tool("Рекомендовать книги на основе жанра, автора, минимального и максимального рейтинга, года издания, минимальной и максимальной цены, количества страниц, типа обложки. Параметры могут быть null, если не указаны. Используй для запросов вроде 'порекомендуй книги жанра Фэнтези с рейтингом выше 4' или 'найди книги в твёрдой обложке до $20'. Сортируй по рейтингу (убывание).")
//    public String recommendBooks(String genre, String author, Float minRating, Float maxRating, Integer minYear, Integer maxYear, Float minCost, Float maxCost, Integer minPages, Integer maxPages, Boolean hardcover) {
//        try {
//            FilterBooksDTO filter = new FilterBooksDTO();
//            filter.setGenres(genre != null ? Collections.singletonList(genre) : null);
//            filter.setAuthors(author != null ? Collections.singletonList(author) : null);
//            filter.setMinRating(Math.round(minRating));
//            filter.setMaxRating(Math.round(maxRating));
//            filter.setMinPublicationYear(minYear);
//            filter.setMaxPublicationYear(maxYear);
//            filter.setMinCost(minCost);
//            filter.setMaxCost(maxCost);
//            filter.setMinPages(minPages);
//            filter.setMaxPages(maxPages);
//            filter.setHardcover(hardcover);
//            filter.setSortBy("rating");
//            filter.setSortDirection("desc");
//
//            List<BookDTO> books = bookService.filterBooks(filter);
//            if (books.isEmpty()) {
//                return "К сожалению, книги, соответствующие вашим предпочтениям, не найдены. Пожалуйста, уточните жанр, автора или другие критерии.";
//            }
//
//            StringBuilder response = new StringBuilder("Я нашёл для вас несколько замечательных книг:\n");
//            for (BookDTO book : books) {
//                Set<String> genres = bookService.getBookGenres(book.getId()).stream()
//                        .map(GenreDTO::getName)
//                        .collect(Collectors.toSet());
//                String genresString = genres.isEmpty() ? "не указаны" : String.join(", ", genres);
//
//                response.append("- ").append(book.getTitle())
//                        .append(" (").append(book.getAuthor())
//                        .append(", ").append(genresString)
//                        .append(", $").append(book.getCost())
//                        .append(", ").append(book.getHardcover() ? "твёрдая обложка" : "мягкая обложка")
//                        .append(", рейтинг: ").append(bookService.getBookAverageRatingById(book.getId()))
//                        .append(")\n");
//            }
//            return response.toString();
//        } catch (AppException e) {
//            return "Ошибка при поиске книг: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить список всех жанров в магазине. Возвращает названия жанров. Используй для запросов вроде 'какие жанры есть в магазине?' или 'покажи все жанры'.")
//    public String getAllGenres() {
//        try {
//            List<GenreDTO> genres = genreService.getAllGenresDTO();
//            if (genres.isEmpty()) {
//                return "В магазине пока нет жанров.";
//            }
//
//            StringBuilder response = new StringBuilder("Доступные жанры в магазине:\n");
//            for (GenreDTO genre : genres) {
//                response.append("- ").append(genre.getName()).append("\n");
//            }
//            return response.toString();
//        } catch (AppException e) {
//            return "Ошибка при получении списка жанров: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить информацию о жанре по его названию. Возвращает название жанра и описание. Используй для запросов вроде 'расскажи о жанре Фэнтези'.")
//    public String getGenreByName(String genreName) {
//        try {
//            GenreDTO genre = genreService.getGenreDTOByName(genreName);
//            return "Информация о жанре:\n- Название: " + genre.getName();
//        } catch (AppException e) {
//            return "К сожалению, жанр с названием \"" + genreName + "\" не найден.";
//        }
//    }
//
//    @Tool("Получить список всех авторов книг в магазине. Возвращает уникальные имена авторов. Используй для запросов вроде 'кто авторы книг в магазине?'.")
//    public String getAllAuthors() {
//        try {
//            Set<String> authors = bookService.getAllAuthors();
//            if (authors.isEmpty()) {
//                return "Авторы в магазине пока отсутствуют.";
//            }
//
//            StringBuilder response = new StringBuilder("Авторы книг в магазине:\n");
//            for (String author : authors) {
//                response.append("- ").append(author).append("\n");
//            }
//            return response.toString();
//        } catch (AppException e) {
//            return "Ошибка при получении списка авторов: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить список всех издателей книг в магазине. Возвращает уникальные названия издателей. Используй для запросов вроде 'какие издатели есть в магазине?'.")
//    public String getAllPublishers() {
//        try {
//            Set<String> publishers = bookService.getAllPublishers();
//            if (publishers.isEmpty()) {
//                return "Издатели в магазине пока отсутствуют.";
//            }
//
//            StringBuilder response = new StringBuilder("Издатели книг в магазине:\n");
//            for (String publisher : publishers) {
//                response.append("- ").append(publisher).append("\n");
//            }
//            return response.toString();
//        } catch (AppException e) {
//            return "Ошибка при получении списка издателей: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить максимальную стоимость книги в каталоге. Используй для запросов вроде 'какая самая дорогая книга?'.")
//    public String getMaxBookCost() {
//        try {
//            Float maxCost = bookService.getMaxBookCost();
//            return "Максимальная стоимость книги в каталоге: $" + maxCost;
//        } catch (AppException e) {
//            return "Ошибка при получении максимальной стоимости: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить минимальную стоимость книги в каталоге. Используй для запросов вроде 'какая самая дешёвая книга?'.")
//    public String getMinBookCost() {
//        try {
//            Float minCost = bookService.getMinBookCost();
//            return "Минимальная стоимость книги в каталоге: $" + minCost;
//        } catch (AppException e) {
//            return "Ошибка при получении минимальной стоимости: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить максимальный рейтинг книги в каталоге. Используй для запросов вроде 'какой самый высокий рейтинг у книг?'.")
//    public String getMaxBookRating() {
//        try {
//            Integer maxRating = bookService.getMaxBookRating();
//            return "Максимальный рейтинг книги в каталоге: " + maxRating;
//        } catch (AppException e) {
//            return "Ошибка при получении максимального рейтинга: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить минимальный рейтинг книги в каталоге. Используй для запросов вроде 'какой самый низкий рейтинг у книг?'.")
//    public String getMinBookRating() {
//        try {
//            Integer minRating = bookService.getMinBookRating();
//            return "Минимальный рейтинг книги в каталоге: " + minRating;
//        } catch (AppException e) {
//            return "Ошибка при получении минимального рейтинга: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить максимальный год издания книги в каталоге. Используй для запросов вроде 'какая самая новая книга?'.")
//    public String getMaxBookPublicationYear() {
//        try {
//            Integer maxYear = bookService.getMaxBookPublicationYear();
//            return "Максимальный год издания книги в каталоге: " + maxYear;
//        } catch (AppException e) {
//            return "Ошибка при получении максимального года издания: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить минимальный год издания книги в каталоге. Используй для запросов вроде 'какая самая старая книга?'.")
//    public String getMinBookPublicationYear() {
//        try {
//            Integer minYear = bookService.getMinBookPublicationYear();
//            return "Минимальный год издания книги в каталоге: " + minYear;
//        } catch (AppException e) {
//            return "Ошибка при получении минимального года издания: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить максимальное количество страниц в книге в каталоге. Используй для запросов вроде 'какая книга с наибольшим количеством страниц?'.")
//    public String getMaxBookPages() {
//        try {
//            Integer maxPages = bookService.getMaxBookPages();
//            return "Максимальное количество страниц в книге: " + maxPages;
//        } catch (AppException e) {
//            return "Ошибка при получении максимального количества страниц: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить минимальное количество страниц в книге в каталоге. Используй для запросов вроде 'какая книга с наименьшим количеством страниц?'.")
//    public String getMinBookPages() {
//        try {
//            Integer minPages = bookService.getMinBookPages();
//            return "Минимальное количество страниц в книге: " + minPages;
//        } catch (AppException e) {
//            return "Ошибка при получении минимального количества страниц: " + e.getMessage();
//        }
//    }
//
//    @Tool("Получить отзывы о книге по её названию. Возвращает список отзывов с именем пользователя, текстом отзыва и рейтингом. Используй для запросов вроде 'покажи отзывы на книгу Властелин колец'.")
//    public String getBookReviews(String bookTitle) {
//        try {
//            BookDTO book = bookService.getBookDTOByTitle(bookTitle);
//            Set<ReviewDTO> reviews = bookService.getBookReviewsDTO(book.getId());
//            if (reviews.isEmpty()) {
//                return "Отзывы на книгу \"" + bookTitle + "\" отсутствуют.";
//            }
//
//            StringBuilder response = new StringBuilder("Отзывы на книгу \"" + bookTitle + "\":\n");
//            for (ReviewDTO review : reviews) {
//                response.append("- ").append(review.getUserName())
//                        .append(": ").append(review.getText() != null ? review.getText() : "(без текста)")
//                        .append(" (рейтинг: ").append(review.getRating()).append(")\n");
//            }
//            return response.toString();
//        } catch (AppException e) {
//            return "Ошибка при получении отзывов: " + e.getMessage();
//        }
//    }
//}