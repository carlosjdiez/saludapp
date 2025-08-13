package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DoctorRegistrationDto {
    @NotBlank(message = "El campo name es obligatorio")
    private String name;
    @NotBlank(message = "El campo surname es obligatorio")
    private String surname;
    @NotBlank(message = "El número de licencia es obligatorio")
    private String licenseNumber;

    private String specialty;                  // opcional
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hiringDate;              // opcional (si null, el Service puede poner hoy)
    private Boolean active;                    // opcional
}
