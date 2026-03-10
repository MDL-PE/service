package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateMovieRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Genre is required")
    String genre,

    @Positive(message = "Release year must be positive")
    int releaseYear
) {}