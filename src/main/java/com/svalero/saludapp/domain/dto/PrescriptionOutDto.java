package com.svalero.saludapp.domain.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class PrescriptionOutDto {
    private long id;
    private String notes;
    private boolean active;
    private int durationDays;
    private float totalCost;
    private String dosageInstructions;
    private long appointmentId;
    private long medicineId;
}
