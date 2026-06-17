package com.siems.service.impl;

import com.siems.dto.auth.AuthResponse;
import com.siems.dto.auth.LoginRequest;
import com.siems.dto.auth.RefreshTokenRequest;
import com.siems.dto.auth.RegisterRequest;
import com.siems.entity.RefreshToken;
import com.siems.entity.Role;
import com.siems.entity.User;
import com.siems.exception.DuplicateResourceException;
import com.siems.exception.ResourceNotFoundException;
import com.siems.exception.UnauthorizedException;
import com.siems.repository.RefreshTokenRepository;
import com.siems.repository.RoleRepository;
import com.siems.repository.UserRepository;
import com.siems.security.JwtService;
import com.siems.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Set<String> VALID_ROLES =
            Set.of("ADMIN", "IMPORT_MANAGER", "EXPORT_MANAGER", "INVENTORY_MANAGER");

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        String roleName = request.getRoleName().toUpperCase();
        if (!VALID_ROLES.contains(roleName)) {
            throw new ResourceNotFoundException(
                    "Invalid role. Allowed roles: ADMIN, IMPORT_MANAGER, EXPORT_MANAGER, INVENTORY_MANAGER");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new UnauthorizedException("Refresh token expired or revoked. Please login again.");
        }

        if (!"REFRESH".equals(jwtService.extractTokenType(storedToken.getToken()))) {
            throw new UnauthorizedException("Invalid token type");
        }

        User user = storedToken.getUser();

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        refreshTokenRepository.revokeAllByUserId(user.getUserId());
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenStr = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpirationMs() / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().getRoleName())
                .expiresIn(jwtService.getAccessTokenExpirationMs())
                .build();
    }
}
