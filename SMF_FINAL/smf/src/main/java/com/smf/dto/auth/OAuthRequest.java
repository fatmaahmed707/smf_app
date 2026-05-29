package com.smf.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthRequest(@NotBlank String idToken) {}
