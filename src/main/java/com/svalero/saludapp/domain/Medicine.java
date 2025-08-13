package com.svalero.saludapp.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Medicine")
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private String manufacturer;

    @Column
    private float price;

    @Column (name = "prescription_required")
    private boolean prescriptionRequired;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column
    private int stock;

    @OneToMany(mappedBy = "medicine")
    @JsonManagedReference(value = "medicine_prescriptions")
    private List<Prescription> prescriptions;
}