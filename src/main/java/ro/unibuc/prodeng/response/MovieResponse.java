package ro.unibuc.prodeng.response;

public record MovieResponse(
    String id,
    String title,
    String genre,
    int releaseYear,
    double averageRating,
    int ratingCount
) {}