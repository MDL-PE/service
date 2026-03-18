package ro.unibuc.prodeng.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.LoginRequest;
import ro.unibuc.prodeng.request.RegisterRequest;
import ro.unibuc.prodeng.response.AuthResponse;
import ro.unibuc.prodeng.request.ChangePasswordRequest;
import ro.unibuc.prodeng.response.UserProfileResponse;

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
            throw new IllegalArgumentException("Email already used!");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already used!");
        }
        
        String assignedRole = (request.role() != null && request.role().equalsIgnoreCase("ADMIN")) 
                              ? "ROLE_ADMIN" : "ROLE_USER";

        UserEntity user = new UserEntity(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password()),
            assignedRole
        );

        userRepository.save(user);

        String token = jwtService.generateToken(user.email(), user.role());
        return new AuthResponse(token, user.username());
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Email or password incorrect"));

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new IllegalArgumentException("Email or password incorrect");
        }

        String token = jwtService.generateToken(user.email(), user.role());
        return new AuthResponse(token, user.username());
    }
    public UserProfileResponse getCurrentUserProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found!"));
        
        return new UserProfileResponse(user.id(), user.username(), user.email());
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

        if (!passwordEncoder.matches(request.oldPassword(), user.password())) {
            throw new IllegalArgumentException("Old password is incorrect!");
        }

        UserEntity updatedUser = new UserEntity(
            user.id(),
            user.username(),
            user.email(),
            passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(updatedUser);
    }
}