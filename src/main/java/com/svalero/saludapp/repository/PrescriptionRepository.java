package com.svalero.saludapp.repository;

import com.svalero.saludapp.domain.Prescription;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends CrudRepository<Prescription, Long> {

    List<Prescription> findAll();
    List<Prescription> findByActive(boolean active);
    List<Prescription> findByDurationDays(int durationDays);
    List<Prescription> findByActiveAndDurationDays(boolean active, int durationDays);


}