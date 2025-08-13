package com.svalero.saludapp.repository;

import com.svalero.saludapp.domain.Medicine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends CrudRepository<Medicine, Long> {

    List<Medicine> findAll();
    List<Medicine> findByName(String name);
    List<Medicine> findByManufacturer(String manufacturer);
    List<Medicine> findByNameAndManufacturer(String name, String manufacturer);


}