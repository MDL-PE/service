package ro.unibuc.prodeng.controller;

import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.model.Watchlist;
import ro.unibuc.prodeng.service.WatchlistService;

@RestController
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService service;

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    @PostMapping("/{userId}/movies/{movieId}")
    public void addMovie(@PathVariable String userId, @PathVariable String movieId) {
        service.addMovie(userId, movieId);
    }

    @DeleteMapping("/{userId}/movies/{movieId}")
    public void removeMovie(@PathVariable String userId, @PathVariable String movieId) {
        service.removeMovie(userId, movieId);
    }

    @PatchMapping("/{userId}/movies/{movieId}/watched")
    public void markWatched(@PathVariable String userId, @PathVariable String movieId) {
        service.markWatched(userId, movieId);
    }

    @GetMapping("/{userId}")
    public Watchlist getWatchlist(@PathVariable String userId) {
        return service.getWatchlist(userId);
    }
}