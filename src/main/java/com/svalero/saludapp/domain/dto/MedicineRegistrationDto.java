package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class MedicineRegistrationDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    private String manufacturer;
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Float price;

    private Boolean prescriptionRequired;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}
