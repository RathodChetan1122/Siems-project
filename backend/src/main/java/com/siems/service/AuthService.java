package com.siems.service;

import com.siems.dto.auth.AuthResponse;
import com.siems.dto.auth.LoginRequest;
import com.siems.dto.auth.RefreshTokenRequest;
import com.siems.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshAccessToken(RefreshTokenRequest request);
    void logout(String username);
}
