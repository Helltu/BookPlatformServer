package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.Genre;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> findAllGenres() {
        return genreRepository.findAll();
    }

    public Optional<Genre> findGenreById(Long id) {
        return genreRepository.findById(id);
    }

    public Genre saveGenre(Genre genre) {
        Optional<Genre> existingGenre = genreRepository.findAll().stream()
                .filter(g -> g.getName().equalsIgnoreCase(genre.getName()))
                .findFirst();

        if (existingGenre.isPresent()) {
            throw new AppException("A genre with this name already exists.", HttpStatus.CONFLICT);
        }
        return genreRepository.save(genre);
    }

    public void deleteGenreById(Long id) {
        if (!genreRepository.existsById(id)) {
            throw new AppException("Genre not found.", HttpStatus.NOT_FOUND);
        }
        genreRepository.deleteById(id);
    }
}