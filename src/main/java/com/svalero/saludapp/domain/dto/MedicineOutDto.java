package com.svalero.saludapp.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class MedicineOutDto {
    private long id;
    private String name;
    private String manufacturer;
    private float price;
    private boolean prescriptionRequired;
    private LocalDate expiryDate;
    private int stock;
}
