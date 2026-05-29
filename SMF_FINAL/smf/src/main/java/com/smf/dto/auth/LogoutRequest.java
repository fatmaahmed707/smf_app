package com.smf.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank(message = "Refresh token required") String refreshToken) {
}
