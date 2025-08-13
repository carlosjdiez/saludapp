package com.svalero.saludapp.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class PrescriptionRegistrationDto {
    @NotBlank(message = "Las notas son obligatorias")
    private String notes;

    private Boolean active;
    @Min(value = 0, message = "La duración no puede ser negativa")
    private Integer durationDays;
    @Min(value = 0, message = "El coste total no puede ser negativo")
    private Float totalCost;
    private String dosageInstructions;
}
