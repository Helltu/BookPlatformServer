package by.bsuir.bookplatform.repositories;

import by.bsuir.bookplatform.entities.Book;
import by.bsuir.bookplatform.entities.Review;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    static Specification<Book> hasTitle(String title) {
        return (book, cq, cb) -> {
            if (title == null || title.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            } else {
                return cb.like(cb.lower(book.get("title")), "%" + title.toLowerCase() + "%");
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

    static Specification<Book> hasAverageRatingGreaterThan(Integer minRating) {
        return (book, cq, cb) -> {
            if (minRating == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                Double minRatingDouble = minRating.doubleValue();
                Subquery<Double> subquery = cq.subquery(Double.class);
                var subRoot = subquery.from(Review.class);
                subquery.select(cb.avg(subRoot.get("rating")))
                        .where(cb.equal(subRoot.get("book"), book));
                return cb.greaterThanOrEqualTo(subquery, minRatingDouble);
            }
        };
    }

    static Specification<Book> hasAverageRatingLessThan(Integer maxRating) {
        return (book, cq, cb) -> {
            if (maxRating == null) {
                return cb.isTrue(cb.literal(true));
            } else {
                Double maxRatingDouble = maxRating.doubleValue();
                Subquery<Double> subquery = cq.subquery(Double.class);
                var subRoot = subquery.from(Review.class);
                subquery.select(cb.avg(subRoot.get("rating")))
                        .where(cb.equal(subRoot.get("book"), book));
                return cb.lessThanOrEqualTo(subquery, maxRatingDouble);
            }
        };
    }

    static Specification<Book> hasAuthors(List<String> authors) {
        return (book, cq, cb) -> {
            if (authors == null || authors.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            } else {
                return book.get("author").in(authors);
            }
        };
    }

    static Specification<Book> hasPublishers(List<String> publishers) {
        return (book, cq, cb) -> {
            if (publishers == null || publishers.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            } else {
                return book.get("publisher").in(publishers);
            }
        };
    }

    static Specification<Book> hasGenres(List<String> genres) {
        return (book, cq, cb) -> {
            if (genres == null || genres.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            } else {
                var genreJoin = book.join("genres");
                cq.groupBy(book.get("id"));
                cq.having(cb.equal(cb.count(genreJoin.get("name")), genres.size()));
                return genreJoin.get("name").in(genres);
            }
        };
    }

    static Specification<Book> orderByAverageRating(Sort.Direction direction) {
        return (root, query, cb) -> {
            Subquery<Double> subquery = query.subquery(Double.class);
            var reviewRoot = subquery.from(Review.class);
            subquery.select(cb.avg(reviewRoot.get("rating")))
                    .where(cb.equal(reviewRoot.get("book"), root));

            if (direction == Sort.Direction.DESC) {
                query.orderBy(cb.desc(subquery));
            } else {
                query.orderBy(cb.asc(subquery));
            }
            return cb.isTrue(cb.literal(true));
        };
    }

    static Specification<Book> orderByPopularity(Sort.Direction direction) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var reviewRoot = subquery.from(Review.class);
            subquery.select(cb.count(reviewRoot))
                    .where(cb.equal(reviewRoot.get("book"), root));

            if (direction == Sort.Direction.DESC) {
                query.orderBy(cb.desc(subquery));
            } else {
                query.orderBy(cb.asc(subquery));
            }
            return cb.isTrue(cb.literal(true));
        };
    }

    @Query("SELECT DISTINCT b.author FROM Book b")
    Set<String> findAllDistinctAuthors();

    @Query("SELECT DISTINCT b.publisher FROM Book b")
    Set<String> findAllDistinctPublishers();

    @Query("SELECT MAX(b.cost) FROM Book b")
    Float findMaxBookCost();

    @Query("SELECT MIN(b.cost) FROM Book b")
    Float findMinBookCost();

    @Query("SELECT MAX(b.publicationYear) FROM Book b")
    Integer findMaxPublicationYear();

    @Query("SELECT MIN(b.publicationYear) FROM Book b")
    Integer findMinPublicationYear();

    @Query("SELECT MAX(b.pages) FROM Book b")
    Integer findMaxPages();

    @Query("SELECT MIN(b.pages) FROM Book b")
    Integer findMinPages();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Optional<Float> findAverageRatingById(@Param("bookId") Long bookId);

    @Query(value = "SELECT MIN(avg_rating) FROM (SELECT AVG(r.rating) as avg_rating FROM review r GROUP BY r.book_id) as sub", nativeQuery = true)
    Float findMinAverageRating();

    @Query(value = "SELECT MAX(avg_rating) FROM (SELECT AVG(r.rating) as avg_rating FROM review r GROUP BY r.book_id) as sub", nativeQuery = true)
    Float findMaxAverageRating();

    boolean existsByTitleIgnoreCaseAndAuthorIgnoreCaseAndPublisherIgnoreCaseAndPublicationYear(
            String title, String author, String publisher, Integer publicationYear);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b " +
            "WHERE LOWER(b.title) = LOWER(:title) AND LOWER(b.author) = LOWER(:author) " +
            "AND LOWER(b.publisher) = LOWER(:publisher) AND b.publicationYear = :publicationYear " +
            "AND b.id != :currentBookId")
    boolean existsByUniqueFieldsExcludingId(@Param("title") String title,
                                            @Param("author") String author,
                                            @Param("publisher") String publisher,
                                            @Param("publicationYear") Integer publicationYear,
                                            @Param("currentBookId") Long currentBookId);
}