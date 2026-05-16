package com.subscription.billing.billing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 120) String company
) {
}
