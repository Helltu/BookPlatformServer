package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.MediaDTO;
import by.bsuir.bookplatform.entities.Media;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repositories.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaServiceTest {

    @InjectMocks
    private MediaService mediaService;

    @Mock
    private MediaRepository mediaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllMediaDTO() {
        Media media1 = new Media();
        media1.setId(1L);
        byte[] bytes1 = new byte[] { 1, 2, 3 };
        media1.setMedia(bytes1);
        Media media2 = new Media();
        media2.setId(2L);
        byte[] bytes2 = new byte[] { 4, 5, 6 };
        media2.setMedia(bytes2);
        when(mediaRepository.findAll()).thenReturn(Arrays.asList(media1, media2));
        List<MediaDTO> result = mediaService.getAllMediaDTO();
        assertEquals(2, result.size());
        assertArrayEquals(bytes1, result.get(0).getMedia());
        assertArrayEquals(bytes2, result.get(1).getMedia());
    }

    @Test
    void testGetMediaById_Found() {
        Media media = new Media();
        media.setId(1L);
        when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));
        Media result = mediaService.getMediaById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetMediaById_NotFound() {
        when(mediaRepository.findById(1L)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> mediaService.getMediaById(1L));
        assertEquals("Медиа с Id 1 не найдено.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testCreateMedia_Success() {
        MediaDTO mediaDTO = new MediaDTO();
        mediaDTO.setBookId(1L);
        byte[] bytes1 = new byte[] { 1, 2, 3 };
        mediaDTO.setMedia(bytes1);
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setId(1L);
            return media;
        });
        MediaDTO result = mediaService.createMedia(mediaDTO);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertArrayEquals(bytes1, result.getMedia());
    }

    @Test
    void testDeleteMediaById_Success() {
        Long mediaId = 1L;
        when(mediaRepository.existsById(mediaId)).thenReturn(true);
        mediaService.deleteMediaById(mediaId);
        verify(mediaRepository, times(1)).deleteById(mediaId);
    }

    @Test
    void testDeleteMediaById_NotFound() {
        Long mediaId = 1L;
        when(mediaRepository.existsById(mediaId)).thenReturn(false);
        AppException exception = assertThrows(AppException.class, () -> mediaService.deleteMediaById(mediaId));
        assertEquals("Media not found.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
