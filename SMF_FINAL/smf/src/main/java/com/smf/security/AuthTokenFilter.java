package com.smf.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smf.service.auth.AppUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;
	private final AppUserDetailsService service;

	public AuthTokenFilter(JwtUtils jwtUtils, AppUserDetailsService service) {
		this.jwtUtils = jwtUtils;
		this.service = service;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = parseJwt(request);

			if (StringUtils.hasText(jwt) && jwtUtils.validateJwtToken(jwt)) {
				// The subject in the JWT is the User ID (UUID), not the email
		String userId = jwtUtils.getUserIdFromToken(jwt);
				
				UserDetails userDetails = service.loadUserById(userId);
				
				var auth = new UsernamePasswordAuthenticationToken(userDetails,
						null,
						userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest req) {
		String headerAuth = req.getHeader("Authorization");
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7);
		}

		return null;
	}
}
