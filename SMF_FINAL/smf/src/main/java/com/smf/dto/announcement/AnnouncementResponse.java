package com.smf.dto.announcement;

import com.smf.model.enums.AnnouncementPriority;
import com.smf.model.enums.AnnouncementStatus;
import java.time.Instant;
import java.util.UUID;

public record AnnouncementResponse(
    UUID id,
    String title,
    String message,
    AnnouncementPriority priority,
    AnnouncementStatus status,
    Instant scheduledFor,
    Instant sentAt,
    Instant createdAt,
    UUID createdById,
    String createdByUsername) {}
