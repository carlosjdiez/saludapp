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
@Entity(name = "Patient")
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private String surname;

    @Column
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column
    private boolean active;

    @Column(name = "weight_kg")
    private float weightKg;

    @OneToMany(mappedBy = "patient")
    @JsonManagedReference(value = "patient_appointments")
    private List<Appointment> appointments;
}
