package com.smf.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smf.dto.zone.ZoneAccessResult;
import com.smf.model.Event;
import com.smf.model.enums.EventTypes;
import com.smf.repo.EventRepository;
import com.smf.service.notification.NotificationService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventService implements IEventService {

  private final EventRepository eventRepo;
  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;

  @Override
  public List<Event> getEvents(int since) {
    Instant sinceTime = Instant.now().minusSeconds(since);
    return eventRepo.findRecent(sinceTime);
  }

  @Override
  public List<Event> getAllEvents() {
    return eventRepo.findAll();
  }

  @Override
  public void handleTest(String macAddress) {
    eventRepo.save(new Event(EventTypes.TESTING, macAddress, "{}"));
  }

  @Override
  public void handleDenied(String macAddress) {
    Event event = eventRepo.save(new Event(EventTypes.ACCESS_DENIED, macAddress, "{}"));
    notificationService.broadcastEvent(
        event.getEventType(), event.getMacAddress(), event.getMetadata(), event.getId());
  }

  @Override
  public void handleOnline(String macAddress) {
    eventRepo.save(new Event(EventTypes.DEVICE_ONLINE, macAddress, "{}"));
  }

  @Override
  public void handleGranted(String macAddress) {
    eventRepo.save(new Event(EventTypes.ACCESS_GRANTED, macAddress, "{}"));
  }

  @Override
  public void handleSos(String macAddress) {
    Event event = eventRepo.save(new Event(EventTypes.SOS_TRIGGERED, macAddress, "{}"));
    notificationService.broadcastEvent(
        event.getEventType(), event.getMacAddress(), event.getMetadata(), event.getId());
  }

  @Override
  public void handleOffline(String macAddress) {
    Event event = eventRepo.save(new Event(EventTypes.DEVICE_OFFLINE, macAddress, "{}"));
    notificationService.broadcastEvent(
        event.getEventType(), event.getMacAddress(), event.getMetadata(), event.getId());
  }

  @Override
  public void logZoneAccessEvent(ZoneAccessResult result, String macAddress) {
    ObjectNode metadata = objectMapper.createObjectNode();
    metadata.put("zoneId", result.zoneId().toString());
    metadata.put("zoneName", result.zoneName());
    metadata.put("accessGranted", result.granted());
    metadata.put("message", result.message());
    metadata.set("userRoles", objectMapper.valueToTree(result.userRoles()));
    metadata.set("zoneAllowedRoles", objectMapper.valueToTree(result.zoneAllowedRoles()));

    EventTypes eventType = result.granted() ? EventTypes.ACCESS_GRANTED : EventTypes.ACCESS_DENIED;
    Event event = eventRepo.save(new Event(eventType, macAddress, metadata.toString()));

    if (!result.granted()) {
      notificationService.broadcastEvent(
          event.getEventType(), event.getMacAddress(), event.getMetadata(), event.getId());
    }
  }
}