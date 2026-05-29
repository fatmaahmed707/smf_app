package com.smf.service.announcement;

import com.smf.dto.announcement.AnnouncementRequest;
import com.smf.dto.announcement.AnnouncementResponse;
import com.smf.model.enums.AnnouncementStatus;
import java.util.List;
import java.util.UUID;

public interface IAnnouncementService {

  AnnouncementResponse create(AnnouncementRequest request);

  List<AnnouncementResponse> list(AnnouncementStatus status, String search);

  AnnouncementResponse getById(UUID id);

  void delete(UUID id);

  void dispatchScheduled();
}
