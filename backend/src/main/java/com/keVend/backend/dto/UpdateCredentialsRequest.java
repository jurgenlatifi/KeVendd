// UpdateCredentialsRequest.java
package com.keVend.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCredentialsRequest {

    /** Required to authorize any credential change. */
    @NotBlank
    private String currentPassword;

    @Email
    @Size(max = 255)
    private String newEmail;       // null → no change

    @Size(min = 8, max = 128)
    private String newPassword;    // null → no change
}