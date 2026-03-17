package ro.unibuc.prodeng.service;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.model.Watchlist;
import ro.unibuc.prodeng.model.WatchlistItem;
import ro.unibuc.prodeng.repository.WatchlistRepository;

@Service
public class WatchlistService {

    private final WatchlistRepository repository;

    public WatchlistService(WatchlistRepository repository) {
        this.repository = repository;
    }

    public void addMovie(String userId, String movieId) {
        Watchlist watchlist = repository.findByUserId(userId)
                .orElse(new Watchlist(userId, new ArrayList<>()));

        boolean exists = watchlist.getMovies().stream()
                .anyMatch(m -> m.getMovieId().equals(movieId));

        if (exists) {
            throw new RuntimeException("Movie already in watchlist");
        }

        WatchlistItem item = new WatchlistItem();
        item.setMovieId(movieId);
        item.setWatched(false);
        item.setAddedAt(LocalDateTime.now());

        watchlist.getMovies().add(item);
        repository.save(watchlist);
    }

    public void removeMovie(String userId, String movieId) {
        Watchlist watchlist = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));

        watchlist.getMovies().removeIf(m -> m.getMovieId().equals(movieId));
        repository.save(watchlist);
    }

    public void markWatched(String userId, String movieId) {
        Watchlist watchlist = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));

        watchlist.getMovies().forEach(m -> {
            if (m.getMovieId().equals(movieId)) {
                m.setWatched(true);
                m.setWatchedAt(LocalDateTime.now());
            }
        });

        repository.save(watchlist);
    }

    public Watchlist getWatchlist(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));
    }
}