package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class DoctorInDto {
    private String name;
    private String surname;
    private String licenseNumber;
    private String specialty;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hiringDate;
    private Boolean active;
}
