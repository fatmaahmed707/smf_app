package com.smf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
public class Zone {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(unique = true)
  String name;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "zone_roles",
      joinColumns = @JoinColumn(name = "zone_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> allowedRoles = new HashSet<>();
}
