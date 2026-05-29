package com.smf.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepo;

  @Mock private BCryptPasswordEncoder passwordEncoder;

  @Mock private AuthenticationManager authManager;

  @Mock private JwtUtils jwtUtils;

  @Mock private IRoleService roleService;

  @InjectMocks private AuthService authService;

  private User user;

  @BeforeEach
  void setup() {
    user = new User("test@mail.com", "testUser", "encodedPass");
    user.setId(UUID.randomUUID());
  }

  // ─── Existing tests (unchanged) ────────────────────────────────────────────

  @Test
  void register_shouldCreateUser_whenEmailNotExists() {
    RegisterRequest req = new RegisterRequest("test@mail.com", "testUser", "password");

    when(userRepo.existsByEmail(req.email())).thenReturn(false);
    when(passwordEncoder.encode(req.password())).thenReturn("encodedPass");
    when(userRepo.save(any(User.class))).thenReturn(user);

    User savedUser = authService.register(req);

    assertNotNull(savedUser);
    assertEquals("test@mail.com", savedUser.getEmail());

    verify(userRepo, times(1)).save(any(User.class));
  }

  @Test
  void register_shouldThrowException_whenEmailExists() {
    RegisterRequest req = new RegisterRequest("test@mail.com", "testUser", "password");

    when(userRepo.existsByEmail(req.email())).thenReturn(true);

    AppError exception = assertThrows(AppError.class, () -> authService.register(req));

    assertEquals(HttpStatus.CONFLICT, exception.getStatus());
  }

  @Test
  void login_shouldReturnJwtResponse_whenCredentialsValid() {
    LoginRequest req = new LoginRequest("test@mail.com", "password");

    Authentication authentication = mock(Authentication.class);
    AppUserDetails userDetails = mock(AppUserDetails.class);

    lenient().when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);

    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(userDetails.getUsername()).thenReturn(user.getEmail());
    
    lenient().when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    lenient().when(userRepo.save(any(User.class))).thenReturn(user);
    lenient().when(jwtUtils.generateTokenFromUserDetails(any(AppUserDetails.class))).thenReturn("mocked-jwt"); 

    JwtResponse response = authService.login(req);

    assertNotNull(response);
    assertEquals("mocked-jwt", response.accessToken());
    assertNotNull(response.refreshToken());
    verify(userRepo, times(1)).save(any(User.class)); // refresh token save
  }

  @Test
  void login_shouldThrowException_whenInvalidCredentials() {
    LoginRequest req = new LoginRequest("test@mail.com", "wrongpassword");

    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

    assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(req));
    
  }

  @Test
  void login_shouldThrowException_whenEmailNotExists() {
    LoginRequest req = new LoginRequest("nonexistent@mail.com", "password");

    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

    assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(req));
  }

  @Test
  void refresh_shouldReturnNewTokens_whenValidRefreshToken() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(tokenId);
    user.setRefreshTokenHash(sha256Hash(refreshToken));
    user.setRefreshTokenExpiry(LocalDateTime.now().plusHours(1));

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));
    when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtUtils.generateTokenFromUserDetails(any(AppUserDetails.class))).thenReturn("new-jwt");

    JwtResponse response = authService.refresh(refreshToken);

    assertNotNull(response);
    assertEquals("new-jwt", response.accessToken());
    assertNotNull(response.refreshToken());
    verify(userRepo, times(2)).save(any(User.class));
  }

  @Test
  void refresh_shouldThrowException_whenInvalidTokenFormat() {
    String invalidToken = "no-dot-here";

    AppError exception = assertThrows(AppError.class, () -> authService.refresh(invalidToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid refresh token format", exception.getMessage());
  }

  @Test
  void refresh_shouldThrowException_whenTokenNotFound() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.empty());

    AppError exception = assertThrows(AppError.class, () -> authService.refresh(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid refresh token", exception.getMessage());
  }

  @Test
  void refresh_shouldThrowException_whenRefreshTokenIdIsNull() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(null);
    user.setRefreshTokenHash("hashedToken");

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));

    AppError exception = assertThrows(AppError.class, () -> authService.refresh(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid or expired refresh token", exception.getMessage());
  }

  @Test
  void refresh_shouldThrowException_whenRefreshTokenExpiryIsNull() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(tokenId);
    user.setRefreshTokenHash("hashedToken");
    user.setRefreshTokenExpiry(null);

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));

    AppError exception = assertThrows(AppError.class, () -> authService.refresh(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid or expired refresh token", exception.getMessage());
  }

  @Test
  void refresh_shouldThrowException_whenTokenExpired() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(tokenId);
    user.setRefreshTokenHash("hashedToken");
    user.setRefreshTokenExpiry(LocalDateTime.now().minusHours(1));

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));

    AppError exception = assertThrows(AppError.class, () -> authService.refresh(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid or expired refresh token", exception.getMessage());
  }

  @Test
  void refresh_shouldThrowException_whenTokenHashMismatch() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(tokenId);
    user.setRefreshTokenHash("wrong-hash");
    user.setRefreshTokenExpiry(LocalDateTime.now().plusHours(1));

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));

    AppError exception = assertThrows(AppError.class, () -> authService.refresh(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid or expired refresh token", exception.getMessage());
  }

  @Test
  void logout_shouldClearTokens_whenValidRefreshToken() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(tokenId);
    user.setRefreshTokenHash(sha256Hash(refreshToken));

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));
    when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    assertDoesNotThrow(() -> authService.logout(refreshToken));

    verify(userRepo).save(any(User.class));
  }

  @Test
  void logout_shouldThrowException_whenInvalidTokenFormat() {
    String invalidToken = "no-dot-here";

    AppError exception = assertThrows(AppError.class, () -> authService.logout(invalidToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid refresh token format", exception.getMessage());
  }

  @Test
  void logout_shouldThrowException_whenTokenNotFound() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.empty());

    AppError exception = assertThrows(AppError.class, () -> authService.logout(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid refresh token", exception.getMessage());
  }

  @Test
  void logout_shouldThrowException_whenRefreshTokenIdIsNull() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(null);
    user.setRefreshTokenHash("hashedToken");

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));

    AppError exception = assertThrows(AppError.class, () -> authService.logout(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid refresh token", exception.getMessage());
  }

  @Test
  void logout_shouldThrowException_whenTokenHashMismatch() {
    String tokenId = UUID.randomUUID().toString();
    String refreshToken = tokenId + ".abc123";
    user.setRefreshTokenId(tokenId);
    user.setRefreshTokenHash("wrong-hash");

    when(userRepo.findByRefreshTokenId(tokenId)).thenReturn(Optional.of(user));

    AppError exception = assertThrows(AppError.class, () -> authService.logout(refreshToken));

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    assertEquals("Invalid refresh token", exception.getMessage());
  }

  // ─── Google Sign-In tests ──────────────────────────────────────────────────

  /** Subclass that lets tests inject a mock JwtDecoder instead of hitting Google's JWKS. */
  static class TestableAuthService extends AuthService {
    private final JwtDecoder mockDecoder;

    TestableAuthService(UserRepository userRepo, IRoleService roleService, BCryptPasswordEncoder enc,
        AuthenticationManager authManager, JwtUtils jwtUtils, JwtDecoder mockDecoder) {
      super(userRepo, roleService, enc, authManager, jwtUtils);
      this.mockDecoder = mockDecoder;
    }

    @Override
    protected JwtDecoder googleJwtDecoder() {
      return mockDecoder;
    }
  }

  private Jwt buildGoogleJwt(String subject, String email, String name, String picture,
      String clientId) {
    return Jwt.withTokenValue("fake-token")
        .header("alg", "RS256")
        .subject(subject)
        .issuer("https://accounts.google.com")
        .audience(List.of(clientId))
        .claim("email", email)
        .claim("email_verified", true)
        .claim("name", name)
        .claim("picture", picture)
        .issuedAt(java.time.Instant.now())
        .expiresAt(java.time.Instant.now().plusSeconds(3600))
        .build();
  }

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

  @Test
  void googleSignIn_shouldCreateNewUser_whenGoogleIdNotExists() {
    String googleId = "google-sub-123";
    String clientId = "test-client-id.apps.googleusercontent.com";
    Jwt jwt = buildGoogleJwt(googleId, "new@google.com", "New User", "https://pic.url", clientId);

    JwtDecoder mockDecoder = mock(JwtDecoder.class);
    when(mockDecoder.decode(anyString())).thenReturn(jwt);

    TestableAuthService svc = new TestableAuthService(userRepo, roleService, passwordEncoder, authManager, jwtUtils, mockDecoder);
    // Inject the client-id field via reflection
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdWeb", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdAndroid", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdIos", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "refreshTokenExpiryMs", 1209600000L);

    when(userRepo.findByGoogleId(googleId)).thenReturn(Optional.empty());
    when(userRepo.findByEmail("new@google.com")).thenReturn(Optional.empty());
    when(userRepo.save(any(User.class))).thenAnswer(inv -> {
      User u = inv.getArgument(0);
      u.setId(UUID.randomUUID());
      return u;
    });
    when(roleService.findRoleByName("ROLE_USER")).thenReturn(new Role("ROLE_USER"));
    when(jwtUtils.generateTokenFromUserDetails(any(AppUserDetails.class))).thenReturn("jwt-token");

    JwtResponse response = svc.googleSignIn("fake-token");

    assertNotNull(response);
    assertEquals("jwt-token", response.accessToken());
    verify(userRepo, atLeastOnce()).save(any(User.class));
  }

  @Test
  void googleSignIn_shouldReturnExistingUser_whenGoogleIdExists() {
    String googleId = "google-sub-456";
    String clientId = "test-client-id.apps.googleusercontent.com";
    Jwt jwt = buildGoogleJwt(googleId, "existing@google.com", "Existing User", "https://pic.url", clientId);

    JwtDecoder mockDecoder = mock(JwtDecoder.class);
    when(mockDecoder.decode(anyString())).thenReturn(jwt);

    User existingUser = new User("existing@google.com", "Existing User", null);
    existingUser.setId(UUID.randomUUID());
    existingUser.setGoogleId(googleId);
    existingUser.setProvider("GOOGLE");

    TestableAuthService svc = new TestableAuthService(userRepo, roleService, passwordEncoder, authManager, jwtUtils, mockDecoder);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdWeb", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdAndroid", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdIos", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "refreshTokenExpiryMs", 1209600000L);

    when(userRepo.findByGoogleId(googleId)).thenReturn(Optional.of(existingUser));
    when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtUtils.generateTokenFromUserDetails(any(AppUserDetails.class))).thenReturn("jwt-token");

    JwtResponse response = svc.googleSignIn("fake-token");

    assertNotNull(response);
    verify(userRepo, never()).findByEmail(anyString());
  }

  @Test
  void googleSignIn_shouldLinkExistingLocalAccount_whenEmailMatches() {
    String googleId = "google-sub-789";
    String clientId = "test-client-id.apps.googleusercontent.com";
    Jwt jwt = buildGoogleJwt(googleId, "local@app.com", "Local User", "https://pic.url", clientId);

    JwtDecoder mockDecoder = mock(JwtDecoder.class);
    when(mockDecoder.decode(anyString())).thenReturn(jwt);

    User localUser = new User("local@app.com", "Local User", "hashed-password");
    localUser.setId(UUID.randomUUID());

    TestableAuthService svc = new TestableAuthService(userRepo, roleService, passwordEncoder, authManager, jwtUtils, mockDecoder);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdWeb", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdAndroid", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdIos", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "refreshTokenExpiryMs", 1209600000L);

    when(userRepo.findByGoogleId(googleId)).thenReturn(Optional.empty());
    when(userRepo.findByEmail("local@app.com")).thenReturn(Optional.of(localUser));
    when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtUtils.generateTokenFromUserDetails(any(AppUserDetails.class))).thenReturn("jwt-token");

    JwtResponse response = svc.googleSignIn("fake-token");

    assertNotNull(response);
    assertEquals("GOOGLE", localUser.getProvider());
    assertEquals(googleId, localUser.getGoogleId());
  }

  @Test
  void googleSignIn_shouldThrow_whenTokenInvalid() {
    JwtDecoder mockDecoder = mock(JwtDecoder.class);
    when(mockDecoder.decode(anyString())).thenThrow(new JwtException("bad token"));

    TestableAuthService svc = new TestableAuthService(userRepo, roleService, passwordEncoder, authManager, jwtUtils, mockDecoder);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdWeb", "some-client-id");
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdAndroid", "some-client-id");
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdIos", "some-client-id");
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "refreshTokenExpiryMs", 1209600000L);

    AppError ex = assertThrows(AppError.class, () -> svc.googleSignIn("bad-token"));
    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    assertEquals("Invalid Google ID token", ex.getMessage());
  }

  @Test
  void googleSignIn_shouldThrow_whenAudienceMismatch() {
    String clientId = "correct-client-id.apps.googleusercontent.com";
    Jwt jwt = buildGoogleJwt("sub", "user@google.com", "User", "https://pic.url", "wrong-client-id");

    JwtDecoder mockDecoder = mock(JwtDecoder.class);
    when(mockDecoder.decode(anyString())).thenReturn(jwt);

    TestableAuthService svc = new TestableAuthService(userRepo, roleService, passwordEncoder, authManager, jwtUtils, mockDecoder);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdWeb", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdAndroid", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "googleClientIdIos", clientId);
    org.springframework.test.util.ReflectionTestUtils.setField(svc, "refreshTokenExpiryMs", 1209600000L);

    AppError ex = assertThrows(AppError.class, () -> svc.googleSignIn("fake-token"));
    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    assertEquals("Google ID token audience mismatch", ex.getMessage());
  }
}
