package com.smf.service.announcement;

import com.smf.dto.announcement.AnnouncementRequest;
import com.smf.dto.announcement.AnnouncementResponse;
import com.smf.model.Announcement;
import com.smf.model.User;
import com.smf.model.enums.AnnouncementStatus;
import com.smf.repo.AnnouncementRepository;
import com.smf.repo.UserRepository;
import com.smf.service.notification.NotificationService;
import com.smf.util.AppError;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.smf.security.AppUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnnouncementService implements IAnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public AnnouncementResponse create(AnnouncementRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails principal)) {
      throw new AppError(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
    }
    User creator =
        userRepository
            .findById(principal.getId())
            .orElseThrow(() -> new AppError(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

    Announcement announcement = new Announcement();
    announcement.setTitle(request.title());
    announcement.setMessage(request.message());
    announcement.setPriority(request.priority());
    announcement.setCreatedAt(Instant.now());
    announcement.setCreatedBy(creator);
    announcement.setScheduledFor(request.scheduledFor());

    boolean sendNow =
        request.scheduledFor() == null || !request.scheduledFor().isAfter(Instant.now());

    if (sendNow) {
      announcement.setStatus(AnnouncementStatus.SENT);
      announcement.setSentAt(Instant.now());
    } else {
      announcement.setStatus(AnnouncementStatus.SCHEDULED);
    }

    announcement = announcementRepository.save(announcement);

    AnnouncementResponse response = mapToResponse(announcement);
    if (sendNow) {
      broadcastAfterCommit(response);
    }
    return response;
  }

  @Override
  public List<AnnouncementResponse> list(AnnouncementStatus status, String search) {
    List<Announcement> results;

    boolean hasSearch = search != null && !search.isBlank();
    boolean hasStatus = status != null;

    if (hasSearch && hasStatus) {
      results =
          announcementRepository
              .findByTitleContainingIgnoreCaseOrMessageContainingIgnoreCaseOrderByCreatedAtDesc(
                  search, search)
              .stream()
              .filter(a -> a.getStatus() == status)
              .collect(Collectors.toList());
    } else if (hasSearch) {
      results =
          announcementRepository
              .findByTitleContainingIgnoreCaseOrMessageContainingIgnoreCaseOrderByCreatedAtDesc(
                  search, search);
    } else if (hasStatus) {
      results = announcementRepository.findByStatusOrderByScheduledForAsc(status);
    } else {
      results = announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    return results.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Override
  public AnnouncementResponse getById(UUID id) {
    Announcement announcement =
        announcementRepository
            .findById(id)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Announcement not found"));
    return mapToResponse(announcement);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    Announcement announcement =
        announcementRepository
            .findById(id)
            .orElseThrow(() -> new AppError(HttpStatus.NOT_FOUND, "Announcement not found"));
    announcementRepository.delete(announcement);
  }

  @Override
  @Transactional
  public void dispatchScheduled() {
    List<Announcement> due =
        announcementRepository.findByStatusAndScheduledForLessThanEqual(
            AnnouncementStatus.SCHEDULED, Instant.now());

    for (Announcement announcement : due) {
      announcement.setStatus(AnnouncementStatus.SENT);
      announcement.setSentAt(Instant.now());
      announcement = announcementRepository.save(announcement);
      final AnnouncementResponse dispatched = mapToResponse(announcement);
      broadcastAfterCommit(dispatched);
      log.info("Dispatched scheduled announcement: id={}, title={}", announcement.getId(), announcement.getTitle());
    }
  }

  private void broadcastAfterCommit(AnnouncementResponse response) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          notificationService.broadcastAnnouncement(response);
        }
      });
    } else {
      notificationService.broadcastAnnouncement(response);
    }
  }

  private AnnouncementResponse mapToResponse(Announcement a) {
    return new AnnouncementResponse(
        a.getId(),
        a.getTitle(),
        a.getMessage(),
        a.getPriority(),
        a.getStatus(),
        a.getScheduledFor(),
        a.getSentAt(),
        a.getCreatedAt(),
        a.getCreatedBy().getId(),
        a.getCreatedBy().getUsername());
  }
}
