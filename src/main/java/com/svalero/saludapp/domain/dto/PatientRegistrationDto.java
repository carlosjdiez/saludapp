package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PatientRegistrationDto {
    @NotBlank(message = "El campo name es obligatorio")
    private String name;
    @NotBlank(message = "El campo surname es obligatorio")
    private String surname;

    @Email(message = "Formato de email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "El campo active es obligatorio")
    private Boolean active;

    @PositiveOrZero(message = "El peso no puede ser negativo")
    private Float weightKg;
}
