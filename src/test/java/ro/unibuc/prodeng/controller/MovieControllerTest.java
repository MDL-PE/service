package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.response.MovieResponse;
import ro.unibuc.prodeng.service.MovieService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class MovieControllerTest {

        @Mock
        private MovieService movieService;

        @InjectMocks
        private MovieController movieController;

        private MockMvc mockMvc;

        private ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setup() {
                mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
        }

        // GET ALL MOVIES
        @Test
        void testGetAllMovies_returnsList() throws Exception {
                List<MovieResponse> movies = List.of(
                                new MovieResponse("1", "The Matrix", "Sci-Fi", 1999, 9.0, 100));

                when(movieService.getAllMovies()).thenReturn(movies);

                mockMvc.perform(get("/api/movies"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].title").value("The Matrix"));

                verify(movieService).getAllMovies();
        }

        // GET MOVIE BY ID
        @Test
        void testGetMovieById_returnsMovie() throws Exception {
                MovieResponse movie = new MovieResponse("1", "The Matrix", "Sci-Fi", 1999, 9.0, 100);

                when(movieService.getMovieById("1")).thenReturn(movie);

                mockMvc.perform(get("/api/movies/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("The Matrix"));
        }

        // CREATE MOVIE
        @Test
        void testCreateMovie_returnsCreated() throws Exception {
                MovieResponse response = new MovieResponse("1", "The Matrix", "Sci-Fi", 1999, 0.0, 0);

                when(movieService.createMovie(any())).thenReturn(response);

                String body = """
                                {
                                  "title": "The Matrix",
                                  "genre": "Sci-Fi",
                                  "releaseYear": 1999
                                }
                                """;

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("The Matrix"));
        }

        // CREATE MOVIE INVALID DATA
        @Test
        void testCreateMovie_invalidData() throws Exception {
                String body = """
                                {
                                  "title": "",
                                  "genre": "",
                                  "releaseYear": -1
                                }
                                """;

                mockMvc.perform(post("/api/movies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        // DELETE MOVIE
        @Test
        void testDeleteMovie_returnsNoContent() throws Exception {
                doNothing().when(movieService).deleteMovie("1");

                mockMvc.perform(delete("/api/movies/1"))
                                .andExpect(status().isNoContent());

                verify(movieService).deleteMovie("1");
        }

        // SEARCH BY TITLE
        @Test
        void testSearchByTitle_returnsList() throws Exception {
                List<MovieResponse> list = List.of(
                                new MovieResponse("1", "The Matrix", "Sci-Fi", 1999, 9.0, 100));

                when(movieService.searchByTitle("mat")).thenReturn(list);

                mockMvc.perform(get("/api/movies/search/title")
                                .param("title", "mat"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].title").value("The Matrix"));
        }

        // SEARCH BY GENRE
        @Test
        void testSearchByGenre_returnsList() throws Exception {
                List<MovieResponse> list = List.of(
                                new MovieResponse("1", "The Matrix", "Sci-Fi", 1999, 9.0, 100));

                when(movieService.getMoviesByGenre("Sci-Fi")).thenReturn(list);

                mockMvc.perform(get("/api/movies/search/genre")
                                .param("genre", "Sci-Fi"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].genre").value("Sci-Fi"));
        }

        // SEARCH BY YEAR
        @Test
        void testSearchByYear_returnsList() throws Exception {
                List<MovieResponse> list = List.of(
                                new MovieResponse("1", "Matrix", "Sci-Fi", 1999, 9.0, 100));

                when(movieService.getMoviesByYear(1999)).thenReturn(list);

                mockMvc.perform(get("/api/movies/search/year")
                                .param("year", "1999"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].releaseYear").value(1999));
        }

        // UPDATE MOVIE
        @Test
        void testUpdateMovie_returnsUpdated() throws Exception {
                MovieResponse response = new MovieResponse("1", "Updated", "Action", 2005, 0, 0);

                when(movieService.updateMovie(eq("1"), any())).thenReturn(response);

                String body = """
                                {
                                  "title": "Updated",
                                  "genre": "Action",
                                  "releaseYear": 2005
                                }
                                """;

                mockMvc.perform(put("/api/movies/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("Updated"));
        }

        // ADD RATING
        @Test
        void testAddRating_returnsUpdatedMovie() throws Exception {
                MovieResponse response = new MovieResponse("1", "The Matrix", "Sci-Fi", 1999, 9.5, 2);

                when(movieService.addRating(eq("1"), eq("user1"), any()))
                                .thenReturn(response);

                String body = """
                                {
                                  "rating": 10
                                }
                                """;

                mockMvc.perform(post("/api/movies/1/rating")
                                .param("userId", "user1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.averageRating").value(9.5));
        }
}