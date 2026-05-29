package com.smf.controller;

import com.smf.dto.api.ApiResponse;
import com.smf.dto.worker.WorkerRequest;
import com.smf.dto.worker.WorkerResponse;
import com.smf.service.worker.IWorkerService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/workers")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class WorkerController {

  private final IWorkerService workerService;

  @PostMapping
  public ResponseEntity<ApiResponse> create(@Valid @RequestBody WorkerRequest req) {
    WorkerResponse response = workerService.create(req);
    return ResponseEntity.ok(new ApiResponse(true, "Worker created successfully", response));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse> update(
      @PathVariable UUID id, @Valid @RequestBody WorkerRequest req) {
    WorkerResponse response = workerService.update(id, req);
    return ResponseEntity.ok(new ApiResponse(true, "Worker updated successfully", response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> delete(@PathVariable UUID id) {
    workerService.delete(id);
    return ResponseEntity.ok(new ApiResponse(true, "Worker deleted successfully", null));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> get(@PathVariable UUID id) {
    WorkerResponse response = workerService.get(id);
    return ResponseEntity.ok(new ApiResponse(true, "Worker fetched successfully", response));
  }
}
