package com.smf.util;

import com.smf.dto.auth.RegisterRequest;
import com.smf.dto.worker.WorkerRequest;
import com.smf.dto.worker.WorkerResponse;
import com.smf.model.Announcement;
import com.smf.model.Device;
import com.smf.model.RegisteredDevice;
import com.smf.model.Role;
import com.smf.model.SmfDevice;
import com.smf.model.User;
import com.smf.model.Zone;
import com.smf.model.enums.AnnouncementPriority;
import com.smf.model.enums.AnnouncementStatus;
import com.smf.repo.AnnouncementRepository;
import com.smf.repo.DeviceRepository;
import com.smf.repo.RegisteredDeviceRepository;
import com.smf.repo.RoleRepository;
import com.smf.repo.SmfDeviceRepository;
import com.smf.repo.UserRepository;
import com.smf.repo.ZoneRepository;
import com.smf.service.auth.IAuthService;
import com.smf.service.worker.IWorkerService;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class SeedData implements CommandLineRunner {

  private final IAuthService authService;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final DeviceRepository deviceRepository;
  private final ZoneRepository zoneRepository;
  private final SmfDeviceRepository smfDeviceRepository;
  private final RegisteredDeviceRepository registeredDeviceRepository;
  private final AnnouncementRepository announcementRepository;
  private final EncryptionUtil encryptionUtil;
  private final IWorkerService workerService;
  private final RestClient supabaseRestClient;

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
      AnnouncementRepository announcementRepository,
      EncryptionUtil encryptionUtil,
      IWorkerService workerService,
      RestClient supabaseRestClient) {
    this.authService = authService;
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.deviceRepository = deviceRepository;
    this.zoneRepository = zoneRepository;
    this.smfDeviceRepository = smfDeviceRepository;
    this.registeredDeviceRepository = registeredDeviceRepository;
    this.announcementRepository = announcementRepository;
    this.encryptionUtil = encryptionUtil;
    this.workerService = workerService;
    this.supabaseRestClient = supabaseRestClient;
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
    User engineerUser =
        seedTestUser(
            "engineer", "engineer@test.com", "password", new HashSet<>(Set.of(engineerRole)));
    User managerUser =
        seedTestUser("manager", "manager@test.com", "password", new HashSet<>(Set.of(managerRole)));
    User workerUser =
        seedTestUser("worker", "worker@test.com", "password", new HashSet<>(Set.of(workerRole)));

    // Seed zones with role restrictions
    Zone zoneA = seedZone("Zone A - Engineering Only", new HashSet<>(Set.of(engineerRole)));
    Zone zoneB =
        seedZone(
            "Zone B - Engineering & Manager", new HashSet<>(Set.of(engineerRole, managerRole)));
    Zone zoneC = seedZone("Zone C - Open Access", new HashSet<>());

    Role[][] roleCombos = {
      {engineerRole},
      {managerRole},
      {workerRole},
      {engineerRole, managerRole},
      {managerRole, workerRole},
      {engineerRole, workerRole},
      {engineerRole, managerRole, workerRole},
      {}
    };
    for (int i = 1; i <= 50; i++) {
      Role[] combo = roleCombos[i % roleCombos.length];
      seedZone("Zone " + i, new HashSet<>(Set.of(combo)));
    }

    System.out.println("Zones seeded: Zone A, Zone B, Zone C, and Zone 1-50");

    // Seed devices in dev mode
    if (isDevMode) {
      seedTestDevices(adminUser, engineerUser, managerUser, workerUser);
      seedSmfDevice(
          adminUser,
          "28:56:2F:4A:87:6C",
          "smf device",
          "f09a641e6ecc4539cd6dd2d255801de5de5e7994e7e0a8c131aa9afd5ef21749");
    }

    // Seed announcements
    seedAnnouncements(adminUser);

    // Seed workers to Supabase
    seedWorkers();
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

  private void seedTestDevices(
      User adminUser, User engineerUser, User managerUser, User workerUser) {
    seedDevice("00:11:22:33:44:AA", adminUser, "Admin Device");
    seedDevice("00:11:22:33:44:BB", engineerUser, "Engineer Device");
    seedDevice("00:11:22:33:44:CC", managerUser, "Manager Device");
    seedDevice("00:11:22:33:44:DD", workerUser, "Worker Device");
  }

  private void seedDevice(String macAddress, User owner, String description) {
    if (deviceRepository.findByMacAddress(macAddress).isEmpty()) {
      System.out.println("Seeding device: " + description + " (" + macAddress + ")");
      Device device = new Device();
      device.setMacAddress(macAddress);
      device.setOwner(owner);
      device.setLastSeenTimestamp(Timestamp.from(Instant.now()));
      deviceRepository.save(device);
    }
  }

  private void seedAnnouncements(User admin) {
    if (announcementRepository.count() > 0) return;

    seedAnnouncement(
        admin,
        "Security Update Available",
        "System firmware v2.4.1 is ready for deployment. Please schedule maintenance window.",
        AnnouncementPriority.MEDIUM,
        Instant.now().minusSeconds(120));

    seedAnnouncement(
        admin,
        "New Zone Access Policy",
        "Zone B access has been restricted to ENGINEER and MANAGER roles effective immediately.",
        AnnouncementPriority.HIGH,
        Instant.now().minusSeconds(480));

    seedAnnouncement(
        admin,
        "System Health Check Complete",
        "Weekly diagnostics completed. All sensors and devices are operating within normal parameters.",
        AnnouncementPriority.LOW,
        Instant.now().minusSeconds(3600));

    seedAnnouncement(
        admin,
        "Planned Maintenance Window",
        "System will undergo scheduled maintenance Sunday 02:00–04:00 UTC. Expect brief downtime.",
        AnnouncementPriority.HIGH,
        Instant.now().plusSeconds(86400));

    System.out.println("Announcements seeded.");
  }

  private void seedAnnouncement(
      User createdBy, String title, String message, AnnouncementPriority priority, Instant time) {
    Announcement a = new Announcement();
    a.setTitle(title);
    a.setMessage(message);
    a.setPriority(priority);
    a.setCreatedAt(Instant.now());
    a.setCreatedBy(createdBy);

    if (time.isAfter(Instant.now())) {
      a.setStatus(AnnouncementStatus.SCHEDULED);
      a.setScheduledFor(time);
    } else {
      a.setStatus(AnnouncementStatus.SENT);
      a.setSentAt(time);
    }

    announcementRepository.save(a);
  }

  private void seedWorkers() {
    try {
      Role workerRole = roleRepository.findByRoleName("WORKER").orElseThrow();

      seedWorkerWithUser(workerRole, "omar.rashidi", "omar.rashidi@smf.com",
          new WorkerRequest(
              null, "عمر حسن الرشيدي", "Omar Hassan Al-Rashidi",
              LocalDate.of(1985, 3, 14),
              "شارع الملك فهد، الرياض", "King Fahd Road, Riyadh",
              "0501234567",
              "عامل بناء", "Construction Worker",
              "شركة الإنشاءات المتحدة", "United Construction Co.",
              "مشروع أبراج المدينة", "City Towers Project",
              "داء السكري من النوع الثاني", "Type 2 Diabetes",
              "يحتاج إلى مراقبة الجلوكوز بانتظام وتناول الأدوية عن طريق الفم.",
              "Requires regular glucose monitoring and oral medications.",
              "سارة عمر", "زوجة / Wife", "0509876543"));

      seedWorkerWithUser(workerRole, "fatima.alzahra", "fatima.alzahra@smf.com",
          new WorkerRequest(
              null, "فاطمة ناصر الزهراء", "Fatima Nasser Al-Zahra",
              LocalDate.of(1990, 7, 22),
              "حي النزهة، جدة", "Al-Nuzha District, Jeddah",
              "0551122334",
              "كهربائية", "Electrician",
              "شركة الطاقة الوطنية", "National Energy Co.",
              "محطة التوزيع الرئيسية", "Main Distribution Station",
              "ارتفاع ضغط الدم", "Hypertension",
              "تتناول أملوديبين 5 ملغ يومياً. تجنب الإجهاد الشديد.",
              "Takes Amlodipine 5mg daily. Avoid heavy exertion.",
              "خالد ناصر", "أخ / Brother", "0567788990"));

      seedWorkerWithUser(workerRole, "mohammed.alsayed", "mohammed.alsayed@smf.com",
          new WorkerRequest(
              null, "محمد خالد السيد", "Mohammed Khalid Al-Sayed",
              LocalDate.of(1992, 11, 5),
              "حي العزيزية، مكة المكرمة", "Al-Aziziyah District, Makkah",
              "0533445566",
              "عامل عام", "General Worker",
              "مجموعة الخليج للمقاولات", "Gulf Contracting Group",
              "مستودع الجنوب", "South Warehouse",
              null, null, null, null,
              "أمينة السيد", "أم / Mother", "0512233445"));

      seedWorkerWithUser(workerRole, "yousef.almansour", "yousef.almansour@smf.com",
          new WorkerRequest(
              null, "يوسف إبراهيم المنصور", "Yousef Ibrahim Al-Mansour",
              LocalDate.of(1988, 1, 30),
              "شارع التحلية، الدمام", "Al-Tahliyah Street, Dammam",
              "0544556677",
              "مشغّل رافعة", "Crane Operator",
              "شركة المنصور للرفع الثقيل", "Al-Mansour Heavy Lifting Co.",
              "ميناء الملك عبدالعزيز", "King Abdulaziz Port",
              "الربو", "Asthma",
              "يحمل بخاخ سالبوتامول للطوارئ. يتجنب الغبار والأدخنة.",
              "Carries Salbutamol inhaler for emergencies. Avoid dust and fumes.",
              "نورة المنصور", "زوجة / Wife", "0566778899"));

      System.out.println("Workers seeded to Supabase.");
    } catch (Exception ex) {
      log.warn("Worker seeding skipped — Supabase unreachable or key not configured: {}", ex.getMessage());
    }
  }

  private void seedWorkerWithUser(Role workerRole, String username, String email, WorkerRequest req) {
    User user = seedTestUser(username, email, "password", new HashSet<>(Set.of(workerRole)));
    if (user == null) return;
    try {
      workerService.get(user.getId());
    } catch (AppError ex) {
      if (ex.getStatus() == org.springframework.http.HttpStatus.NOT_FOUND) {
        workerService.create(req, user.getId());
      }
    }
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

