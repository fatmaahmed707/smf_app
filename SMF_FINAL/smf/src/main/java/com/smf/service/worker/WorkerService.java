package com.smf.service.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smf.dto.worker.WorkerRequest;
import com.smf.dto.worker.WorkerResponse;
import com.smf.util.AppError;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkerService implements IWorkerService {

  private final RestClient supabaseRestClient;
  private final ObjectMapper objectMapper;

  @Override
  public WorkerResponse create(WorkerRequest req) {
    return create(req, req.userId());
  }

  @Override
  public WorkerResponse create(WorkerRequest req, UUID id) {
    ObjectNode body = objectMapper.valueToTree(req);
    body.put("id", id.toString());

    try {
      WorkerResponse[] result =
          supabaseRestClient.post().uri("/workers").body(body).retrieve().body(WorkerResponse[].class);
      if (result == null || result.length == 0) {
        throw new AppError(HttpStatus.BAD_GATEWAY, "Supabase returned empty response on create");
      }
      return result[0];
    } catch (RestClientResponseException ex) {
      log.error("Supabase create worker failed [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
      throw new AppError(HttpStatus.BAD_GATEWAY, "Failed to create worker");
    }
  }

  @Override
  public WorkerResponse update(UUID id, WorkerRequest req) {
    try {
      WorkerResponse[] result =
          supabaseRestClient
              .patch()
              .uri("/workers?id=eq." + id)
              .body(req)
              .retrieve()
              .body(WorkerResponse[].class);
      if (result == null || result.length == 0) {
        throw new AppError(HttpStatus.NOT_FOUND, "Worker not found");
      }
      return result[0];
    } catch (RestClientResponseException ex) {
      log.error("Supabase update worker failed [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
      throw new AppError(HttpStatus.BAD_GATEWAY, "Failed to update worker");
    }
  }

  @Override
  public void delete(UUID id) {
    try {
      WorkerResponse[] result =
          supabaseRestClient
              .delete()
              .uri("/workers?id=eq." + id)
              .retrieve()
              .body(WorkerResponse[].class);
      if (result == null || result.length == 0) {
        throw new AppError(HttpStatus.NOT_FOUND, "Worker not found");
      }
    } catch (RestClientResponseException ex) {
      log.error("Supabase delete worker failed [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
      throw new AppError(HttpStatus.BAD_GATEWAY, "Failed to delete worker");
    }
  }

  @Override
  public WorkerResponse get(UUID id) {
    try {
      WorkerResponse[] result =
          supabaseRestClient
              .get()
              .uri("/workers?id=eq." + id + "&select=*")
              .retrieve()
              .body(WorkerResponse[].class);
      if (result == null || result.length == 0) {
        throw new AppError(HttpStatus.NOT_FOUND, "Worker not found");
      }
      return result[0];
    } catch (RestClientResponseException ex) {
      log.error("Supabase get worker failed [{}]: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
      throw new AppError(HttpStatus.BAD_GATEWAY, "Failed to get worker");
    }
  }
}
