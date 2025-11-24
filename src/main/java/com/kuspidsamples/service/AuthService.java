package com.kuspidsamples.service;

import com.kuspidsamples.dto.request.LoginRequest;
import com.kuspidsamples.dto.request.RegisterRequest;
import com.kuspidsamples.dto.response.AuthResponse;
import com.kuspidsamples.entity.RefreshToken;
import com.kuspidsamples.entity.Role;
import com.kuspidsamples.entity.User;
import com.kuspidsamples.exception.BadRequestException;
import com.kuspidsamples.exception.UnauthorizedException;
import com.kuspidsamples.repository.RefreshTokenRepository;
import com.kuspidsamples.repository.UserRepository;
import com.kuspidsamples.security.JwtTokenProvider;
import com.kuspidsamples.util.Constants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate username & email uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException(Constants.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException(Constants.EMAIL_ALREADY_EXISTS);
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = tokenProvider.generateTokenFromUsername(user.getUsername());
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Login a user
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Retrieve user
        User user = userRepository.findByUsernameOrEmail(
                request.getUsernameOrEmail(),
                request.getUsernameOrEmail()
        ).orElseThrow(() -> new UnauthorizedException(Constants.INVALID_CREDENTIALS));

        // Check account status
        if (!user.getAccountNonLocked()) {
            throw new UnauthorizedException(Constants.ACCOUNT_LOCKED);
        }
        if (!user.getEnabled()) {
            throw new UnauthorizedException(Constants.ACCOUNT_DISABLED);
        }

        // Update login info
        user.updateLastLogin();
        user.resetFailedLoginAttempts();
        userRepository.save(user);

        // Generate tokens
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new UnauthorizedException(Constants.INVALID_TOKEN));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException(Constants.TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        String accessToken = tokenProvider.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshTokenString,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * Logout user (delete refresh token)
     */
    @Transactional
    public void logout(String refreshTokenString) {
        refreshTokenRepository.deleteByToken(refreshTokenString);
    }

    /**
     * Create a new refresh token for a user
     */
    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(Constants.REFRESH_TOKEN_VALIDITY));
        refreshToken.setUser(user);

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
