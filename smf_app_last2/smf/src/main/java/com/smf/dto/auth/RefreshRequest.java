package com.smf.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank(message = "Refresh token required") String refreshToken) {
}
