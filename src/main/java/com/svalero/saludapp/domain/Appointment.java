package com.svalero.saludapp.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Appointment")
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private LocalDate date;

    @Column
    private String reason;

    @Column
    private boolean confirmed;

    @Column
    private float cost;

    @Column (name = "duration_minutes")
    private int durationMinutes;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonBackReference(value = "patient_appointments")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference(value = "doctor_appointments")
    private Doctor doctor;

    @OneToOne(mappedBy = "appointment")
    @JsonManagedReference(value = "appointment_prescription")
    private Prescription prescription;
}