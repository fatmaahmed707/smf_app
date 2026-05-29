package com.smf.repo;

import com.smf.model.Event;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
  @Query("SELECT e FROM Event e WHERE e.createdAt > :since ORDER BY e.createdAt ASC")
  List<Event> findRecent(@Param("since") Instant since);
}
