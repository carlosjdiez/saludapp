package com.svalero.saludapp.repository;

import com.svalero.saludapp.domain.Appointment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, Long> {

    // Búsquedas básicas usadas por getAll(...)
    List<Appointment> findAll();
    List<Appointment> findByDate(LocalDate date);
    List<Appointment> findByConfirmed(boolean confirmed);
    List<Appointment> findByDateAndConfirmed(LocalDate date, boolean confirmed);

    // =========================
    // JPQL (3 operaciones)
    // =========================

    // 1) Citas por email del paciente
    @Query("SELECT a FROM Appointment a WHERE a.patient.email = :email")
    List<Appointment> findByPatientEmailJPQL(@Param("email") String email);

    // 2) Citas por nombre del doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctor.name = :name")
    List<Appointment> findByDoctorNameJPQL(@Param("name") String name);

    // 3) Citas con coste mayor que X
    @Query("SELECT a FROM Appointment a WHERE a.cost > :cost")
    List<Appointment> findByCostGreaterThanJPQL(@Param("cost") float cost);

    // =========================
    // SQL Nativa (3 operaciones)
    // =========================

    // 1) Citas por email del paciente
    @Query(value = "SELECT a.* " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.id " +
            "WHERE p.email = :email",
            nativeQuery = true)
    List<Appointment> findByPatientEmailNative(@Param("email") String email);

    // 2) Citas por nombre del doctor
    @Query(value = "SELECT a.* " +
            "FROM appointments a " +
            "JOIN doctors d ON a.doctor_id = d.id " +
            "WHERE d.name = :name",
            nativeQuery = true)
    List<Appointment> findByDoctorNameNative(@Param("name") String name);

    // 3) Citas con coste mayor que X
    @Query(value = "SELECT * FROM appointments WHERE cost > :cost", nativeQuery = true)
    List<Appointment> findByCostGreaterThanNative(@Param("cost") float cost);
}
