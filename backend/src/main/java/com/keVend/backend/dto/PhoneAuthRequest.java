package com.keVend.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneAuthRequest {

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone number must be valid")
    private String phone;

    /** Required only on the verify endpoint. */
    private String code;
}
