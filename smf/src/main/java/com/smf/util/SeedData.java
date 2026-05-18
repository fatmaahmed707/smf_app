package com.smf.util;

import com.smf.dto.auth.RegisterRequest;
import com.smf.model.Device;
import com.smf.model.RegisteredDevice;
import com.smf.model.Role;
import com.smf.model.SmfDevice;
import com.smf.model.User;
import com.smf.model.Zone;
import com.smf.repo.DeviceRepository;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.repo.RoleRepository;
import com.smf.repo.SmfDeviceRepository;
import com.smf.repo.UserRepository;
import com.smf.repo.ZoneRepository;
import com.smf.service.auth.IAuthService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedData implements CommandLineRunner {

  private final IAuthService authService;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final DeviceRepository deviceRepository;
  private final ZoneRepository zoneRepository;
  private final SmfDeviceRepository smfDeviceRepository;
  private final RegisteredDeviceRepository registeredDeviceRepository;
  private final EncryptionUtil encryptionUtil;

  @Value("${app.is-dev-mode:false}")
  private boolean isDevMode;

  public SeedData(
      IAuthService authService,
      RoleRepository roleRepository,
      UserRepository userRepository,
      DeviceRepository deviceRepository,
      ZoneRepository zoneRepository,
      SmfDeviceRepository smfDeviceRepository,
      RegisteredDeviceRepository registeredDeviceRepository,
      EncryptionUtil encryptionUtil) {
    this.authService = authService;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.deviceRepository = deviceRepository;
    this.zoneRepository = zoneRepository;
    this.smfDeviceRepository = smfDeviceRepository;
    this.registeredDeviceRepository = registeredDeviceRepository;
    this.encryptionUtil = encryptionUtil;
  }

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    // Seed roles
    Role adminRole = seedRole("ADMIN", true);
    Role engineerRole = seedRole("ENGINEER", false);
    Role managerRole = seedRole("MANAGER", false);
    Role workerRole = seedRole("WORKER", false);
    Role userRole = seedRole("ROLE_USER", false);

    System.out.println("Roles seeded: ADMIN, ENGINEER, MANAGER, WORKER, ROLE_USER");

    // Seed admin user
    User adminUser = seedAdminUser(adminRole);

    // Seed test users with different roles
    seedTestUser("engineer", "engineer@test.com", "password", new HashSet<>(Set.of(engineerRole)));
    seedTestUser("manager", "manager@test.com", "password", new HashSet<>(Set.of(managerRole)));
    seedTestUser("worker", "worker@test.com", "password", new HashSet<>(Set.of(workerRole)));

    // Seed zones with role restrictions
    Zone zoneA = seedZone("Zone A - Engineering Only", new HashSet<>(Set.of(engineerRole)));
    Zone zoneB =
        seedZone(
            "Zone B - Engineering & Manager", new HashSet<>(Set.of(engineerRole, managerRole)));
    Zone zoneC = seedZone("Zone C - Open Access", new HashSet<>());

    System.out.println("Zones seeded: Zone A, Zone B, Zone C");

    // Seed devices in dev mode
    if (isDevMode) {
      seedSmfDevice(
          adminUser,
          "28:56:2F:4A:87:6C",
          "smf device",
          "f09a641e6ecc4539cd6dd2d255801de5de5e7994e7e0a8c131aa9afd5ef21749");
    }
  }

  private Role seedRole(String roleName, boolean isAdmin) {
    return roleRepository
        .findByRoleName(roleName)
        .orElseGet(
            () -> {
              Role role = new Role();
              role.setRoleName(roleName);
              role.setAdmin(isAdmin);
              return roleRepository.save(role);
            });
  }

  private User seedAdminUser(Role adminRole) {
    if (!userRepository.existsByEmail("admin@smf.com")) {
      System.out.println("Seeding admin user...");
      RegisterRequest request = new RegisterRequest("admin@smf.com", "admin", "password");
      User user = authService.register(request);
      Set<Role> roles = new HashSet<>(Set.of(adminRole));
      user.setRoles(roles);
      System.out.println("Admin user seeded: admin@smf.com / admin");
      return userRepository.save(user);
    }
    return userRepository.findByEmail("admin@smf.com").orElse(null);
  }

  private User seedTestUser(String username, String email, String password, Set<Role> roles) {
    if (!userRepository.existsByEmail(email)) {
      System.out.println("Seeding " + username + " user...");
      RegisterRequest request = new RegisterRequest(email, username, password);
      User user = authService.register(request);
      user.setRoles(roles);
      System.out.println(username + " user seeded: " + email + " / " + password);
      return userRepository.save(user);
    }
    return userRepository.findByEmail(email).orElse(null);
  }

  private Zone seedZone(String name, Set<Role> allowedRoles) {
    return zoneRepository
        .findByName(name)
        .orElseGet(
            () -> {
              Zone zone = new Zone();
              zone.setName(name);
              zone.setAllowedRoles(allowedRoles);
              return zoneRepository.save(zone);
            });
  }

  private void seedSmfDevice(
      User owner, String macAddress, String label, String secret) {
    if (deviceRepository.findByMacAddress(macAddress).isEmpty()) {
      System.out.println("Seeding SMF device: " + label + " (" + macAddress + ")");
      Device device = new Device();
      device.setMacAddress(macAddress);
      device.setOwner(owner);
      device.setLabel(label);
      device.setSecret(encryptionUtil.encrypt(secret));
      device.setRegistered(true);
      device.setLastSeenTimestamp(Timestamp.from(Instant.now()));
      deviceRepository.save(device);

      if (smfDeviceRepository.findByMacAddress(macAddress).isEmpty()) {
        SmfDevice smfDevice = new SmfDevice();
        smfDevice.setMacAddress(macAddress);
        smfDevice.setLabel(label);
        smfDevice.setSecret(encryptionUtil.encrypt(secret));
        smfDevice.setRegistered(true);
        smfDeviceRepository.save(smfDevice);

        Device existingDevice = deviceRepository.findByMacAddress(macAddress).orElse(null);
        if (existingDevice != null) {
          RegisteredDevice regDevice = new RegisteredDevice(smfDevice, existingDevice);
          registeredDeviceRepository.save(regDevice);
        }
      }
    }
  }
}
