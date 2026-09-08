package com.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String provider;
    private String profilePicture;
    private Long locationId; // User's location ID
    private LocalDateTime expiryDate; // Account expiry date
    private String locationName; // Location name for display
}
