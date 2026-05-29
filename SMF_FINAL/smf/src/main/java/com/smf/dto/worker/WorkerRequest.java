package com.smf.dto.worker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WorkerRequest(
    @JsonProperty(value = "user_id", access = Access.WRITE_ONLY) @NotNull UUID userId,
    String fullNameAr,
    @NotBlank String fullNameEn,
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
    String emergencyPhone) {}
