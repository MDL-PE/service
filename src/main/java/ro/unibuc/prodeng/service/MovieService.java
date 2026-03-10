package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.MovieEntity;
import ro.unibuc.prodeng.repository.MovieRepository;
import ro.unibuc.prodeng.request.CreateMovieRequest;
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

    // Returne movie by ID
    public MovieResponse getMovieById(String id) {
        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found with id: " + id));
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
    public MovieResponse createMovie(CreateMovieRequest request) {
        MovieEntity movie = new MovieEntity(request.title(), request.genre(), request.releaseYear());
        MovieEntity saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    // Delete movie with ID
    public void deleteMovie(String id) {
        if (!movieRepository.existsById(id)) {
            throw new EntityNotFoundException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    // Add rating
    public MovieResponse addRating(String movieId, AddRatingRequest request) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found with id: " + movieId));

        double totalRating = movie.averageRating() * movie.ratingCount();
        int newCount = movie.ratingCount() + 1;
        double newAverage = (totalRating + request.rating()) / newCount;

        MovieEntity updated = new MovieEntity(
                movie.id(),
                movie.title(),
                movie.genre(),
                movie.releaseYear(),
                newAverage,
                newCount
        );

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
                movie.ratingCount()
        );
    }
}