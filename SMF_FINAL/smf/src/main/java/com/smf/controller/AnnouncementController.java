package com.smf.controller;

import com.smf.dto.announcement.AnnouncementRequest;
import com.smf.dto.announcement.AnnouncementResponse;
import com.smf.dto.api.ApiResponse;
import com.smf.model.enums.AnnouncementStatus;
import com.smf.ratelimit.RateLimit;
import com.smf.service.announcement.IAnnouncementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/announcements")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AnnouncementController {

  private final IAnnouncementService announcementService;

  @PostMapping("/")
  @RateLimit(capacity = 10, period = 60)
  public ResponseEntity<ApiResponse> createAnnouncement(
      @Valid @RequestBody AnnouncementRequest request) {
    AnnouncementResponse response = announcementService.create(request);
    return ResponseEntity.ok(new ApiResponse(true, "Announcement created successfully", response));
  }

  @GetMapping("/")
  public ResponseEntity<ApiResponse> getAnnouncements(
      @RequestParam(required = false) AnnouncementStatus status,
      @RequestParam(required = false) String search) {
    List<AnnouncementResponse> announcements = announcementService.list(status, search);
    return ResponseEntity.ok(
        new ApiResponse(true, "Announcements fetched successfully", announcements));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getAnnouncementById(@PathVariable UUID id) {
    AnnouncementResponse response = announcementService.getById(id);
    return ResponseEntity.ok(new ApiResponse(true, "Announcement fetched successfully", response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteAnnouncement(@PathVariable UUID id) {
    announcementService.delete(id);
    return ResponseEntity.ok(new ApiResponse(true, "Announcement deleted successfully", null));
  }
}
