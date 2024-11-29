package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.MediaDTO;
import by.bsuir.bookplatform.entities.Media;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.mapper.DTOMapper;
import by.bsuir.bookplatform.repositories.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;

    public List<MediaDTO> getAllMediaDTO() {
        return mediaRepository.findAll().stream()
                .map(MediaDTO::new)
                .toList();
    }

    public MediaDTO getMediaDTOById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new AppException("Медиа с Id " + id + " не найдено.", HttpStatus.NOT_FOUND));
        return new MediaDTO(media);
    }

    public Media getMediaById(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new AppException("Медиа с Id " + id + " не найдено.", HttpStatus.NOT_FOUND));
    }

    public MediaDTO createMedia(MediaDTO mediaDTO) {
        mediaDTO.checkValues();
        Media media = DTOMapper.getInstance().map(mediaDTO, Media.class);
        Media savedMedia = mediaRepository.save(media);
        return new MediaDTO(savedMedia);
    }

    public void deleteMediaById(Long id) {
        if (!mediaRepository.existsById(id)) {
            throw new AppException("Media not found.", HttpStatus.NOT_FOUND);
        }
        mediaRepository.deleteById(id);
    }
}
