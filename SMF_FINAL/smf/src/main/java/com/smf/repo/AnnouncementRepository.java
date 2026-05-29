package com.smf.repo;

import com.smf.model.Announcement;
import com.smf.model.enums.AnnouncementStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

  List<Announcement> findAllByOrderByCreatedAtDesc();

  List<Announcement> findByStatusOrderByScheduledForAsc(AnnouncementStatus status);

  List<Announcement> findByTitleContainingIgnoreCaseOrMessageContainingIgnoreCaseOrderByCreatedAtDesc(
      String title, String message);

  List<Announcement> findByStatusAndScheduledForLessThanEqual(
      AnnouncementStatus status, Instant cutoff);
}
