package com.svalero.saludapp;

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
import com.svalero.saludapp.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTests {

    @InjectMocks
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------- getAll combinaciones ----------

    @Test
    public void testGetAll_NoFilters() {
        List<Appointment> mock = List.of(
                new Appointment(1, LocalDate.now(), "A", true, 10f, 10, null, null, null),
                new Appointment(2, LocalDate.now().plusDays(1), "B", false, 0f, 5, null, null, null)
        );
        List<AppointmentOutDto> out = List.of(
                new AppointmentOutDto(1, mock.get(0).getDate(), "A", true, 10f, 10, 10, 100),
                new AppointmentOutDto(2, mock.get(1).getDate(), "B", false, 0f, 5, 11, 101)
        );

        when(appointmentRepository.findAll()).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.getAll(null, null);

        assertEquals(2, result.size());
        verify(appointmentRepository, times(1)).findAll();
        verify(appointmentRepository, never()).findByConfirmed(anyBoolean());
        verify(appointmentRepository, never()).findByDate(any());
        verify(appointmentRepository, never()).findByDateAndConfirmed(any(), anyBoolean());
    }

    @Test
    public void testGetAll_ByDate() {
        LocalDate date = LocalDate.of(2025, 1, 10);
        List<Appointment> mock = List.of(new Appointment(1, date, "A", true, 10f, 10, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(1, date, "A", true, 10f, 10, 10, 100));

        when(appointmentRepository.findByDate(date)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.getAll(date, null);

        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByDate(date);
    }

    @Test
    public void testGetAll_ByConfirmed() {
        List<Appointment> mock = List.of(new Appointment(1, LocalDate.now(), "A", true, 10f, 10, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(1, LocalDate.now(), "A", true, 10f, 10, 10, 100));

        when(appointmentRepository.findByConfirmed(true)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.getAll(null, true);

        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByConfirmed(true);
    }

    @Test
    public void testGetAll_ByDateAndConfirmed() {
        LocalDate date = LocalDate.of(2025, 1, 10);
        List<Appointment> mock = List.of(new Appointment(1, date, "A", false, 10f, 10, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(1, date, "A", false, 10f, 10, 10, 100));

        when(appointmentRepository.findByDateAndConfirmed(date, false)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.getAll(date, false);

        assertEquals(1, result.size());
        verify(appointmentRepository, times(1)).findByDateAndConfirmed(date, false);
    }

    // ---------- get(id) ----------

    @Test
    public void testGet_ReturnsAppointment() throws AppointmentNotFoundException {
        Appointment a = new Appointment(1, LocalDate.now(), "A", true, 10f, 10, null, null, null);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));

        Appointment result = appointmentService.get(1L);

        assertEquals(1, result.getId());
    }

    @Test
    public void testGet_NotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.get(99L));
    }

    // ---------- add(patientId, doctorId, dto) ----------

    @Test
    public void testAdd_Ok() throws PatientNotFoundException, DoctorNotFoundException {
        long patientId = 10, doctorId = 100;

        Patient p = new Patient(
                patientId, "Pepe", "Pérez", "p@e.com",
                LocalDate.of(1990, 1, 1), true, 70.5f, null
        );

        Doctor d = new Doctor(
                doctorId, "Gregory", "House", "LIC123", "Diagnóstico",
                LocalDate.of(2010, 1, 1), true, null
        );

        AppointmentRegistrationDto in = new AppointmentRegistrationDto(LocalDate.now().plusDays(1), "Fiebre", true, 20f, 15);
        Appointment toSave = new Appointment(0, in.getDate(), "Fiebre", true, 20f, 15, p, d, null);
        Appointment saved = new Appointment(1, in.getDate(), "Fiebre", true, 20f, 15, p, d, null);
        AppointmentOutDto out = new AppointmentOutDto(1, in.getDate(), "Fiebre", true, 20f, 15, patientId, doctorId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(p));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(d));
        when(modelMapper.map(in, Appointment.class)).thenReturn(toSave);
        when(appointmentRepository.save(toSave)).thenReturn(saved);
        when(modelMapper.map(saved, AppointmentOutDto.class)).thenReturn(out);

        AppointmentOutDto result = appointmentService.add(patientId, doctorId, in);

        assertEquals(1, result.getId());
        assertEquals("Fiebre", result.getReason());
        verify(appointmentRepository, times(1)).save(toSave);
    }

    @Test
    public void testAdd_PatientNotFound() {
        long patientId = 999, doctorId = 100;
        AppointmentRegistrationDto in = new AppointmentRegistrationDto(LocalDate.now(), "X", false, 0f, 10);

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> appointmentService.add(patientId, doctorId, in));
        verify(doctorRepository, never()).findById(anyLong());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    public void testAdd_DoctorNotFound() {
        long patientId = 10, doctorId = 999;
        AppointmentRegistrationDto in = new AppointmentRegistrationDto(LocalDate.now(), "X", false, 0f, 10);

        Patient p = new Patient(
                patientId, "Pepe", "Pérez", "p@e.com",
                LocalDate.of(1990, 1, 1), true, 70.5f, null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(p));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThrows(DoctorNotFoundException.class, () -> appointmentService.add(patientId, doctorId, in));
        verify(appointmentRepository, never()).save(any());
    }

    // ---------- modify(id, dto) ----------

    @Test
    public void testModify_Ok() throws AppointmentNotFoundException {
        long id = 1;
        AppointmentInDto in = new AppointmentInDto(LocalDate.now().plusDays(2), "Cambio", true, 40f, 25);
        Appointment entity = new Appointment(id, LocalDate.now(), "Viejo", false, 0f, 5, null, null, null);
        AppointmentOutDto out = new AppointmentOutDto(id, in.getDate(), "Cambio", true, 40f, 25, 10, 100);

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(entity));
        // ModelMapper muta "entity" con los valores de "in"
        doAnswer(inv -> {
            AppointmentInDto src = inv.getArgument(0);
            Appointment dest = inv.getArgument(1);
            dest.setDate(src.getDate());
            dest.setReason(src.getReason());
            dest.setConfirmed(Boolean.TRUE.equals(src.getConfirmed()));
            if (src.getCost() != null) dest.setCost(src.getCost());
            if (src.getDurationMinutes() != null) dest.setDurationMinutes(src.getDurationMinutes());
            return null;
        }).when(modelMapper).map(in, entity);

        when(modelMapper.map(entity, AppointmentOutDto.class)).thenReturn(out);

        AppointmentOutDto result = appointmentService.modify(id, in);

        assertEquals("Cambio", result.getReason());
        assertTrue(result.isConfirmed());
        verify(appointmentRepository, times(1)).save(entity);
    }

    @Test
    public void testModify_NotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        AppointmentInDto in = new AppointmentInDto(LocalDate.now(), "X", false, 0f, 10);
        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.modify(99L, in));
    }

    // ---------- remove(id) ----------

    @Test
    public void testRemove_Ok() throws AppointmentNotFoundException {
        Appointment a = new Appointment(1, LocalDate.now(), "A", true, 10f, 10, null, null, null);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));

        appointmentService.remove(1L);

        verify(appointmentRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRemove_NotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.remove(99L));
        verify(appointmentRepository, never()).deleteById(anyLong());
    }

    // ---------- JPQL (3 métodos) ----------

    @Test
    public void testFindByPatientEmailJPQL() {
        List<Appointment> mock = List.of(new Appointment(1, LocalDate.now(), "A", true, 5f, 5, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(1, LocalDate.now(), "A", true, 5f, 5, 10, 100));

        when(appointmentRepository.findByPatientEmailJPQL("p@e.com")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.findByPatientEmailJPQL("p@e.com");
        assertEquals(1, result.size());
    }

    @Test
    public void testFindByDoctorNameJPQL() {
        List<Appointment> mock = List.of(new Appointment(2, LocalDate.now(), "B", false, 0f, 10, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(2, LocalDate.now(), "B", false, 0f, 10, 11, 101));

        when(appointmentRepository.findByDoctorNameJPQL("House")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.findByDoctorNameJPQL("House");
        assertEquals(1, result.size());
    }

    @Test
    public void testFindByCostGreaterThanJPQL() {
        List<Appointment> mock = List.of(new Appointment(3, LocalDate.now(), "C", true, 100f, 30, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(3, LocalDate.now(), "C", true, 100f, 30, 12, 102));

        when(appointmentRepository.findByCostGreaterThanJPQL(50f)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.findByCostGreaterThanJPQL(50f);
        assertEquals(1, result.size());
    }

    // ---------- Nativas (3 métodos) ----------

    @Test
    public void testFindByPatientEmailNative() {
        List<Appointment> mock = List.of(new Appointment(4, LocalDate.now(), "D", true, 15f, 12, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(4, LocalDate.now(), "D", true, 15f, 12, 13, 103));

        when(appointmentRepository.findByPatientEmailNative("p@e.com")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.findByPatientEmailNative("p@e.com");
        assertEquals(1, result.size());
    }

    @Test
    public void testFindByDoctorNameNative() {
        List<Appointment> mock = List.of(new Appointment(5, LocalDate.now(), "E", false, 0f, 10, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(5, LocalDate.now(), "E", false, 0f, 10, 14, 104));

        when(appointmentRepository.findByDoctorNameNative("House")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.findByDoctorNameNative("House");
        assertEquals(1, result.size());
    }

    @Test
    public void testFindByCostGreaterThanNative() {
        List<Appointment> mock = List.of(new Appointment(6, LocalDate.now(), "F", true, 200f, 45, null, null, null));
        List<AppointmentOutDto> out = List.of(new AppointmentOutDto(6, LocalDate.now(), "F", true, 200f, 45, 15, 105));

        when(appointmentRepository.findByCostGreaterThanNative(150f)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<AppointmentOutDto> result = appointmentService.findByCostGreaterThanNative(150f);
        assertEquals(1, result.size());
    }
}
