package com.smf.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.smf.dto.announcement.AnnouncementRequest;
import com.smf.dto.announcement.AnnouncementResponse;
import com.smf.model.Announcement;
import com.smf.model.User;
import com.smf.model.enums.AnnouncementPriority;
import com.smf.model.enums.AnnouncementStatus;
import com.smf.repo.AnnouncementRepository;
import com.smf.repo.UserRepository;
import com.smf.security.AppUserDetails;
import com.smf.service.announcement.AnnouncementService;
import com.smf.service.notification.NotificationService;
import com.smf.util.AppError;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

  @Mock private AnnouncementRepository announcementRepository;
  @Mock private UserRepository userRepository;
  @Mock private NotificationService notificationService;

  @InjectMocks private AnnouncementService announcementService;

  private final UUID userId = UUID.randomUUID();
  private User creator;

  @BeforeEach
  void setup() {
    creator = new User("admin@test.com", "admin", "pass");
    creator.setId(userId);
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private void stubSecurityContext() {
    AppUserDetails principal = new AppUserDetails(userId, "admin@test.com", "pass", List.of());
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(principal);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void create_sendImmediately_statusSentAndBroadcast() {
    stubSecurityContext();
    AnnouncementRequest request =
        new AnnouncementRequest("Title", "Message body", AnnouncementPriority.HIGH, null);

    when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
    when(announcementRepository.save(any(Announcement.class)))
        .thenAnswer(i -> i.getArgument(0));

    AnnouncementResponse response = announcementService.create(request);

    assertEquals(AnnouncementStatus.SENT, response.status());
    assertNotNull(response.sentAt());
    verify(notificationService).broadcastAnnouncement(any(AnnouncementResponse.class));
  }

  @Test
  void create_scheduledInFuture_statusScheduledNoBroadcast() {
    stubSecurityContext();
    Instant future = Instant.now().plusSeconds(3600);
    AnnouncementRequest request =
        new AnnouncementRequest("Title", "Message body", AnnouncementPriority.LOW, future);

    when(userRepository.findById(userId)).thenReturn(Optional.of(creator));
    when(announcementRepository.save(any(Announcement.class)))
        .thenAnswer(i -> i.getArgument(0));

    AnnouncementResponse response = announcementService.create(request);

    assertEquals(AnnouncementStatus.SCHEDULED, response.status());
    assertNull(response.sentAt());
    verify(notificationService, never()).broadcastAnnouncement(any());
  }

  @Test
  void getById_existingId_returnsResponse() {
    UUID id = UUID.randomUUID();
    Announcement announcement = buildAnnouncement(id, AnnouncementStatus.SENT);

    when(announcementRepository.findById(id)).thenReturn(Optional.of(announcement));

    AnnouncementResponse response = announcementService.getById(id);

    assertEquals(id, response.id());
    assertEquals("Test title", response.title());
  }

  @Test
  void getById_missingId_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(announcementRepository.findById(id)).thenReturn(Optional.empty());

    AppError error = assertThrows(AppError.class, () -> announcementService.getById(id));
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
  }

  @Test
  void delete_existingId_deletesRow() {
    UUID id = UUID.randomUUID();
    Announcement announcement = buildAnnouncement(id, AnnouncementStatus.SENT);

    when(announcementRepository.findById(id)).thenReturn(Optional.of(announcement));

    announcementService.delete(id);

    verify(announcementRepository).delete(announcement);
  }

  @Test
  void delete_missingId_throwsNotFound() {
    UUID id = UUID.randomUUID();
    when(announcementRepository.findById(id)).thenReturn(Optional.empty());

    AppError error = assertThrows(AppError.class, () -> announcementService.delete(id));
    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
  }

  @Test
  void dispatchScheduled_dueRows_markedSentAndBroadcast() {
    Announcement due = buildAnnouncement(UUID.randomUUID(), AnnouncementStatus.SCHEDULED);
    due.setScheduledFor(Instant.now().minusSeconds(10));

    when(announcementRepository.findByStatusAndScheduledForLessThanEqual(
            eq(AnnouncementStatus.SCHEDULED), any(Instant.class)))
        .thenReturn(List.of(due));
    when(announcementRepository.save(any(Announcement.class))).thenAnswer(i -> i.getArgument(0));

    announcementService.dispatchScheduled();

    assertEquals(AnnouncementStatus.SENT, due.getStatus());
    assertNotNull(due.getSentAt());
    verify(notificationService).broadcastAnnouncement(any(AnnouncementResponse.class));
  }

  @Test
  void dispatchScheduled_noDueRows_noAction() {
    when(announcementRepository.findByStatusAndScheduledForLessThanEqual(
            eq(AnnouncementStatus.SCHEDULED), any(Instant.class)))
        .thenReturn(List.of());

    announcementService.dispatchScheduled();

    verify(notificationService, never()).broadcastAnnouncement(any());
    verify(announcementRepository, never()).save(any());
  }

  private Announcement buildAnnouncement(UUID id, AnnouncementStatus status) {
    Announcement a = new Announcement();
    a.setId(id);
    a.setTitle("Test title");
    a.setMessage("Test message");
    a.setPriority(AnnouncementPriority.MEDIUM);
    a.setStatus(status);
    a.setCreatedAt(Instant.now());
    a.setCreatedBy(creator);
    return a;
  }
}
