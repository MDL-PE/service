package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, message = "Parola trebuie să aibă minim 6 caractere") String password
) {}