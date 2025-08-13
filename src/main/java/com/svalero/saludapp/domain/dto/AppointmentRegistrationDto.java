package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class AppointmentRegistrationDto {
    @NotNull(message = "La fecha de la cita es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String reason;
    private Boolean confirmed;                 // opcional; false si null en Service
    @Min(value = 0, message = "El coste no puede ser negativo")
    private Float cost;                        // opcional
    @Min(value = 1, message = "La duración debe ser mayor que 0")
    private Integer durationMinutes;           // opcional
}
