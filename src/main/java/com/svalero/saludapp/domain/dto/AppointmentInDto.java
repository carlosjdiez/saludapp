package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class AppointmentInDto {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String reason;
    private Boolean confirmed;
    @Min(value = 0, message = "El coste no puede ser negativo")
    private Float cost;
    @Min(value = 1, message = "La duración debe ser mayor que 0")
    private Integer durationMinutes;
}
