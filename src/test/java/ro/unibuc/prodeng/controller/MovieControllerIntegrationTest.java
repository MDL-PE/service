package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.model.MovieEntity;
import ro.unibuc.prodeng.repository.MovieRepository;
import ro.unibuc.prodeng.request.MovieRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("MovieController Integration Tests")
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        movieRepository.deleteAll();
    }

    private String createMovie(String title, String genre, int year) throws Exception {
        MovieRequest request = new MovieRequest(title, genre, year);

        String response = mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.genre").value(genre))
                .andExpect(jsonPath("$.releaseYear").value(year))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void testCreateAndGetMovie_validData_success() throws Exception {
        String movieId = createMovie("Inception", "Sci-Fi", 2010);

        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"))
                .andExpect(jsonPath("$.genre").value("Sci-Fi"))
                .andExpect(jsonPath("$.releaseYear").value(2010));
    }

    @Test
    void testGetAllMovies_returnsList() throws Exception {
        createMovie("Movie1", "Action", 2000);
        createMovie("Movie2", "Drama", 2001);

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateMovie_updatesSuccessfully() throws Exception {
        String movieId = createMovie("Old Title", "Action", 2000);

        MovieRequest updateRequest = new MovieRequest("New Title", "Drama", 2001);

        mockMvc.perform(put("/api/movies/" + movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.genre").value("Drama"))
                .andExpect(jsonPath("$.releaseYear").value(2001));
    }

    @Test
    void testDeleteMovie_deletesSuccessfully() throws Exception {
        String movieId = createMovie("To Delete", "Horror", 1999);

        mockMvc.perform(delete("/api/movies/" + movieId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/movies/" + movieId))
                .andExpect(status().isNotFound());

        Assertions.assertEquals(0, movieRepository.count());
    }

    @Test
    void testSearchByTitle_returnsMatchingMovies() throws Exception {
        createMovie("Matrix", "Action", 1999);
        createMovie("Matrix Reloaded", "Action", 2003);

        mockMvc.perform(get("/api/movies/search/title")
                        .param("title", "matrix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testAddRating_updatesAverageAndCount_correctly() throws Exception {
        String movieId = createMovie("Interstellar", "Sci-Fi", 2014);

        mockMvc.perform(post("/api/movies/" + movieId + "/rating")
                        .param("userId", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(8.0))
                .andExpect(jsonPath("$.ratingCount").value(1));

        mockMvc.perform(post("/api/movies/" + movieId + "/rating")
                        .param("userId", "user2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(9.0))
                .andExpect(jsonPath("$.ratingCount").value(2));

        MovieEntity movie = movieRepository.findById(movieId).orElseThrow();

        Assertions.assertEquals(2, movie.ratingCount());
        Assertions.assertEquals(9.0, movie.averageRating());
        Assertions.assertEquals(2, movie.userRatings().size());
    }

    @Test
    void testCreateMovie_duplicate_throwsBadRequest() throws Exception {
        createMovie("The Matrix", "Action", 1999);

        MovieRequest duplicate = new MovieRequest("The Matrix", "Action", 1999);

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(1, movieRepository.count());
    }

    @Test
    void testCreateMovie_invalidYear_throwsBadRequest() throws Exception {
        MovieRequest invalid = new MovieRequest("Future Movie", "Sci-Fi", 3000);

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        Assertions.assertEquals(0, movieRepository.count());
    }

    @Test
    void testAddRating_sameUserTwice_throwsError() throws Exception {
        String movieId = createMovie("Dune", "Sci-Fi", 2021);

        mockMvc.perform(post("/api/movies/" + movieId + "/rating")
                        .param("userId", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 9}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/movies/" + movieId + "/rating")
                        .param("userId", "user1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\": 10}"))
                .andExpect(status().isBadRequest());

        MovieEntity movie = movieRepository.findById(movieId).orElseThrow();
        Assertions.assertEquals(1, movie.ratingCount());
    }
}