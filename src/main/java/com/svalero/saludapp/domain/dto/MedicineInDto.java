package com.svalero.saludapp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class MedicineInDto {
    private String name;
    private String manufacturer;
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Float price;
    private Boolean prescriptionRequired;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}
