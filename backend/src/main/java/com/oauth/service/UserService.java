package com.oauth.service;

import com.oauth.dto.UserDTO;
import com.oauth.entity.OAuthToken;
import com.oauth.entity.User;
import com.oauth.repository.OAuthTokenRepository;
import com.oauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthTokenRepository oauthTokenRepository;

    public User findOrCreateUser(String email, String firstName, String lastName, 
                                  String provider, String providerId, String profilePicture) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setProfilePicture(profilePicture);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    public void saveOAuthToken(Long userId, String provider, String accessToken, 
                               String refreshToken, LocalDateTime expiresAt) {
        Optional<OAuthToken> existingToken = oauthTokenRepository.findByUserId(userId);
        
        OAuthToken token;
        if (existingToken.isPresent()) {
            token = existingToken.get();
        } else {
            token = new OAuthToken();
            token.setUserId(userId);
        }

        token.setProvider(provider);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(expiresAt);
        token.setCreatedAt(LocalDateTime.now());

        oauthTokenRepository.save(token);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setProvider(user.getProvider());
        dto.setProfilePicture(user.getProfilePicture());
        return dto;
    }
}
