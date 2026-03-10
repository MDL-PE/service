package ro.unibuc.prodeng.response;

public record AuthResponse(
    String token,
    String username
) {}