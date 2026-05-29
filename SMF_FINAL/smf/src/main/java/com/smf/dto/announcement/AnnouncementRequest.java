package com.smf.dto.announcement;

import com.smf.model.enums.AnnouncementPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record AnnouncementRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 400) String message,
    @NotNull AnnouncementPriority priority,
    Instant scheduledFor) {}
