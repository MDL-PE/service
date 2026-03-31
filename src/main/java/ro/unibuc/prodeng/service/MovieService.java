package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.MovieEntity;
import ro.unibuc.prodeng.repository.MovieRepository;
import ro.unibuc.prodeng.request.MovieRequest;
import ro.unibuc.prodeng.request.AddRatingRequest;
import ro.unibuc.prodeng.response.MovieResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Return all movies
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Return movie by ID
    public MovieResponse getMovieById(String id) {
        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie with id " + id));
        return toResponse(movie);
    }

    // Search movies by title
    public List<MovieResponse> searchByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Search movies by genre
    public List<MovieResponse> getMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Search movies by year
    public List<MovieResponse> getMoviesByYear(int year) {
        return movieRepository.findByReleaseYear(year)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Create new movie
    public MovieResponse createMovie(MovieRequest request) {
        validateReleaseYear(request.releaseYear());
        validateDuplicate(request.title(), request.releaseYear());

        MovieEntity movie = new MovieEntity(request.title(), request.genre(), request.releaseYear());
        MovieEntity saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // Edit movie
    public MovieResponse updateMovie(String id, MovieRequest request) {
        validateReleaseYear(request.releaseYear());
        validateDuplicate(request.title(), request.releaseYear());

        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie with id " + id));

        MovieEntity updated = new MovieEntity(
                movie.id(),
                request.title(),
                request.genre(),
                request.releaseYear(),
                movie.averageRating(),
                movie.ratingCount(),
                movie.userRatings());

        movieRepository.save(updated);
        return toResponse(updated);
    }

    // Delete movie with ID
    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            throw new EntityNotFoundException("Movie with id " + id);
        }
        movieRepository.deleteById(id);
    }

    // Add rating
    public MovieResponse addRating(String movieId, String userId, AddRatingRequest request) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie with id " + movieId));

        if (movie.userRatings().containsKey(userId)) {
            throw new IllegalArgumentException("User already rated this movie");
        }

        double totalRating = movie.averageRating() * movie.ratingCount();
        int newCount = movie.ratingCount() + 1;
        double newAverage = (totalRating + request.rating()) / newCount;

        movie.userRatings().put(userId, request.rating());

        MovieEntity updated = new MovieEntity(
                movie.id(),
                movie.title(),
                movie.genre(),
                movie.releaseYear(),
                newAverage,
                newCount,
                movie.userRatings());

        movieRepository.save(updated);
        return toResponse(updated);
    }

    // Convert Entity → Response
    private MovieResponse toResponse(MovieEntity movie) {
        return new MovieResponse(
                movie.id(),
                movie.title(),
                movie.genre(),
                movie.releaseYear(),
                movie.averageRating(),
                movie.ratingCount());
    }

    private void validateReleaseYear(int year) {
        int currentYear = java.time.Year.now().getValue();
        if (year < 1888) {
            throw new IllegalArgumentException("Release year too old");
        }
        if (year > currentYear) {
            throw new IllegalArgumentException("Release year cannot be in the future");
        }
    }

    private void validateDuplicate(String title, int year) {
        if (movieRepository.existsByTitleIgnoreCaseAndReleaseYear(title, year)) {
            throw new IllegalArgumentException("Movie already exists");
        }
    }
}