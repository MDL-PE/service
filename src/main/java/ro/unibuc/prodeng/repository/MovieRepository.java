package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.MovieEntity;

import java.util.List;

@Repository
public interface MovieRepository extends MongoRepository<MovieEntity, String> {
    List<MovieEntity> findByTitleContainingIgnoreCase(String title);

    List<MovieEntity> findByGenre(String genre);

    List<MovieEntity> findByReleaseYear(int releaseYear);

}