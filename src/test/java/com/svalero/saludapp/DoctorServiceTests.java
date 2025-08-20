package com.svalero.saludapp;

import com.svalero.saludapp.domain.Doctor;
import com.svalero.saludapp.domain.dto.DoctorInDto;
import com.svalero.saludapp.domain.dto.DoctorOutDto;
import com.svalero.saludapp.domain.dto.DoctorRegistrationDto;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.repository.DoctorRepository;
import com.svalero.saludapp.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class DoctorServiceTests {

    @InjectMocks
    private DoctorService doctorService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------- getAll combinaciones ----------

    @Test
    public void testGetAll_NoFilters() {
        List<Doctor> mock = List.of(
                new Doctor(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true, null),
                new Doctor(2, "Meredith", "Grey", "LIC999", "Cirugía", LocalDate.now(), true, null)
        );
        List<DoctorOutDto> out = List.of(
                new DoctorOutDto(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true),
                new DoctorOutDto(2, "Meredith", "Grey", "LIC999", "Cirugía", LocalDate.now(), true)
        );

        when(doctorRepository.findAll()).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<DoctorOutDto> result = doctorService.getAll("", "");

        assertEquals(2, result.size());
        verify(doctorRepository, times(1)).findAll();
        verify(doctorRepository, never()).findByName(anyString());
        verify(doctorRepository, never()).findBySpecialty(anyString());
        verify(doctorRepository, never()).findByNameAndSpecialty(anyString(), anyString());
    }

    @Test
    public void testGetAll_ByName() {
        List<Doctor> mock = List.of(
                new Doctor(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true, null)
        );
        List<DoctorOutDto> out = List.of(
                new DoctorOutDto(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true)
        );

        when(doctorRepository.findByName("Gregory")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<DoctorOutDto> result = doctorService.getAll("Gregory", "");

        assertEquals(1, result.size());
        verify(doctorRepository, times(1)).findByName("Gregory");
    }

    @Test
    public void testGetAll_BySpecialty() {
        List<Doctor> mock = List.of(
                new Doctor(2, "Meredith", "Grey", "LIC999", "Cirugía", LocalDate.now(), true, null)
        );
        List<DoctorOutDto> out = List.of(
                new DoctorOutDto(2, "Meredith", "Grey", "LIC999", "Cirugía", LocalDate.now(), true)
        );

        when(doctorRepository.findBySpecialty("Cirugía")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<DoctorOutDto> result = doctorService.getAll("", "Cirugía");

        assertEquals(1, result.size());
        verify(doctorRepository, times(1)).findBySpecialty("Cirugía");
    }

    @Test
    public void testGetAll_ByNameAndSpecialty() {
        List<Doctor> mock = List.of(
                new Doctor(3, "James", "Wilson", "LIC777", "Oncología", LocalDate.now(), true, null)
        );
        List<DoctorOutDto> out = List.of(
                new DoctorOutDto(3, "James", "Wilson", "LIC777", "Oncología", LocalDate.now(), true)
        );

        when(doctorRepository.findByNameAndSpecialty("James", "Oncología")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<DoctorOutDto> result = doctorService.getAll("James", "Oncología");

        assertEquals(1, result.size());
        verify(doctorRepository, times(1)).findByNameAndSpecialty("James", "Oncología");
    }

    // ---------- get(id) ----------

    @Test
    public void testGet_ReturnsDoctor() throws DoctorNotFoundException {
        Doctor d = new Doctor(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true, null);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(d));

        Doctor result = doctorService.get(1L);

        assertEquals(1, result.getId());
    }

    @Test
    public void testGet_NotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DoctorNotFoundException.class, () -> doctorService.get(99L));
    }

    // ---------- add(dto) ----------

    @Test
    public void testAdd_Ok_SetsHiringDateWhenNull() {
        DoctorRegistrationDto in = new DoctorRegistrationDto("Gregory", "House", "LIC123", "Diagnóstico", null, true);

        Doctor mapped = new Doctor(0, "Gregory", "House", "LIC123", "Diagnóstico", null, true, null);
        Doctor saved = new Doctor(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true, null);
        DoctorOutDto out = new DoctorOutDto(1, "Gregory", "House", "LIC123", "Diagnóstico", saved.getHiringDate(), true);

        when(modelMapper.map(in, Doctor.class)).thenReturn(mapped);

        // capturar el argumento que se guarda para comprobar que se setea hiringDate
        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(saved);
        when(modelMapper.map(saved, DoctorOutDto.class)).thenReturn(out);

        DoctorOutDto result = doctorService.add(in);

        verify(doctorRepository).save(captor.capture());
        Doctor toPersist = captor.getValue();
        assertNotNull(toPersist.getHiringDate(), "El hiringDate debe establecerse a hoy si viene null");
        assertEquals(1, result.getId());
        assertEquals("Gregory", result.getName());
    }

    @Test
    public void testAdd_Ok_RespectsHiringDateIfProvided() {
        LocalDate hiring = LocalDate.of(2020, 5, 20);
        DoctorRegistrationDto in = new DoctorRegistrationDto("Meredith", "Grey", "LIC999", "Cirugía", hiring, true);

        Doctor mapped = new Doctor(0, "Meredith", "Grey", "LIC999", "Cirugía", hiring, true, null);
        Doctor saved = new Doctor(2, "Meredith", "Grey", "LIC999", "Cirugía", hiring, true, null);
        DoctorOutDto out = new DoctorOutDto(2, "Meredith", "Grey", "LIC999", "Cirugía", hiring, true);

        when(modelMapper.map(in, Doctor.class)).thenReturn(mapped);
        when(doctorRepository.save(mapped)).thenReturn(saved);
        when(modelMapper.map(saved, DoctorOutDto.class)).thenReturn(out);

        DoctorOutDto result = doctorService.add(in);

        assertEquals(hiring, result.getHiringDate());
        verify(doctorRepository, times(1)).save(mapped);
    }

    // ---------- modify(id, dto) ----------

    @Test
    public void testModify_Ok() throws DoctorNotFoundException {
        long id = 1;
        DoctorInDto in = new DoctorInDto("Greg", "House", "LIC123", "Diagnóstico", LocalDate.now(), true);
        Doctor entity = new Doctor(id, "Viejo", "Apellido", "L", "X", LocalDate.now().minusYears(5), false, null);
        DoctorOutDto out = new DoctorOutDto(id, "Greg", "House", "LIC123", "Diagnóstico", in.getHiringDate(), true);

        when(doctorRepository.findById(id)).thenReturn(Optional.of(entity));

        // Simulamos el map(in, entity) mutando el entity
        doAnswer(inv -> {
            DoctorInDto src = inv.getArgument(0);
            Doctor dest = inv.getArgument(1);
            dest.setName(src.getName());
            dest.setSurname(src.getSurname());
            dest.setLicenseNumber(src.getLicenseNumber());
            dest.setSpecialty(src.getSpecialty());
            dest.setHiringDate(src.getHiringDate());
            dest.setActive(Boolean.TRUE.equals(src.getActive()));
            return null;
        }).when(modelMapper).map(in, entity);

        when(modelMapper.map(entity, DoctorOutDto.class)).thenReturn(out);

        DoctorOutDto result = doctorService.modify(id, in);

        assertEquals("Greg", result.getName());
        assertEquals("Diagnóstico", result.getSpecialty());
        verify(doctorRepository, times(1)).save(entity);
    }

    @Test
    public void testModify_NotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        DoctorInDto in = new DoctorInDto("x", "y", "L", "Z", LocalDate.now(), false);
        assertThrows(DoctorNotFoundException.class, () -> doctorService.modify(99L, in));
    }

    // ---------- remove(id) ----------

    @Test
    public void testRemove_Ok() throws DoctorNotFoundException {
        Doctor d = new Doctor(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true, null);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(d));

        doctorService.remove(1L);

        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRemove_NotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DoctorNotFoundException.class, () -> doctorService.remove(99L));
        verify(doctorRepository, never()).deleteById(anyLong());
    }
}
