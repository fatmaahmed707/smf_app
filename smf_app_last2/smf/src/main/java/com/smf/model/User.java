package com.smf.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import com.smf.model.Role;
import com.smf.model.Device;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = true, length = 255)
  private String refreshTokenHash;

  @Column(nullable = true)
  private LocalDateTime refreshTokenExpiry;

@Column(nullable = true, length = 36, unique = true)
  private String refreshTokenId;

  String username;

  @Column(nullable = false, unique = true)
  String email;

  String password;

  @Column(nullable = false, length = 20)
  private String provider = "LOCAL";

  @Column(nullable = true, unique = true)
  private String googleId;

  @Column(nullable = true, length = 512)
  private String pictureUrl;

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
  private Collection<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Collection<Device> devices = new HashSet<>();

  public User(String email, String username, String password) {
    this.email = email;
    this.username = username;
    this.password = password;
  }
}
