package com.svalero.saludapp.service;

import com.svalero.saludapp.domain.Appointment;
import com.svalero.saludapp.domain.Doctor;
import com.svalero.saludapp.domain.Patient;
import com.svalero.saludapp.domain.dto.AppointmentInDto;
import com.svalero.saludapp.domain.dto.AppointmentOutDto;
import com.svalero.saludapp.domain.dto.AppointmentRegistrationDto;
import com.svalero.saludapp.exception.AppointmentNotFoundException;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.repository.AppointmentRepository;
import com.svalero.saludapp.repository.DoctorRepository;
import com.svalero.saludapp.repository.PatientRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<AppointmentOutDto> getAll(LocalDate date, Boolean confirmed) {
        List<Appointment> appointments;
        if (date == null && confirmed == null) {
            appointments = appointmentRepository.findAll();
        } else if (date == null) {
            appointments = appointmentRepository.findByConfirmed(confirmed);
        } else if (confirmed == null) {
            appointments = appointmentRepository.findByDate(date);
        } else {
            appointments = appointmentRepository.findByDateAndConfirmed(date, confirmed);
        }
        return modelMapper.map(appointments, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }

    public Appointment get(long id) throws AppointmentNotFoundException {
        return appointmentRepository.findById(id)
                .orElseThrow(AppointmentNotFoundException::new);
    }

    public AppointmentOutDto add(long patientId, long doctorId, AppointmentRegistrationDto appointmentInDto)
            throws PatientNotFoundException, DoctorNotFoundException {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(PatientNotFoundException::new);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(DoctorNotFoundException::new);

        Appointment appointment = modelMapper.map(appointmentInDto, Appointment.class);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        Appointment newAppointment = appointmentRepository.save(appointment);

        return modelMapper.map(newAppointment, AppointmentOutDto.class);
    }

    public AppointmentOutDto modify(long appointmentId, AppointmentInDto appointmentInDto)
            throws AppointmentNotFoundException {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(AppointmentNotFoundException::new);

        modelMapper.map(appointmentInDto, appointment);
        appointmentRepository.save(appointment);

        return modelMapper.map(appointment, AppointmentOutDto.class);
    }

    public void remove(long id) throws AppointmentNotFoundException {
        appointmentRepository.findById(id)
                .orElseThrow(AppointmentNotFoundException::new);
        appointmentRepository.deleteById(id);
    }

    // ========= Operaciones JPQL =========
    public List<AppointmentOutDto> findByPatientEmailJPQL(String email) {
        List<Appointment> list = appointmentRepository.findByPatientEmailJPQL(email);
        return modelMapper.map(list, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }

    public List<AppointmentOutDto> findByDoctorNameJPQL(String name) {
        List<Appointment> list = appointmentRepository.findByDoctorNameJPQL(name);
        return modelMapper.map(list, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }

    public List<AppointmentOutDto> findByCostGreaterThanJPQL(float cost) {
        List<Appointment> list = appointmentRepository.findByCostGreaterThanJPQL(cost);
        return modelMapper.map(list, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }

    // ======== Operaciones SQL Nativas ========
    public List<AppointmentOutDto> findByPatientEmailNative(String email) {
        List<Appointment> list = appointmentRepository.findByPatientEmailNative(email);
        return modelMapper.map(list, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }

    public List<AppointmentOutDto> findByDoctorNameNative(String name) {
        List<Appointment> list = appointmentRepository.findByDoctorNameNative(name);
        return modelMapper.map(list, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }

    public List<AppointmentOutDto> findByCostGreaterThanNative(float cost) {
        List<Appointment> list = appointmentRepository.findByCostGreaterThanNative(cost);
        return modelMapper.map(list, new TypeToken<List<AppointmentOutDto>>() {}.getType());
    }
}
