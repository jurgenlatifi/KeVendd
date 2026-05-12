// UserProfileResponse.java
package com.keVend.backend.dto;

import com.keVend.backend.model.User;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class UserProfileResponse {
    Long id;
    String name;
    String surname;
    String email;
    String phone;
    String role;
    boolean emailVerified;
    String preferredLocale;
    LocalDateTime createdAt;

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.getPreferredLocale(),
                user.getCreatedAt()
        );
    }
}