package com.smf.service.zone;

import com.smf.dto.zone.ZoneRequest;
import com.smf.dto.zone.ZoneResponse;
import com.smf.model.Device;
import com.smf.model.Role;
import com.smf.model.User;
import com.smf.model.Zone;
import com.smf.repo.ZoneRepository;
import com.smf.service.device.IDeviceService;
import com.smf.service.event.IEventService;
import com.smf.service.role.IRoleService;
import com.smf.service.zone.ZoneService;
import com.smf.util.AppError;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private IRoleService roleService;

    @Mock
    private IDeviceService deviceService;

    @Mock
    private IEventService eventService;

    @InjectMocks
    private ZoneService zoneService;

    private Role engineerRole;
    private Role workerRole;
    private Role managerRole;

    private User engineerUser;
    private User workerUser;
    private User multiRoleUser;
    private User noRoleUser;

    private Device engineerDevice;
    private Device workerDevice;
    private Device multiRoleDevice;
    private Device noRoleDevice;

    private Zone restrictedZone;
    private Zone openZone;
    private Zone multiRoleZone;

    @BeforeEach
    void setUp() {

        engineerRole = createRole(1L, "ENGINEER", false);
        workerRole = createRole(2L, "WORKER", false);
        managerRole = createRole(3L, "MANAGER", false);

        engineerUser = createUser(UUID.randomUUID(), "engineer@test.com", Set.of(engineerRole));
        workerUser = createUser(UUID.randomUUID(), "worker@test.com", Set.of(workerRole));
        multiRoleUser = createUser(UUID.randomUUID(), "multi@test.com", Set.of(engineerRole, workerRole));
        noRoleUser = createUser(UUID.randomUUID(), "norole@test.com", new HashSet<>());

        engineerDevice = createDevice(UUID.randomUUID(), engineerUser);
        workerDevice = createDevice(UUID.randomUUID(), workerUser);
        multiRoleDevice = createDevice(UUID.randomUUID(), multiRoleUser);
        noRoleDevice = createDevice(UUID.randomUUID(), noRoleUser);

        restrictedZone = createZone(UUID.randomUUID(), "Restricted Zone", Set.of(engineerRole));
        openZone = createZone(UUID.randomUUID(), "Open Zone", new HashSet<>());
        multiRoleZone = createZone(UUID.randomUUID(), "Multi Role Zone", Set.of(engineerRole, managerRole));
    }

    private Role createRole(Long id, String name, boolean isAdmin) {
        Role role = new Role();
        role.setId(id);
        role.setRoleName(name);
        role.setAdmin(isAdmin);
        return role;
    }

    private User createUser(UUID id, String email, Set<Role> roles) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(email.split("@")[0]);
        user.setRoles(roles);
        return user;
    }

    private Device createDevice(UUID id, User owner) {
        Device device = new Device();
        device.setId(id);
        device.setOwner(owner);
        device.setMacAddress(UUID.randomUUID().toString());
        return device;
    }

    private Zone createZone(UUID id, String name, Set<Role> roles) {
        Zone zone = new Zone();
        zone.setId(id);
        zone.setName(name);
        zone.setAllowedRoles(new HashSet<>(roles));
        return zone;
    }


    @Test
    void engineerCanAccessRestrictedZone() {

        when(deviceService.findDeviceById(engineerDevice.getId())).thenReturn(engineerDevice);
        when(zoneRepository.findById(restrictedZone.getId())).thenReturn(Optional.of(restrictedZone));

        boolean canAccess = zoneService.canDeviceAccessZone(engineerDevice.getId(), restrictedZone.getId());

        assertTrue(canAccess);
    }

    @Test
    void workerCannotAccessRestrictedZone() {

        when(deviceService.findDeviceById(workerDevice.getId())).thenReturn(workerDevice);
        when(zoneRepository.findById(restrictedZone.getId())).thenReturn(Optional.of(restrictedZone));

        boolean canAccess = zoneService.canDeviceAccessZone(workerDevice.getId(), restrictedZone.getId());

        assertFalse(canAccess);
    }

    @Test
    void everyoneCanAccessOpenZone() {

        when(deviceService.findDeviceById(engineerDevice.getId())).thenReturn(engineerDevice);
        when(zoneRepository.findById(openZone.getId())).thenReturn(Optional.of(openZone));

        boolean canAccess = zoneService.canDeviceAccessZone(engineerDevice.getId(), openZone.getId());

        assertTrue(canAccess);
    }

    @Test
    void multiRoleUserShouldAccessIfOneRoleMatches() {

        when(deviceService.findDeviceById(multiRoleDevice.getId())).thenReturn(multiRoleDevice);
        when(zoneRepository.findById(restrictedZone.getId())).thenReturn(Optional.of(restrictedZone));

        boolean canAccess = zoneService.canDeviceAccessZone(multiRoleDevice.getId(), restrictedZone.getId());

        assertTrue(canAccess);
    }

    @Test
    void userWithNoRolesShouldBeDenied() {

        when(deviceService.findDeviceById(noRoleDevice.getId())).thenReturn(noRoleDevice);
        when(zoneRepository.findById(restrictedZone.getId())).thenReturn(Optional.of(restrictedZone));

        boolean canAccess = zoneService.canDeviceAccessZone(noRoleDevice.getId(), restrictedZone.getId());

        assertFalse(canAccess);
    }



    @Test
    void createZoneSuccess() {

        ZoneRequest request = new ZoneRequest("New Zone");

        when(zoneRepository.findByName("New Zone")).thenReturn(Optional.empty());
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> {
            Zone zone = invocation.getArgument(0);
            zone.setId(UUID.randomUUID());
            return zone;
        });

        ZoneResponse response = zoneService.createZone(request);

        assertEquals("New Zone", response.name());
    }

    @Test
    void createZoneDuplicateShouldThrow() {

        ZoneRequest request = new ZoneRequest("Restricted Zone");

        when(zoneRepository.findByName("Restricted Zone")).thenReturn(Optional.of(restrictedZone));

        AppError error = assertThrows(AppError.class, () -> zoneService.createZone(request));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void getZoneByIdSuccess() {

        when(zoneRepository.findById(restrictedZone.getId())).thenReturn(Optional.of(restrictedZone));

        ZoneResponse response = zoneService.getZoneById(restrictedZone.getId());

        assertEquals(restrictedZone.getName(), response.name());
    }

    @Test
    void getZoneByIdNotFound() {

        UUID id = UUID.randomUUID();

        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        AppError error = assertThrows(AppError.class,
                () -> zoneService.getZoneById(id));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void deleteZoneSuccess() {

        when(zoneRepository.findById(restrictedZone.getId()))
                .thenReturn(Optional.of(restrictedZone));

        doNothing().when(zoneRepository).delete(restrictedZone);

        zoneService.deleteZone(restrictedZone.getId());

        verify(zoneRepository).delete(restrictedZone);
    }

    @Test
    void deleteZoneNotFound() {

        UUID id = UUID.randomUUID();

        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        AppError error = assertThrows(AppError.class,
                () -> zoneService.deleteZone(id));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void getAllZones_success() {

        when(zoneRepository.findAll()).thenReturn(List.of(restrictedZone, openZone));

        List<ZoneResponse> responses = zoneService.getAllZones();

        assertEquals(2, responses.size());
    }

    @Test
    void searchByName_success() {

        when(zoneRepository.findByNameContainingIgnoreCase("Restricted"))
                .thenReturn(List.of(restrictedZone));

        List<ZoneResponse> responses = zoneService.searchByName("Restricted");

        assertEquals(1, responses.size());
        assertEquals("Restricted Zone", responses.get(0).name());
    }

    @Test
    void updateZone_success() {

        ZoneRequest request = new ZoneRequest("Updated Zone");

        when(zoneRepository.findById(restrictedZone.getId()))
                .thenReturn(Optional.of(restrictedZone));
        when(zoneRepository.findByName("Updated Zone")).thenReturn(Optional.empty());
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ZoneResponse response = zoneService.updateZone(restrictedZone.getId(), request);

        assertEquals("Updated Zone", response.name());
    }

    @Test
    void updateZone_duplicateNameShouldThrow() {

        ZoneRequest request = new ZoneRequest("Open Zone");

        when(zoneRepository.findById(restrictedZone.getId()))
                .thenReturn(Optional.of(restrictedZone));
        when(zoneRepository.findByName("Open Zone")).thenReturn(Optional.of(openZone));

        AppError error = assertThrows(AppError.class,
                () -> zoneService.updateZone(restrictedZone.getId(), request));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
    }

    @Test
    void updateZone_notFound() {

        UUID id = UUID.randomUUID();
        ZoneRequest request = new ZoneRequest("New Zone");

        when(zoneRepository.findById(id)).thenReturn(Optional.empty());

        AppError error = assertThrows(AppError.class,
                () -> zoneService.updateZone(id, request));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    }

    @Test
    void assignRoleToZone_success() {

        when(zoneRepository.findById(restrictedZone.getId()))
                .thenReturn(Optional.of(restrictedZone));
        when(roleService.findRoleById(managerRole.getId())).thenReturn(managerRole);
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> zoneService.assignRoleToZone(restrictedZone.getId(), managerRole.getId()));

        verify(zoneRepository).save(any(Zone.class));
    }

    @Test
    void removeRoleFromZone_success() {

        restrictedZone.getAllowedRoles().add(managerRole);

        when(zoneRepository.findById(restrictedZone.getId()))
                .thenReturn(Optional.of(restrictedZone));
        when(roleService.findRoleById(managerRole.getId())).thenReturn(managerRole);
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> zoneService.removeRoleFromZone(restrictedZone.getId(), managerRole.getId()));

        verify(zoneRepository).save(any(Zone.class));
    }

    @Test
    void getAccessibleZones_success() {

        when(deviceService.findDeviceById(engineerDevice.getId())).thenReturn(engineerDevice);
        when(zoneRepository.findAll()).thenReturn(List.of(restrictedZone, openZone));

        List<ZoneResponse> responses = zoneService.getAccessibleZones(engineerDevice.getId());

        assertEquals(2, responses.size());
    }
}