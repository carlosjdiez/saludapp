package com.svalero.saludapp.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class PatientOutDto {
    private long id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
    private boolean active;
    private float weightKg;
}
