package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public record UserEntity(
    @Id
    String id,
    String username,
    String email,
    String password
) {
    public UserEntity(String username, String email, String password) {
        this(null, username, email, password);
    }
}