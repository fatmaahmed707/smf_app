package com.smf.model;

import com.smf.model.enums.DeviceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "devices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Device {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "mac_address")
  @NaturalId
  private String macAddress;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
  private User owner;

  @ManyToOne
  @JoinColumn(name = "last_zone_id")
  private Zone lastZone;

  @Column(name = "last_seen_timestamp", nullable = true)
  private Timestamp lastSeenTimestamp;

  @Enumerated(EnumType.STRING)
  private DeviceStatus status;

  @Column(name = "violation_count")
  private Integer violationCount = 0;

  @Column(name = "label")
  private String label;

  @Column(name = "secret")
  private String secret;

  @Column(name = "is_registered", nullable = false)
  private boolean isRegistered = false;

  public Device(User owner) {
    this.owner = owner;
    this.status = DeviceStatus.OFFLINE;
    this.violationCount = 0;
  }
}
