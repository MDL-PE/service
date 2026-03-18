package ro.unibuc.prodeng.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movies")
public record MovieEntity(
    @Id
    String id,
    String title,
    String genre,
    int releaseYear,
    double averageRating,
    int ratingCount,
    Map<String, Integer> userRatings
) {
    public MovieEntity(String title, String genre, int releaseYear) {
        this(null, title, genre, releaseYear, 0.0, 0, new HashMap<>());
    }
}