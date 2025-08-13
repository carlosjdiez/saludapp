package com.svalero.saludapp.service;

import com.svalero.saludapp.domain.Doctor;
import com.svalero.saludapp.domain.dto.DoctorInDto;
import com.svalero.saludapp.domain.dto.DoctorOutDto;
import com.svalero.saludapp.domain.dto.DoctorRegistrationDto;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.repository.DoctorRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<DoctorOutDto> getAll(String name, String specialty) {
        List<Doctor> doctors;
        if ((name == null || name.isEmpty()) && (specialty == null || specialty.isEmpty())) {
            doctors = doctorRepository.findAll();
        } else if (name == null || name.isEmpty()) {
            doctors = doctorRepository.findBySpecialty(specialty);
        } else if (specialty == null || specialty.isEmpty()) {
            doctors = doctorRepository.findByName(name);
        } else {
            doctors = doctorRepository.findByNameAndSpecialty(name, specialty);
        }
        return modelMapper.map(doctors, new TypeToken<List<DoctorOutDto>>() {}.getType());
    }

    public Doctor get(long id) throws DoctorNotFoundException {
        return doctorRepository.findById(id)
                .orElseThrow(DoctorNotFoundException::new);
    }

    public DoctorOutDto add(DoctorRegistrationDto doctorInDto) {
        Doctor doctor = modelMapper.map(doctorInDto, Doctor.class);
        if (doctor.getHiringDate() == null) {
            doctor.setHiringDate(LocalDate.now()); // similar a registrationDate del ejemplo
        }
        Doctor newDoctor = doctorRepository.save(doctor);
        return modelMapper.map(newDoctor, DoctorOutDto.class);
    }

    public DoctorOutDto modify(long doctorId, DoctorInDto doctorInDto) throws DoctorNotFoundException {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(DoctorNotFoundException::new);

        modelMapper.map(doctorInDto, doctor);
        doctorRepository.save(doctor);

        return modelMapper.map(doctor, DoctorOutDto.class);
    }

    public void remove(long id) throws DoctorNotFoundException {
        doctorRepository.findById(id)
                .orElseThrow(DoctorNotFoundException::new);
        doctorRepository.deleteById(id);
    }
}
