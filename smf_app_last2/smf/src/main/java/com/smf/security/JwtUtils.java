package com.smf.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtils {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.expiration}")
	private int jwtExpirationMs;

	private SecretKey key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(Authentication auth) {
		AppUserDetails userPrincipal = (AppUserDetails) auth.getPrincipal();
		return generateTokenFromUserDetails(userPrincipal);
	}

	public String generateTokenFromUserDetails(AppUserDetails userDetails) {
		List<String> roles = userDetails
				.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		return Jwts.builder()
				.subject(userDetails.getId().toString())
				.claim("email", userDetails.getUsername())
				.claim("roles", roles)
				.issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(key)
				.compact();
	}

	public String getUserIdFromToken(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}

	public boolean validateJwtToken(String token) throws JwtException {
		Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);

		return true;
	}
}