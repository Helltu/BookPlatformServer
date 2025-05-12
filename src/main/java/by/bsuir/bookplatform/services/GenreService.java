package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.GenreDTO;
import by.bsuir.bookplatform.DTO.stats.GenreBookCountDTO;
import by.bsuir.bookplatform.DTO.stats.GenreRatingDTO;
import by.bsuir.bookplatform.entities.Genre;
import by.bsuir.bookplatform.entities.Review;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreDTO> getAllGenresDTO() {
        return genreRepository.findAll().stream()
                .map(GenreDTO::new)
                .toList();
    }

    public GenreDTO getGenreDTOById(Long id) {
        Genre genre = getGenreById(id);
        return new GenreDTO(genre);
    }

    public Genre getGenreById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new AppException("Жанр с ID " + id + " не найден.", HttpStatus.NOT_FOUND));
    }

    public GenreDTO getGenreDTOByName(String name) {
        Genre genre = genreRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new AppException("Жанр с именем \"" + name + "\" не найден.", HttpStatus.NOT_FOUND));
        return new GenreDTO(genre);
    }

    public GenreDTO createGenre(GenreDTO genreDTO) {
        genreDTO.checkValues();

        if (genreRepository.existsByNameIgnoreCase(genreDTO.getName())) {
            throw new AppException("Жанр с таким именем уже существует.", HttpStatus.CONFLICT);
        }

        Genre genre = DTOMapper.getInstance().map(genreDTO, Genre.class);
        genre = genreRepository.save(genre);

        return new GenreDTO(genre);
    }

    public GenreDTO editGenre(Long id, GenreDTO genreDetailsDTO) {
        Genre existingGenre = getGenreById(id);

        if (genreDetailsDTO.getName() != null &&
                genreRepository.existsByNameIgnoreCaseAndIdNot(genreDetailsDTO.getName(), id)) {
            throw new AppException("Жанр с таким именем уже существует.", HttpStatus.CONFLICT);
        }

        if (genreDetailsDTO.getName() != null) {
            existingGenre.setName(genreDetailsDTO.getName());
        }

        existingGenre = genreRepository.save(existingGenre);

        return new GenreDTO(existingGenre);
    }

    public void deleteGenreById(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new AppException("Жанр с ID " + id + " не найден.", HttpStatus.NOT_FOUND);
        }
        genreRepository.deleteById(id);
    }

    public List<GenreBookCountDTO> getBooksPerGenre() {
        return genreRepository.findAll().stream()
                .map(genre -> new GenreBookCountDTO(genre.getName(), genre.getBooks().size()))
                .sorted(Comparator.comparing(GenreBookCountDTO::getBookCount).reversed())
                .toList();
    }

    public List<GenreRatingDTO> getGenreAverageRatings() {
        return genreRepository.findAll().stream()
                .map(genre -> {
                    List<Review> reviews = genre.getBooks().stream()
                            .flatMap(book -> book.getReviews().stream())
                            .toList();
                    double avg = reviews.isEmpty() ? 0.0 :
                            reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
                    return new GenreRatingDTO(genre.getName(), avg);
                })
                .toList();
    }
}
