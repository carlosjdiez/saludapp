package com.svalero.saludapp.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Prescription")
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String notes;

    @Column
    private boolean active;

    @Column (name = "duration_days")
    private int durationDays;

    @Column (name = "total_cost")
    private float totalCost;

    @Column (name = "dosage_instructions")
    private String dosageInstructions;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    @JsonBackReference(value = "appointment_prescription")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    @JsonBackReference(value = "medicine_prescriptions")
    private Medicine medicine;
}