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
@Entity(name = "Doctor")
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private String surname;

    @Column (name = "license_number")
    private String licenseNumber;

    @Column
    private String specialty;

    @Column(name = "hiring_date")
    private LocalDate hiringDate;

    @Column
    private boolean active;

    @OneToMany(mappedBy = "doctor")
    @JsonManagedReference(value = "doctor_appointments")
    private List<Appointment> appointments;
}