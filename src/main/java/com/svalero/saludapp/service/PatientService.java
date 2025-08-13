package com.svalero.saludapp.service;

import com.svalero.saludapp.domain.Patient;
import com.svalero.saludapp.domain.dto.PatientInDto;
import com.svalero.saludapp.domain.dto.PatientOutDto;
import com.svalero.saludapp.domain.dto.PatientRegistrationDto;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.repository.PatientRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<PatientOutDto> getAll(String name, String surname) {
        List<Patient> patients;
        if ((name == null || name.isEmpty()) && (surname == null || surname.isEmpty())) {
            patients = patientRepository.findAll();
        } else if (name == null || name.isEmpty()) {
            patients = patientRepository.findBySurname(surname);
        } else if (surname == null || surname.isEmpty()) {
            patients = patientRepository.findByName(name);
        } else {
            patients = patientRepository.findByNameAndSurname(name, surname);
        }
        return modelMapper.map(patients, new TypeToken<List<PatientOutDto>>() {}.getType());
    }

    public Patient get(long id) throws PatientNotFoundException {
        return patientRepository.findById(id)
                .orElseThrow(PatientNotFoundException::new);
    }

    public PatientOutDto add(PatientRegistrationDto patientInDto) {
        Patient patient = modelMapper.map(patientInDto, Patient.class);
        Patient newPatient = patientRepository.save(patient);
        return modelMapper.map(newPatient, PatientOutDto.class);
    }

    public PatientOutDto modify(long patientId, PatientInDto patientInDto) throws PatientNotFoundException {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(PatientNotFoundException::new);

        modelMapper.map(patientInDto, patient);
        patientRepository.save(patient);

        return modelMapper.map(patient, PatientOutDto.class);
    }

    public void remove(long id) throws PatientNotFoundException {
        patientRepository.findById(id)
                .orElseThrow(PatientNotFoundException::new);
        patientRepository.deleteById(id);
    }
}
