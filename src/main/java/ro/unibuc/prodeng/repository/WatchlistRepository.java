package ro.unibuc.prodeng.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.Watchlist;

public interface WatchlistRepository extends MongoRepository<Watchlist, String> {

    Optional<Watchlist> findByUserId(String userId);
}