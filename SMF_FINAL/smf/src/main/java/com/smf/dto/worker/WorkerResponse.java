package com.smf.dto.worker;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WorkerResponse(
    UUID id,
    String fullNameAr,
    String fullNameEn,
    LocalDate dateOfBirth,
    String addressAr,
    String addressEn,
    String phone,
    String roleAr,
    String roleEn,
    String companyAr,
    String companyEn,
    String workLocationAr,
    String workLocationEn,
    String medicalConditionAr,
    String medicalConditionEn,
    String clinicalNotesAr,
    String clinicalNotesEn,
    String emergencyContactName,
    String emergencyContactRelation,
    String emergencyPhone,
    Instant createdAt,
    Instant updatedAt) {}
