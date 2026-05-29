package com.smf.service.worker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smf.dto.worker.WorkerRequest;
import com.smf.dto.worker.WorkerResponse;
import com.smf.util.AppError;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

  @Mock RestClient supabaseRestClient;

  // RETURNS_SELF handles all chained builder calls (uri, body, header, etc.)
  // that return the same spec type — only retrieve() needs an explicit stub.
  @Mock(answer = Answers.RETURNS_SELF) RestClient.RequestBodyUriSpec requestBodyUriSpec;

  @SuppressWarnings("rawtypes")
  @Mock(answer = Answers.RETURNS_SELF) RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock RestClient.ResponseSpec responseSpec;

  private WorkerService workerService;
  private WorkerRequest request;
  private WorkerResponse response;
  private final UUID workerId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setup() {
    ObjectMapper objectMapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    workerService = new WorkerService(supabaseRestClient, objectMapper);

    request =
        new WorkerRequest(
            userId, null, "Test Worker", null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null);

    response =
        new WorkerResponse(
            workerId, null, "Test Worker", null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, Instant.now(), Instant.now());

    lenient().when(supabaseRestClient.post()).thenReturn(requestBodyUriSpec);
    lenient().when(supabaseRestClient.patch()).thenReturn(requestBodyUriSpec);
    lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

    lenient().when(supabaseRestClient.get()).thenReturn(requestHeadersUriSpec);
    lenient().when(supabaseRestClient.delete()).thenReturn(requestHeadersUriSpec);
    lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void create_validRequest_returnsResponse() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{response});

    WorkerResponse result = workerService.create(request);

    assertNotNull(result);
    assertEquals("Test Worker", result.fullNameEn());
  }

  @Test
  void create_supabaseReturnsEmpty_throwsBadGateway() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{});

    AppError ex = assertThrows(AppError.class, () -> workerService.create(request));
    assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
  }

  @Test
  void create_supabase5xx_throwsBadGateway() {
    when(responseSpec.body(WorkerResponse[].class))
        .thenThrow(mock(RestClientResponseException.class));

    AppError ex = assertThrows(AppError.class, () -> workerService.create(request));
    assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatus());
  }

  @Test
  void get_workerExists_returnsResponse() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{response});

    WorkerResponse result = workerService.get(workerId);

    assertNotNull(result);
    assertEquals(workerId, result.id());
  }

  @Test
  void get_workerMissing_throwsNotFound() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{});

    AppError ex = assertThrows(AppError.class, () -> workerService.get(workerId));
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
  }

  @Test
  void update_workerExists_returnsUpdatedResponse() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{response});

    WorkerResponse result = workerService.update(workerId, request);
    assertNotNull(result);
  }

  @Test
  void update_workerMissing_throwsNotFound() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{});

    AppError ex = assertThrows(AppError.class, () -> workerService.update(workerId, request));
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
  }

  @Test
  void delete_workerExists_succeeds() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{response});

    assertDoesNotThrow(() -> workerService.delete(workerId));
  }

  @Test
  void delete_workerMissing_throwsNotFound() {
    when(responseSpec.body(WorkerResponse[].class)).thenReturn(new WorkerResponse[]{});

    AppError ex = assertThrows(AppError.class, () -> workerService.delete(workerId));
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
  }
}
