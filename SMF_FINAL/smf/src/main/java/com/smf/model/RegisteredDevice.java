package com.smf.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "registered_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RegisteredDevice.RegisteredDeviceId.class)
public class RegisteredDevice {

  @Id
  @ManyToOne
  @JoinColumn(name = "smf_device_id", referencedColumnName = "id", nullable = false)
  private SmfDevice smfDevice;

  @Id
  @ManyToOne
  @JoinColumn(name = "device_id", referencedColumnName = "id", nullable = false)
  private Device device;

  @CreationTimestamp
  @Column(name = "registered_at", nullable = false, updatable = false)
  private LocalDateTime registeredAt;

  public RegisteredDevice(SmfDevice smfDevice, Device device) {
    this.smfDevice = smfDevice;
    this.device = device;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RegisteredDeviceId implements Serializable {
    private UUID smfDevice;
    private UUID device;
  }
}
