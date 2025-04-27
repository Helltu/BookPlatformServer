package by.bsuir.bookplatform.services;

import by.bsuir.bookplatform.DTO.GenreDTO;
import by.bsuir.bookplatform.entities.Genre;
import by.bsuir.bookplatform.exceptions.AppException;
import by.bsuir.bookplatform.repositories.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenreServiceTest {

    @InjectMocks
    private GenreService genreService;

    @Mock
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllGenresDTO() {
        Genre genre1 = new Genre();
        genre1.setId(1L);
        genre1.setName("Genre One");
        Genre genre2 = new Genre();
        genre2.setId(2L);
        genre2.setName("Genre Two");
        when(genreRepository.findAll()).thenReturn(Arrays.asList(genre1, genre2));
        List<GenreDTO> result = genreService.getAllGenresDTO();
        assertEquals(2, result.size());
        assertEquals("Genre One", result.get(0).getName());
        assertEquals("Genre Two", result.get(1).getName());
    }

    @Test
    void testGetGenreById_Found() {
        Genre genre = new Genre();
        genre.setId(1L);
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
        Genre result = genreService.getGenreById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetGenreById_NotFound() {
        when(genreRepository.findById(1L)).thenReturn(Optional.empty());
        AppException exception = assertThrows(AppException.class, () -> genreService.getGenreById(1L));
        assertEquals("Жанр с ID 1 не найден.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testCreateGenre_Success() {
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("New Genre");
        when(genreRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> {
            Genre genre = invocation.getArgument(0);
            genre.setId(1L);
            return genre;
        });
        GenreDTO result = genreService.createGenre(genreDTO);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Genre", result.getName());
    }

    @Test
    void testCreateGenre_GenreExists() {
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("Existing Genre");
        when(genreRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> genreService.createGenre(genreDTO));
        assertEquals("Жанр с таким именем уже существует.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testEditGenre_Success() {
        Long genreId = 1L;
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("Updated Genre");
        Genre existingGenre = new Genre();
        existingGenre.setId(genreId);
        existingGenre.setName("Old Genre");
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByNameIgnoreCaseAndIdNot(anyString(), eq(genreId))).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GenreDTO result = genreService.editGenre(genreId, genreDTO);
        assertNotNull(result);
        assertEquals("Updated Genre", result.getName());
    }

    @Test
    void testEditGenre_GenreExists() {
        Long genreId = 1L;
        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setName("Existing Genre");
        Genre existingGenre = new Genre();
        existingGenre.setId(genreId);
        existingGenre.setName("Old Genre");
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByNameIgnoreCaseAndIdNot(anyString(), eq(genreId))).thenReturn(true);
        AppException exception = assertThrows(AppException.class, () -> genreService.editGenre(genreId, genreDTO));
        assertEquals("Жанр с таким именем уже существует.", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void testDeleteGenreById_Success() {
        Long genreId = 1L;
        when(genreRepository.existsById(genreId)).thenReturn(true);
        genreService.deleteGenreById(genreId);
        verify(genreRepository, times(1)).deleteById(genreId);
    }

    @Test
    void testDeleteGenreById_NotFound() {
        Long genreId = 1L;
        when(genreRepository.existsById(genreId)).thenReturn(false);
        AppException exception = assertThrows(AppException.class, () -> genreService.deleteGenreById(genreId));
        assertEquals("Жанр с ID 1 не найден.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
