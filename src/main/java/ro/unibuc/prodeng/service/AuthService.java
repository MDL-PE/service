package ro.unibuc.prodeng.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.LoginRequest;
import ro.unibuc.prodeng.request.RegisterRequest;
import ro.unibuc.prodeng.response.AuthResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email-ul este deja folosit!");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username-ul este deja folosit!");
        }

        UserEntity user = new UserEntity(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password())
        );

        userRepository.save(user);

        String token = jwtService.generateToken(user.email());
        return new AuthResponse(token, user.username());
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Email sau parolă incorectă"));

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new IllegalArgumentException("Email sau parolă incorectă");
        }

        String token = jwtService.generateToken(user.email());
        return new AuthResponse(token, user.username());
    }
}