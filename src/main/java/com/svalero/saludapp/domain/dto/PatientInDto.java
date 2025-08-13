package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PatientInDto {
    private String name;
    private String surname;
    @Email(message = "Formato de email inválido")
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private Boolean active;
    @PositiveOrZero(message = "El peso no puede ser negativo")
    private Float weightKg;
}
