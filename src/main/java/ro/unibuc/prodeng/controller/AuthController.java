package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ro.unibuc.prodeng.request.ChangePasswordRequest;
import ro.unibuc.prodeng.request.LoginRequest;
import ro.unibuc.prodeng.request.RegisterRequest;
import ro.unibuc.prodeng.response.AuthResponse;
import ro.unibuc.prodeng.response.UserProfileResponse;
import ro.unibuc.prodeng.service.AuthService;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(principal.getName()));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok("Password was changed succesfully!");
    }
}