// UpdateUserRequest.java
package com.keVend.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 64)
    private String name;

    @Size(max = 64)
    private String surname;

    @Pattern(regexp = "^\\+?[0-9\\s\\-().]{7,20}$", message = "Invalid phone number format")
    private String phone;

    /** ISO 639-1 code, e.g. "sq", "en" */
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Invalid locale tag")
    @Size(max = 8)
    private String preferredLocale;
}