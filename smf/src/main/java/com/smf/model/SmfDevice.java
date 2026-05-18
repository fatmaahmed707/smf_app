package com.smf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "smf_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmfDevice {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(name = "mac_address", nullable = false, unique = true, length = 17)
  private String macAddress;

  @NaturalId
  @Column(name = "label", nullable = false)
  private String label;

  @Column(name = "secret", nullable = false)
  private String secret;

  @Column(name = "is_registered", nullable = false)
  private boolean isRegistered = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public SmfDevice(String macAddress, String label, String secret) {
    this.macAddress = macAddress;
    this.label = label;
    this.secret = secret;
    this.isRegistered = false;
  }
}
