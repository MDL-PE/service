package ro.unibuc.prodeng.response;

public record UserProfileResponse(
    String id,
    String username,
    String email
) {}