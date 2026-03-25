package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public record UserEntity(
    @Id
    String id,
    String username,
    String email,
    String password,
    String role
) {

    public UserEntity(String username, String email, String role) {
        this(null, username, email, null, role);
    }

    public UserEntity(String id, String username, String email, String role) {
        this(id, username, email, null, role);
    }
}