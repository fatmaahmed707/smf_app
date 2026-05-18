package com.smf.service.notification;

import com.smf.dto.notification.NotificationMessage;
import com.smf.dto.notification.NotificationMessage.NotificationType;
import com.smf.model.enums.EventTypes;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
  private final SimpMessagingTemplate messagingTemplate;

  private static final String TOPIC_DESTINATION = "/topic/alerts";

  public void broadcastEvent(EventTypes eventType, String macAddress, String metadata, UUID eventId) {
    NotificationType notificationType = mapEventToNotification(eventType);
    if (notificationType == null) {
      log.debug("No notification type mapping for event type: {}", eventType);
      return;
    }

    NotificationMessage message =
        NotificationMessage.fromEvent(eventId, notificationType, macAddress, metadata);
    broadcast(message);
  }

  public void broadcast(NotificationMessage message) {
    log.info(
        "Broadcasting notification: type={}, macAddress={}, eventId={}",
        message.getType(),
        message.getMacAddress(),
        message.getEventId());
    messagingTemplate.convertAndSend(TOPIC_DESTINATION, message);
  }

  private NotificationType mapEventToNotification(EventTypes eventType) {
    return switch (eventType) {
      case SOS_TRIGGERED -> NotificationType.SOS_ALERT;
      case ACCESS_DENIED -> NotificationType.UNAUTHORIZED_ACCESS;
      case DEVICE_OFFLINE -> NotificationType.DEVICE_OFFLINE;
      case DEVICE_ONLINE -> NotificationType.DEVICE_ONLINE;
      case ACCESS_GRANTED, TESTING -> null;
    };
  }
}