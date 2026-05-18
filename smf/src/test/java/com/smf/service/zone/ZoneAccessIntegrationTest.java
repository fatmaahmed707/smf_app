package com.smf.service.zone;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smf.controller.DeviceController;
import com.smf.dto.zone.ZoneEntryRequest;
import com.smf.model.Device;
import com.smf.model.Event;
import com.smf.model.Role;
import com.smf.model.User;
import com.smf.model.Zone;
import com.smf.model.enums.EventTypes;
import com.smf.repo.DeviceRepository;
import com.smf.repo.EventRepository;
import com.smf.repo.RoleRepository;
import com.smf.repo.UserRepository;
import com.smf.repo.ZoneRepository;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ZoneAccessIntegrationTest {

  @Autowired private DeviceController deviceController;

  @Autowired private EventRepository eventRepository;

  @Autowired private DeviceRepository deviceRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ZoneRepository zoneRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private BCryptPasswordEncoder passwordEncoder;

  @Autowired private ObjectMapper objectMapper;

  private Role engineerRole;
  private Role workerRole;
  private User engineerUser;
  private User workerUser;
  private Device engineerDevice;
  private Device workerDevice;
  private Zone restrictedZone;
  private Zone openZone;

  @BeforeEach
  void setUp() {
    engineerRole = createRole("ENGINEER", false);
    workerRole = createRole("WORKER", false);

    // Use different emails to avoid conflicts with SeedData
    engineerUser =
        createUser("test-engineer@test.com", "test-engineer", new HashSet<>(Set.of(engineerRole)));
    workerUser =
        createUser("test-worker@test.com", "test-worker", new HashSet<>(Set.of(workerRole)));

    engineerDevice = createDevice("TEST:AA:BB:CC:01", engineerUser);
    workerDevice = createDevice("TEST:AA:BB:CC:02", workerUser);

    restrictedZone = createZone("Test Restricted Zone", new HashSet<>(Set.of(engineerRole)));
    openZone = createZone("Test Open Zone", new HashSet<>());

    eventRepository.deleteAll();
  }

  private Role createRole(String name, boolean isAdmin) {
    Role role = new Role();
    role.setRoleName(name);
    role.setAdmin(isAdmin);
    return roleRepository.save(role);
  }

  private User createUser(String email, String username, Set<Role> roles) {
    User user = new User();
    user.setEmail(email);
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode("password"));
    user.setRoles(roles);
    return userRepository.save(user);
  }

  private Device createDevice(String macAddress, User owner) {
    Device device = new Device();
    device.setMacAddress(macAddress);
    device.setOwner(owner);
    device.setLastSeenTimestamp(new Timestamp(System.currentTimeMillis()));
    return deviceRepository.save(device);
  }

  private Zone createZone(String name, Set<Role> allowedRoles) {
    Zone zone = new Zone();
    zone.setName(name);
    zone.setAllowedRoles(allowedRoles);
    return zoneRepository.save(zone);
  }

  private void setDeviceAuthentication(String macAddress) {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        macAddress, null, List.of(new SimpleGrantedAuthority("DEVICE")));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void zoneEntryWithAccess_shouldLogAccessGrantedEvent() throws Exception {
    setDeviceAuthentication(engineerDevice.getMacAddress());
    ZoneEntryRequest request = new ZoneEntryRequest(restrictedZone.getId());

    deviceController.handleZoneEntry(engineerDevice.getMacAddress(), request);

    List<Event> events = eventRepository.findAll();
    assertEquals(1, events.size(), "Should have logged one event");

    Event event = events.get(0);
    assertEquals(EventTypes.ACCESS_GRANTED, event.getEventType());
    assertEquals(engineerDevice.getMacAddress(), event.getMacAddress());

    JsonNode metadata = objectMapper.readTree(event.getMetadata());
    assertEquals(restrictedZone.getId().toString(), metadata.get("zoneId").asText());
    assertEquals(restrictedZone.getName(), metadata.get("zoneName").asText());
    assertTrue(metadata.get("accessGranted").asBoolean());
    assertEquals("Access granted", metadata.get("message").asText());

    JsonNode userRoles = metadata.get("userRoles");
    assertTrue(userRoles.isArray());
    assertEquals(1, userRoles.size());
    assertEquals("ENGINEER", userRoles.get(0).asText());

    JsonNode zoneAllowedRoles = metadata.get("zoneAllowedRoles");
    assertTrue(zoneAllowedRoles.isArray());
    assertEquals(1, zoneAllowedRoles.size());
    assertEquals("ENGINEER", zoneAllowedRoles.get(0).asText());
  }

  @Test
  void zoneEntryWithoutAccess_shouldLogAccessDeniedEvent() throws Exception {
    setDeviceAuthentication(workerDevice.getMacAddress());
    ZoneEntryRequest request = new ZoneEntryRequest(restrictedZone.getId());

    deviceController.handleZoneEntry(workerDevice.getMacAddress(), request);

    List<Event> events = eventRepository.findAll();
    assertEquals(1, events.size(), "Should have logged one event");

    Event event = events.get(0);
    assertEquals(EventTypes.ACCESS_DENIED, event.getEventType());
    assertEquals(workerDevice.getMacAddress(), event.getMacAddress());

    JsonNode metadata = objectMapper.readTree(event.getMetadata());
    assertEquals(restrictedZone.getId().toString(), metadata.get("zoneId").asText());
    assertEquals(restrictedZone.getName(), metadata.get("zoneName").asText());
    assertFalse(metadata.get("accessGranted").asBoolean());
    assertEquals("Access denied - insufficient role permissions", metadata.get("message").asText());

    JsonNode userRoles = metadata.get("userRoles");
    assertTrue(userRoles.isArray());
    assertEquals(1, userRoles.size());
    assertEquals("WORKER", userRoles.get(0).asText());

    JsonNode zoneAllowedRoles = metadata.get("zoneAllowedRoles");
    assertTrue(zoneAllowedRoles.isArray());
    assertEquals(1, zoneAllowedRoles.size());
    assertEquals("ENGINEER", zoneAllowedRoles.get(0).asText());
  }

  @Test
  void zoneEntryOpenZone_shouldLogAccessGrantedEvent() throws Exception {
    setDeviceAuthentication(workerDevice.getMacAddress());
    ZoneEntryRequest request = new ZoneEntryRequest(openZone.getId());

    deviceController.handleZoneEntry(workerDevice.getMacAddress(), request);

    List<Event> events = eventRepository.findAll();
    assertEquals(1, events.size(), "Should have logged one event");

    Event event = events.get(0);
    assertEquals(EventTypes.ACCESS_GRANTED, event.getEventType());
    assertEquals(workerDevice.getMacAddress(), event.getMacAddress());

    JsonNode metadata = objectMapper.readTree(event.getMetadata());
    assertEquals(openZone.getId().toString(), metadata.get("zoneId").asText());
    assertTrue(metadata.get("accessGranted").asBoolean());
    assertEquals("Access granted", metadata.get("message").asText());
  }

  @Test
  void multipleZoneEntries_shouldLogMultipleEvents() {
    setDeviceAuthentication(engineerDevice.getMacAddress());
    ZoneEntryRequest request = new ZoneEntryRequest(restrictedZone.getId());

    deviceController.handleZoneEntry(engineerDevice.getMacAddress(), request);
    
    SecurityContextHolder.clearContext();
    setDeviceAuthentication(workerDevice.getMacAddress());
    deviceController.handleZoneEntry(workerDevice.getMacAddress(), request);

    List<Event> events = eventRepository.findAll();
    assertEquals(2, events.size(), "Should have logged two events");

    long grantedCount =
        events.stream().filter(e -> e.getEventType() == EventTypes.ACCESS_GRANTED).count();
    long deniedCount =
        events.stream().filter(e -> e.getEventType() == EventTypes.ACCESS_DENIED).count();

    assertEquals(1, grantedCount, "Should have one ACCESS_GRANTED event");
    assertEquals(1, deniedCount, "Should have one ACCESS_DENIED event");
  }
}
