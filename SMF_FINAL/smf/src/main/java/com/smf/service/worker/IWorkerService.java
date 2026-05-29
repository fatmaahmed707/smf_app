package com.smf.service.worker;

import com.smf.dto.worker.WorkerRequest;
import com.smf.dto.worker.WorkerResponse;
import java.util.UUID;

public interface IWorkerService {
  WorkerResponse create(WorkerRequest req);
  WorkerResponse create(WorkerRequest req, UUID id);
  WorkerResponse update(UUID id, WorkerRequest req);
  void delete(UUID id);
  WorkerResponse get(UUID id);
}
