package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, message = "Password must have at least 6 characters") String password
) {}