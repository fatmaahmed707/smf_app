package com.smf.model;

import com.smf.model.enums.EventTypes;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_logs")
@Getter
@Setter
@NoArgsConstructor
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "event_type")
  private EventTypes eventType;

  @Column(name = "mac_address")
  private String macAddress;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  public Event(EventTypes eventType, String macAddress, String metadata) {
    this.eventType = eventType;
    this.macAddress = macAddress;
    this.createdAt = Instant.now();
    this.metadata = metadata;
  }
}
