package com.svalero.saludapp.repository;

import com.svalero.saludapp.domain.Doctor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DoctorRepository extends CrudRepository<Doctor, Long> {

    List<Doctor> findAll();
    List<Doctor> findByName(String name);
    List<Doctor> findBySpecialty(String specialty);
    List<Doctor> findByNameAndSpecialty(String name, String specialty);

}