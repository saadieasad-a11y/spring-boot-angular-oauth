package com.oauth.controller;

import com.oauth.dto.AuthResponse;
import com.oauth.dto.UserDTO;
import com.oauth.entity.User;
import com.oauth.service.JwtTokenProvider;
import com.oauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/google/callback")
    public AuthResponse googleLogin(
            @AuthenticationPrincipal OAuth2User principal,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        return handleOAuth2Login(principal, authorizedClient, "GOOGLE");
    }

    @GetMapping("/microsoft/callback")
    public AuthResponse microsoftLogin(
            @AuthenticationPrincipal OAuth2User principal,
            @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient) {
        return handleOAuth2Login(principal, authorizedClient, "MICROSOFT");
    }

    @GetMapping("/me")
    public AuthResponse getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("No authenticated user");
            return response;
        }

        String email = principal.getAttribute("email");
        User user = userService.getUserById(extractUserIdFromPrincipal(principal));
        
        if (user == null) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage("User not found");
            return response;
        }

        AuthResponse response = new AuthResponse();
        response.setUser(userService.convertToDTO(user));
        response.setSuccess(true);
        response.setMessage("User retrieved successfully");
        return response;
    }

    @PostMapping("/logout")
    public AuthResponse logout() {
        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setMessage("Logged out successfully");
        return response;
    }

    private AuthResponse handleOAuth2Login(OAuth2User principal, OAuth2AuthorizedClient authorizedClient, String provider) {
        String email = principal.getAttribute("email");
        String firstName = principal.getAttribute("given_name");
        String lastName = principal.getAttribute("family_name");
        String providerId = principal.getName();
        String profilePicture = principal.getAttribute("picture");

        // Handle Microsoft specific attributes
        if (provider.equals("MICROSOFT")) {
            firstName = principal.getAttribute("given_name") != null ? principal.getAttribute("given_name") : "";
            lastName = principal.getAttribute("family_name") != null ? principal.getAttribute("family_name") : "";
        }

        User user = userService.findOrCreateUser(email, firstName, lastName, provider, providerId, profilePicture);

        // Save OAuth token
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        String refreshTokenValue = authorizedClient.getRefreshToken() != null ? authorizedClient.getRefreshToken().getTokenValue() : null;
        LocalDateTime expiresAt = null;
        if (accessToken.getExpiresAt() != null) {
            expiresAt = accessToken.getExpiresAt().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        userService.saveOAuthToken(user.getId(), provider, accessToken.getTokenValue(), refreshTokenValue, expiresAt);

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(jwtToken);
        response.setRefreshToken(refreshTokenValue);
        response.setUser(userService.convertToDTO(user));
        response.setSuccess(true);
        response.setMessage("Login successful");

        return response;
    }

    private Long extractUserIdFromPrincipal(OAuth2User principal) {
        // Extract user ID from principal - implementation depends on your needs
        return 1L; // Placeholder
    }
}
