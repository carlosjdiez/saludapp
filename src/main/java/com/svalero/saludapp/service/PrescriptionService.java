package com.svalero.saludapp.service;

import com.svalero.saludapp.domain.Appointment;
import com.svalero.saludapp.domain.Medicine;
import com.svalero.saludapp.domain.Prescription;
import com.svalero.saludapp.domain.dto.PrescriptionInDto;
import com.svalero.saludapp.domain.dto.PrescriptionOutDto;
import com.svalero.saludapp.domain.dto.PrescriptionRegistrationDto;
import com.svalero.saludapp.exception.AppointmentNotFoundException;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.exception.PrescriptionNotFoundException;
import com.svalero.saludapp.repository.AppointmentRepository;
import com.svalero.saludapp.repository.MedicineRepository;
import com.svalero.saludapp.repository.PrescriptionRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<PrescriptionOutDto> getAll(Boolean active, Integer durationDays) {
        List<Prescription> prescriptions;
        if (active == null && durationDays == null) {
            prescriptions = prescriptionRepository.findAll();
        } else if (active == null) {
            prescriptions = prescriptionRepository.findByDurationDays(durationDays);
        } else if (durationDays == null) {
            prescriptions = prescriptionRepository.findByActive(active);
        } else {
            prescriptions = prescriptionRepository.findByActiveAndDurationDays(active, durationDays);
        }
        return modelMapper.map(prescriptions, new TypeToken<List<PrescriptionOutDto>>() {}.getType());
    }

    public Prescription get(long id) throws PrescriptionNotFoundException {
        return prescriptionRepository.findById(id)
                .orElseThrow(PrescriptionNotFoundException::new);
    }

    public PrescriptionOutDto add(long appointmentId, long medicineId, PrescriptionRegistrationDto inDto)
            throws AppointmentNotFoundException, MedicineNotFoundException {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(AppointmentNotFoundException::new);
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(MedicineNotFoundException::new);

        Prescription prescription = modelMapper.map(inDto, Prescription.class);
        prescription.setAppointment(appointment);
        prescription.setMedicine(medicine);

        Prescription newPrescription = prescriptionRepository.save(prescription);
        return modelMapper.map(newPrescription, PrescriptionOutDto.class);
    }

    public PrescriptionOutDto modify(long prescriptionId, PrescriptionInDto inDto)
            throws PrescriptionNotFoundException {

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(PrescriptionNotFoundException::new);

        modelMapper.map(inDto, prescription);
        prescriptionRepository.save(prescription);

        return modelMapper.map(prescription, PrescriptionOutDto.class);
    }

    public void remove(long id) throws PrescriptionNotFoundException {
        prescriptionRepository.findById(id)
                .orElseThrow(PrescriptionNotFoundException::new);
        prescriptionRepository.deleteById(id);
    }
}
