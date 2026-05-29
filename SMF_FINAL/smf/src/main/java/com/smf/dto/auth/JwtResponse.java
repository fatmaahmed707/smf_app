package com.smf.dto.auth;

import java.util.UUID;


public record JwtResponse(UUID id, String accessToken, String refreshToken) {
}
