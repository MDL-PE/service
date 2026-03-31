package ro.unibuc.prodeng.response;

public record UserResponse(
    String id,
    String username,
    String email
) {}
