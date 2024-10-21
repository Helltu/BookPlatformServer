package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.entities.Genre;
import by.bsuir.bookplatform.entities.Media;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MediaService {
    private final MediaRepository mediaRepository;

    public List<Media> findAllMedia() {
        return mediaRepository.findAll();
    }

    public Optional<Media> findMediaById(Long id) {
        return mediaRepository.findById(id);
    }

    public Media saveMedia(Media media) {
        Optional<Media> existingGenre = mediaRepository.findAll().stream()
                .filter(g -> Arrays.equals(g.getMedia(), media.getMedia()))
                .findFirst();

        if (existingGenre.isPresent()) {
            throw new AppException("Media with this image already exists.", HttpStatus.CONFLICT);
        }
        return mediaRepository.save(media);
    }

    public void deleteMediaById(Long id) {
        if (!mediaRepository.existsById(id)) {
            throw new AppException("Media not found.", HttpStatus.NOT_FOUND);
        }
        mediaRepository.deleteById(id);
    }
}
