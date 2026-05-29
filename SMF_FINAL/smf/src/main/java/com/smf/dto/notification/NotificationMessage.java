package com.smf.dto.notification;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
  private UUID eventId;
  private NotificationType type;
  private String macAddress;
  private String message;
  private Instant timestamp;
  private String metadata;

  public enum NotificationType {
    SOS_ALERT,
    UNAUTHORIZED_ACCESS,
    DEVICE_OFFLINE,
    DEVICE_ONLINE,
  }

  public static NotificationMessage fromEvent(
      UUID eventId, NotificationType type, String macAddress, String metadata) {
    return NotificationMessage.builder()
        .eventId(eventId)
        .type(type)
        .macAddress(macAddress)
        .message(generateMessage(type, macAddress))
        .timestamp(Instant.now())
        .metadata(metadata)
        .build();
  }

  private static String generateMessage(NotificationType type, String macAddress) {
    return switch (type) {
      case SOS_ALERT -> "SOS alert triggered by device: " + macAddress;
      case UNAUTHORIZED_ACCESS -> "Unauthorized access attempt by device: " + macAddress;
      case DEVICE_OFFLINE -> "Device went offline: " + macAddress;
      case DEVICE_ONLINE -> "Device came online: " + macAddress;
    };
  }
}