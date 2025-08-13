package com.svalero.saludapp.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DoctorOutDto {
    private long id;
    private String name;
    private String surname;
    private String licenseNumber;
    private String specialty;
    private LocalDate hiringDate;
    private boolean active;
}
