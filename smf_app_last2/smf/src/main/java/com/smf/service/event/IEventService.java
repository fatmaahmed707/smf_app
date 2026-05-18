package com.smf.service.event;

import com.smf.dto.zone.ZoneAccessResult;
import com.smf.model.Event;
import java.util.List;

public interface IEventService {
  List<Event> getEvents(int since);

  List<Event> getAllEvents();

  void handleTest(String macAddress);

  void handleDenied(String macAddress);

  void handleOnline(String macAddress);

  void handleGranted(String macAddress);

  void handleSos(String macAddress);

  void handleOffline(String macAddress);

  void logZoneAccessEvent(ZoneAccessResult result, String macAddress);
}
