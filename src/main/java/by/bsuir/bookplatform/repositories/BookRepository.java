package by.bsuir.bookplatform.repository;

import by.bsuir.bookplatform.entities.Book;
import jakarta.transaction.Transaction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    static Specification<Book> hasTitle(String title) {
        return (book, cq, cb) -> {
            if (title == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.equal(book.get("title"), title);
            }
        };
    }

    static Specification<Book> hasAuthor(String author) {
        return (book, cq, cb) -> {
            if (author == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.equal(book.get("author"), author);
            }
        };
    }

    static Specification<Book> hasPublisher(String publisher) {
        return (book, cq, cb) -> {
            if (publisher == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.equal(book.get("author"), publisher);
            }
        };
    }

    static Specification<Book> hasHardcover(Boolean hardcover) {
        return (book, cq, cb) -> {
            if (hardcover == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.equal(book.get("hardcover"), hardcover);
            }
        };
    }

    static Specification<Book> hasMorePagesThan(Integer minPages) {
        return (book, cq, cb) -> {
            if (minPages == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.greaterThanOrEqualTo(book.get("pages"), minPages);
            }
        };
    }

    static Specification<Book> hasLessPagesThan(Integer maxPages) {
        return (book, cq, cb) -> {
            if (maxPages == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.lessThanOrEqualTo(book.get("pages"), maxPages);
            }
        };
    }

    static Specification<Book> costsMoreThan(Float minCost) {
        return (book, cq, cb) -> {
            if (minCost == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.greaterThanOrEqualTo(book.get("cost"), minCost);
            }
        };
    }

    static Specification<Book> costsLessThan(Float maxCost) {
        return (book, cq, cb) -> {
            if (maxCost == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.lessThanOrEqualTo(book.get("cost"), maxCost);
            }
        };
    }

    static Specification<Book> hasPublicationYearLaterThan(Integer minPublicationYear) {
        return (book, cq, cb) -> {
            if (minPublicationYear == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.greaterThanOrEqualTo(book.get("cost"), minPublicationYear);
            }
        };
    }

    static Specification<Book> hasPublicationYearBeforeThan(Integer maxPublicationYear) {
        return (book, cq, cb) -> {
            if (maxPublicationYear == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.lessThanOrEqualTo(book.get("cost"), maxPublicationYear);
            }
        };
    }

    //ДОБАВИТЬ ФИЛЬТРАИЦИЮ ПО РЕЙТИНГУ
}
