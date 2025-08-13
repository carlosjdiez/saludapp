package com.svalero.saludapp.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class AppointmentOutDto {
    private long id;
    private LocalDate date;
    private String reason;
    private boolean confirmed;
    private float cost;
    private int durationMinutes;
    private long patientId;
    private long doctorId;
}
