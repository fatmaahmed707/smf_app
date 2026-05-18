package com.smf.repo;

import com.smf.model.Zone;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {
  Optional<Zone> findByName(String name);

  List<Zone> findByNameContainingIgnoreCase(String name);
}
