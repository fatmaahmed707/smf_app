package com.smf.service.announcement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnnouncementScheduler {

  private final AnnouncementService announcementService;

  @Scheduled(fixedDelay = 60_000)
  public void runDueAnnouncements() {
    log.debug("Checking for due scheduled announcements");
    announcementService.dispatchScheduled();
  }
}
