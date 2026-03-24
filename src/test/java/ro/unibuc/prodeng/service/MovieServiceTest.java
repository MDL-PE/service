package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.MovieEntity;
import ro.unibuc.prodeng.repository.MovieRepository;
import ro.unibuc.prodeng.request.MovieRequest;
import ro.unibuc.prodeng.request.AddRatingRequest;
import ro.unibuc.prodeng.response.MovieResponse;
import ro.unibuc.prodeng.service.MovieService;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class MovieServiceTest {

        @Mock
        private MovieRepository movieRepository;

        @InjectMocks
        private MovieService movieService;

        // GET BY ID
        @Test
        void testGetMovieById_existingMovie_returnsMovie() {
                // Arrange
                MovieEntity movie = new MovieEntity(
                                "1", "Inception", "Sci-Fi", 2010,
                                8.5, 2, new HashMap<>());

                when(movieRepository.findById("1"))
                                .thenReturn(Optional.of(movie));

                // Act
                MovieResponse response = movieService.getMovieById("1");

                // Assert
                assertNotNull(response);
                assertEquals("Inception", response.title());
                assertEquals(2010, response.releaseYear());

                verify(movieRepository).findById("1");
        }

        // GET BY ID - NOT FOUND
        @Test
        void testGetMovieById_notFound_throwsException() {
                when(movieRepository.findById("1"))
                                .thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> movieService.getMovieById("1"));
        }

        // CREATE MOVIE
        @Test
        void testCreateMovie_validData_createsMovie() {
                MovieRequest request = new MovieRequest("The Matrix", "Sci-Fi", 1999);

                when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear("The Matrix", 1999))
                                .thenReturn(false);

                when(movieRepository.save(any(MovieEntity.class)))
                                .thenAnswer(invocation -> {
                                        MovieEntity m = invocation.getArgument(0);
                                        return new MovieEntity(
                                                        "generated-id",
                                                        m.title(),
                                                        m.genre(),
                                                        m.releaseYear(),
                                                        0.0,
                                                        0,
                                                        new HashMap<>());
                                });

                MovieResponse response = movieService.createMovie(request);

                assertNotNull(response.id());
                assertEquals("The Matrix", response.title());
                verify(movieRepository).save(any());
        }

        // DUPLICATE
        @Test
        void testCreateMovie_duplicate_throwsException() {
                MovieRequest request = new MovieRequest("The Matrix", "Sci-Fi", 1999);

                when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear("The Matrix", 1999))
                                .thenReturn(true);

                assertThrows(IllegalArgumentException.class,
                                () -> movieService.createMovie(request));
        }

        // GET ALL MOVIES
        @Test
        void testGetAllMovies_returnsList() {
                List<MovieEntity> list = List.of(
                                new MovieEntity("1", "The Matrix", "Sci-Fi", 1999, 0, 0, new HashMap<>()));

                when(movieRepository.findAll()).thenReturn(list);

                List<MovieResponse> result = movieService.getAllMovies();

                assertEquals(1, result.size());
        }

        // INVALID YEAR (IN THE FUTURE)
        @Test
        void testCreateMovie_futureYear_throwsException() {
                int futureYear = java.time.Year.now().getValue() + 1;

                MovieRequest request = new MovieRequest("Future Movie", "Sci-Fi", futureYear);

                assertThrows(IllegalArgumentException.class,
                                () -> movieService.createMovie(request));
        }

        // INVALID YEAR (TOO OLD)
        @Test
        void testCreateMovie_yearTooOld_throwsException() {
                MovieRequest request = new MovieRequest("Old Movie", "Drama", 1800);

                assertThrows(IllegalArgumentException.class,
                                () -> movieService.createMovie(request));
        }

        // ADD RATING
        @Test
        void testAddRating_validRating_updatesMovie() {
                Map<String, Integer> ratings = new HashMap<>();
                MovieEntity movie = new MovieEntity(
                                "1", "Test", "Drama", 2020,
                                8.0, 1, ratings);

                when(movieRepository.findById("1"))
                                .thenReturn(Optional.of(movie));

                when(movieRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                AddRatingRequest request = new AddRatingRequest(10);

                MovieResponse response = movieService.addRating("1", "user1", request);

                assertEquals(2, response.ratingCount());
                assertTrue(response.averageRating() > 8.0);

                verify(movieRepository).save(any());
        }

        // ADD RATING MOVIE NOT FOUND
        @Test
        void testAddRating_movieNotFound_throwsException() {
                when(movieRepository.findById("1")).thenReturn(Optional.empty());

                AddRatingRequest request = new AddRatingRequest(8);

                assertThrows(EntityNotFoundException.class,
                                () -> movieService.addRating("1", "user1", request));
        }

        // USER ALREADY RATED
        @Test
        void testAddRating_userAlreadyRated_throwsException() {
                Map<String, Integer> ratings = new HashMap<>();
                ratings.put("user1", 8);

                MovieEntity movie = new MovieEntity(
                                "1", "Test", "Drama", 2020,
                                8.0, 1, ratings);

                when(movieRepository.findById("1"))
                                .thenReturn(Optional.of(movie));

                AddRatingRequest request = new AddRatingRequest(9);

                assertThrows(IllegalArgumentException.class,
                                () -> movieService.addRating("1", "user1", request));
        }

        // DELETE MOVIE
        @Test
        void testDeleteMovie_existingMovie_deletesSuccessfully() {
                when(movieRepository.existsById("1")).thenReturn(true);
                doNothing().when(movieRepository).deleteById("1");

                movieService.deleteMovie("1");

                verify(movieRepository).existsById("1");
                verify(movieRepository).deleteById("1");
        }

        // DELETE NOT FOUND
        @Test
        void testDeleteMovie_notFound_throwsException() {
                when(movieRepository.existsById("1")).thenReturn(false);

                assertThrows(EntityNotFoundException.class,
                                () -> movieService.deleteMovie("1"));
        }

        // UPDATE MOVIE
        @Test
        void testUpdateMovie_validData_updatesMovie() {
                MovieEntity existing = new MovieEntity(
                                "1", "Old", "Drama", 2000,
                                7.0, 2, new HashMap<>());

                MovieRequest request = new MovieRequest("New Title", "Action", 2005);

                when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear("New Title", 2005))
                                .thenReturn(false);

                when(movieRepository.findById("1"))
                                .thenReturn(Optional.of(existing));

                when(movieRepository.save(any()))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                MovieResponse response = movieService.updateMovie("1", request);

                assertEquals("New Title", response.title());
                assertEquals(2005, response.releaseYear());

                verify(movieRepository).save(any());
        }

        // UPDATE NOT FOUND
        @Test
        void testUpdateMovie_notFound_throwsException() {
                MovieRequest request = new MovieRequest("New", "Action", 2005);

                when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear(any(), anyInt()))
                                .thenReturn(false);

                when(movieRepository.findById("1"))
                                .thenReturn(Optional.empty());

                assertThrows(EntityNotFoundException.class,
                                () -> movieService.updateMovie("1", request));
        }

        // SEARCH BY TITLE
        @Test
        void testSearchByTitle_returnsResults() {
                List<MovieEntity> list = List.of(
                                new MovieEntity("1", "The Matrix", "Sci-Fi", 1999, 0, 0, new HashMap<>()));

                when(movieRepository.findByTitleContainingIgnoreCase("mat"))
                                .thenReturn(list);

                List<MovieResponse> result = movieService.searchByTitle("mat");

                assertEquals(1, result.size());
                assertEquals("The Matrix", result.get(0).title());
        }

        // SEARCH BY GENRE
        @Test
        void testGetMoviesByGenre_returnsResults() {
                List<MovieEntity> list = List.of(
                                new MovieEntity("1", "The Matrix", "Sci-Fi", 1999, 0, 0, new HashMap<>()));

                when(movieRepository.findByGenre("Sci-Fi")).thenReturn(list);

                List<MovieResponse> result = movieService.getMoviesByGenre("Sci-Fi");

                assertEquals(1, result.size());
        }

        // SEARCH BY YEAR
        @Test
        void testGetMoviesByYear_returnsResults() {
                List<MovieEntity> list = List.of(
                                new MovieEntity("1", "The Matrix", "Sci-Fi", 1999, 0, 0, new HashMap<>()));

                when(movieRepository.findByReleaseYear(1999)).thenReturn(list);

                List<MovieResponse> result = movieService.getMoviesByYear(1999);

                assertEquals(1, result.size());
        }
}