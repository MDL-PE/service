package ro.unibuc.prodeng.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "watchlists")
public class Watchlist {

    @Id
    private String id;

    private String userId;

    private List<WatchlistItem> movies;

    public Watchlist() {}

    public Watchlist(String userId, List<WatchlistItem> movies) {
        this.userId = userId;
        this.movies = movies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<WatchlistItem> getMovies() {
        return movies;
    }

    public void setMovies(List<WatchlistItem> movies) {
        this.movies = movies;
    }
}