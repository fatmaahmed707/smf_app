package com.smf.service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smf.dto.zone.ZoneAccessResult;
import com.smf.model.Event;
import com.smf.model.enums.EventTypes;
import com.smf.repo.EventRepository;
import com.smf.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventService eventService;

    @Test
    void getEvents_success() {
        when(eventRepo.findRecent(any(Instant.class)))
                .thenReturn(List.of(new Event(EventTypes.TESTING,"mac","{}")));

        List<Event> events = eventService.getEvents(60);

        assertEquals(1, events.size());
    }

    @Test
    void getAllEvents_success() {
        when(eventRepo.findAll())
                .thenReturn(List.of(new Event(EventTypes.TESTING,"mac","{}")));

        List<Event> events = eventService.getAllEvents();

        assertEquals(1, events.size());
    }

    @Test
    void handleTest_shouldSaveEvent() {
        eventService.handleTest("mac");
        verify(eventRepo).save(any(Event.class));
    }

    @Test
    void handleDenied_shouldSaveEventAndBroadcast() {
        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.handleDenied("mac");

        verify(eventRepo).save(any(Event.class));
        verify(notificationService).broadcastEvent(eq(EventTypes.ACCESS_DENIED), anyString(), anyString(), any());
    }

    @Test
    void handleOnline_shouldSaveEvent() {
        eventService.handleOnline("mac");
        verify(eventRepo).save(any(Event.class));
    }

    @Test
    void handleGranted_shouldSaveEvent() {
        eventService.handleGranted("mac");
        verify(eventRepo).save(any(Event.class));
    }

    @Test
    void handleOffline_shouldSaveEventAndBroadcast() {
        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.handleOffline("mac");

        verify(eventRepo).save(any(Event.class));
        verify(notificationService).broadcastEvent(eq(EventTypes.DEVICE_OFFLINE), anyString(), anyString(), any());
    }

    @Test
    void handleSos_shouldSaveEventAndBroadcast() {
        when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.handleSos("mac");

        verify(eventRepo).save(any(Event.class));
        verify(notificationService).broadcastEvent(eq(EventTypes.SOS_TRIGGERED), anyString(), anyString(), any());
    }
}