package com.smf.model;

import com.smf.model.enums.AnnouncementPriority;
import com.smf.model.enums.AnnouncementStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
public class Announcement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 400)
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnnouncementPriority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnnouncementStatus status;

  @Column(name = "scheduled_for")
  private Instant scheduledFor;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;
}
