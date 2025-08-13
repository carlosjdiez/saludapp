package com.svalero.saludapp.repository;

import com.svalero.saludapp.domain.Patient;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientRepository extends CrudRepository<Patient, Long> {

    List<Patient> findAll();
    List<Patient> findByName(String name);
    List<Patient> findBySurname(String surname);
    List<Patient> findByNameAndSurname(String name, String surname);


}