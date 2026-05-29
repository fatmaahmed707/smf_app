package com.smf.service.auth;

import com.smf.dto.auth.JwtResponse;
import com.smf.dto.auth.LoginRequest;
import com.smf.dto.auth.RegisterRequest;
import com.smf.model.Role;
import com.smf.model.User;
import com.smf.repo.UserRepository;
import com.smf.security.AppUserDetails;
import com.smf.security.JwtUtils;
import com.smf.service.role.IRoleService;
import com.smf.util.AppError;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService implements IAuthService {

  private final UserRepository userRepo;
  private final IRoleService roleService;
  private final BCryptPasswordEncoder passwordEncoder;
  private final AuthenticationManager authManager;
  private final JwtUtils jwtUtils;

  @Value("${jwt.refresh.expiration:1209600000}")
  private long refreshTokenExpiryMs;

  @Value("${google.jwks-uri}")
  private String googleJwksUri;

  @Value("${google.client-id-web}")
  private String googleClientIdWeb;

  @Value("${google.client-id-android}")
  private String googleClientIdAndroid;

  @Value("${google.client-id-ios}")
  private String googleClientIdIos;

  private volatile JwtDecoder cachedJwtDecoder;

  private String sha256Hash(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }

  private String generateRefreshToken(User user) {
    String refreshTokenId = UUID.randomUUID().toString();

    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[32];
    secureRandom.nextBytes(randomBytes);
    String randomSuffix = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

    String refreshToken = refreshTokenId + "." + randomSuffix;
    String hash = sha256Hash(refreshToken);

    user.setRefreshTokenId(refreshTokenId);
    user.setRefreshTokenHash(hash);
    user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
    userRepo.save(user);

    return refreshToken;
  }

  private JwtResponse generateTokens(User user) {
    AppUserDetails userDetails = AppUserDetails.buildUserDetails(user);
    String jwt = jwtUtils.generateTokenFromUserDetails(userDetails);
    String refreshToken = generateRefreshToken(user);
    return new JwtResponse(user.getId(), jwt, refreshToken);
  }

  @Override
  public JwtResponse login(LoginRequest req) {
    Authentication auth =
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
    SecurityContextHolder.getContext().setAuthentication(auth);
    AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
    User user =
        userRepo
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new AppError(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    return generateTokens(user);
  }

  @Override
  public User register(RegisterRequest req) {
    boolean alreadyExist = userRepo.existsByEmail(req.email());

    if (alreadyExist) throw new AppError(HttpStatus.CONFLICT, "Email Already Used");

    User newUser = new User(req.email(), req.username(), passwordEncoder.encode(req.password()));
    Role defaultRole = roleService.findRoleByName("ROLE_USER");
    newUser.getRoles().add(defaultRole);

    User savedUser = userRepo.save(newUser);
    return savedUser;
  }

  @Transactional
  public JwtResponse refresh(String refreshToken) {
    String[] parts = refreshToken.split("\\.", 2);
    if (parts.length != 2) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Invalid refresh token format");
    }
    String tokenId = parts[0];
    User user =
        userRepo
            .findByRefreshTokenId(tokenId)
            .orElseThrow(() -> new AppError(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
    if (user.getRefreshTokenHash() == null
        || user.getRefreshTokenExpiry() == null
        || user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())
        || !sha256Hash(refreshToken).equals(user.getRefreshTokenHash())) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }
    user.setRefreshTokenId(null);
    user.setRefreshTokenHash(null);
    user.setRefreshTokenExpiry(null);
    userRepo.save(user);
    return generateTokens(user);
  }

  @Transactional
  public void logout(String refreshToken) {
    String[] parts = refreshToken.split("\\.", 2);
    if (parts.length != 2) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Invalid refresh token format");
    }
    String tokenId = parts[0];
    User user =
        userRepo
            .findByRefreshTokenId(tokenId)
            .orElseThrow(() -> new AppError(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
    if (user.getRefreshTokenHash() == null
        || !sha256Hash(refreshToken).equals(user.getRefreshTokenHash())) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }
    user.setRefreshTokenId(null);
    user.setRefreshTokenHash(null);
    user.setRefreshTokenExpiry(null);
    userRepo.save(user);
  }

  @Override
  @Transactional
  public JwtResponse googleSignIn(String idToken) {
    // 1. Verify the Google ID token against Google's JWKS
    JwtDecoder decoder = googleJwtDecoder();
    Jwt jwt;
    try {
      jwt = decoder.decode(idToken);
    } catch (JwtException e) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
    }

    // 2. Validate audience matches one of our allowed client IDs
    List<String> audiences = jwt.getAudience();
    boolean validAudience =
        audiences != null
            && audiences.stream()
                .anyMatch(
                    a ->
                        a.equals(googleClientIdWeb)
                            || a.equals(googleClientIdAndroid)
                            || a.equals(googleClientIdIos));
    if (!validAudience) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Google ID token audience mismatch");
    }

    // 3. Validate issuer
    String issuer = jwt.getIssuer().toString();
    if (!"https://accounts.google.com".equals(issuer) && !"accounts.google.com".equals(issuer)) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Invalid Google ID token issuer");
    }

    // 4. Validate email_verified claim
    Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
    if (!Boolean.TRUE.equals(emailVerified)) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Google email not verified");
    }

    // 5. Extract and validate claims
    String googleId = jwt.getSubject();
    String email = jwt.getClaimAsString("email");

    if (googleId == null || googleId.isBlank()) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Google ID token missing subject");
    }
    if (email == null || email.isBlank()) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Google ID token missing email");
    }

    String name = jwt.getClaimAsString("name");
    String pictureUrl = jwt.getClaimAsString("picture");

    // 6. Find by Google ID first (returning user)
    User user =
        userRepo
            .findByGoogleId(googleId)
            .orElseGet(
                () -> {
                  // 7. Try to link an existing local account by email, or create new one
                  User u =
                      userRepo
                          .findByEmail(email)
                          .orElseGet(
                              () -> {
                                User newUser = new User(email, name != null ? name : email, null);
                                newUser.setProvider("GOOGLE");
                                Role defaultRole = roleService.findRoleByName("ROLE_USER");
                                newUser.getRoles().add(defaultRole);
                                return newUser;
                              });
                  if (u.getGoogleId() != null && !u.getGoogleId().equals(googleId)) {
                    throw new AppError(
                        HttpStatus.CONFLICT, "Email is already linked to another Google account");
                  }
                  u.setGoogleId(googleId);
                  u.setProvider("GOOGLE");
                  return u;
                });

    // Always keep the profile picture fresh
    user.setPictureUrl(pictureUrl);
    userRepo.save(user);

    return generateTokens(user);
  }

  /** Returns a cached JwtDecoder that reuses Google's JWKS via its internal cache. */
  protected JwtDecoder googleJwtDecoder() {
    if (cachedJwtDecoder == null) {
      synchronized (this) {
        if (cachedJwtDecoder == null) {
          cachedJwtDecoder = NimbusJwtDecoder.withJwkSetUri(googleJwksUri).build();
        }
      }
    }
    return cachedJwtDecoder;
  }
}
