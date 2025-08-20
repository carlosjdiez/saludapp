package com.svalero.saludapp;

import com.svalero.saludapp.domain.Patient;
import com.svalero.saludapp.domain.dto.PatientInDto;
import com.svalero.saludapp.domain.dto.PatientOutDto;
import com.svalero.saludapp.domain.dto.PatientRegistrationDto;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.repository.PatientRepository;
import com.svalero.saludapp.service.PatientService;
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
public class PatientServiceTests {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------- getAll combinaciones ----------

    @Test
    public void testGetAll_NoFilters() {
        List<Patient> mock = List.of(
                new Patient(1, "Ana", "López", "ana@example.com", LocalDate.of(1990,1,1), true, 60.5f, null),
                new Patient(2, "Juan", "Pérez", "juan@example.com", LocalDate.of(1985,5,10), false, 80f, null)
        );
        List<PatientOutDto> out = List.of(
                new PatientOutDto(1, "Ana", "López", "ana@example.com", mock.get(0).getBirthDate(), true, 60.5f),
                new PatientOutDto(2, "Juan", "Pérez", "juan@example.com", mock.get(1).getBirthDate(), false, 80f)
        );

        when(patientRepository.findAll()).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<PatientOutDto> result = patientService.getAll("", "");

        assertEquals(2, result.size());
        verify(patientRepository, times(1)).findAll();
        verify(patientRepository, never()).findByName(anyString());
        verify(patientRepository, never()).findBySurname(anyString());
        verify(patientRepository, never()).findByNameAndSurname(anyString(), anyString());
    }

    @Test
    public void testGetAll_ByName() {
        List<Patient> mock = List.of(
                new Patient(3, "Ana", "López", "ana@example.com", LocalDate.of(1990,1,1), true, 60.5f, null)
        );
        List<PatientOutDto> out = List.of(
                new PatientOutDto(3, "Ana", "López", "ana@example.com", mock.get(0).getBirthDate(), true, 60.5f)
        );

        when(patientRepository.findByName("Ana")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<PatientOutDto> result = patientService.getAll("Ana", "");

        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findByName("Ana");
    }

    @Test
    public void testGetAll_BySurname() {
        List<Patient> mock = List.of(
                new Patient(4, "Luis", "García", "luis@example.com", LocalDate.of(1992,3,3), true, 70f, null)
        );
        List<PatientOutDto> out = List.of(
                new PatientOutDto(4, "Luis", "García", "luis@example.com", mock.get(0).getBirthDate(), true, 70f)
        );

        when(patientRepository.findBySurname("García")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<PatientOutDto> result = patientService.getAll("", "García");

        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findBySurname("García");
    }

    @Test
    public void testGetAll_ByNameAndSurname() {
        List<Patient> mock = List.of(
                new Patient(5, "Marta", "Ruiz", "marta@example.com", LocalDate.of(2000,7,7), false, 55f, null)
        );
        List<PatientOutDto> out = List.of(
                new PatientOutDto(5, "Marta", "Ruiz", "marta@example.com", mock.get(0).getBirthDate(), false, 55f)
        );

        when(patientRepository.findByNameAndSurname("Marta", "Ruiz")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<PatientOutDto> result = patientService.getAll("Marta", "Ruiz");

        assertEquals(1, result.size());
        verify(patientRepository, times(1)).findByNameAndSurname("Marta", "Ruiz");
    }

    // ---------- get(id) ----------

    @Test
    public void testGet_ReturnsPatient() throws PatientNotFoundException {
        Patient p = new Patient(1, "Ana", "López", "ana@example.com", LocalDate.of(1990,1,1), true, 60.5f, null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        Patient result = patientService.get(1L);

        assertEquals(1, result.getId());
    }

    @Test
    public void testGet_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> patientService.get(99L));
    }

    // ---------- add(dto) ----------

    @Test
    public void testAdd_Ok() {
        LocalDate birth = LocalDate.of(1995, 6, 15);
        PatientRegistrationDto in = new PatientRegistrationDto(
                "Ana", "López", "ana@example.com", birth, true, 60.5f
        );

        Patient mapped = new Patient(0, "Ana", "López", "ana@example.com", birth, true, 60.5f, null);
        Patient saved = new Patient(1, "Ana", "López", "ana@example.com", birth, true, 60.5f, null);
        PatientOutDto out = new PatientOutDto(1, "Ana", "López", "ana@example.com", birth, true, 60.5f);

        when(modelMapper.map(in, Patient.class)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(modelMapper.map(saved, PatientOutDto.class)).thenReturn(out);

        PatientOutDto result = patientService.add(in);

        assertEquals(1, result.getId());
        assertEquals("Ana", result.getName());
        verify(patientRepository, times(1)).save(mapped);
    }

    // ---------- modify(id, dto) ----------

    @Test
    public void testModify_Ok() throws PatientNotFoundException {
        long id = 1;
        PatientInDto in = new PatientInDto("Ana María", "López", "ana.maria@example.com",
                LocalDate.of(1995,6,15), true, 61.2f);
        Patient entity = new Patient(id, "Old", "Old", "old@example.com", LocalDate.of(1990,1,1), false, 50f, null);
        PatientOutDto out = new PatientOutDto(id, "Ana María", "López", "ana.maria@example.com",
                in.getBirthDate(), true, 61.2f);

        when(patientRepository.findById(id)).thenReturn(Optional.of(entity));

        // Simula el map(in, entity) mutando el entity
        doAnswer(inv -> {
            PatientInDto src = inv.getArgument(0);
            Patient dest = inv.getArgument(1);
            if (src.getName() != null) dest.setName(src.getName());
            if (src.getSurname() != null) dest.setSurname(src.getSurname());
            if (src.getEmail() != null) dest.setEmail(src.getEmail());
            if (src.getBirthDate() != null) dest.setBirthDate(src.getBirthDate());
            if (src.getActive() != null) dest.setActive(src.getActive());
            if (src.getWeightKg() != null) dest.setWeightKg(src.getWeightKg());
            return null;
        }).when(modelMapper).map(in, entity);

        when(modelMapper.map(entity, PatientOutDto.class)).thenReturn(out);

        PatientOutDto result = patientService.modify(id, in);

        assertEquals("Ana María", result.getName());
        assertEquals("ana.maria@example.com", result.getEmail());
        verify(patientRepository, times(1)).save(entity);
    }

    @Test
    public void testModify_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        PatientInDto in = new PatientInDto("X", "Y", "x@y.com", LocalDate.of(2000,1,1), false, 50f);
        assertThrows(PatientNotFoundException.class, () -> patientService.modify(99L, in));
    }

    // ---------- remove(id) ----------

    @Test
    public void testRemove_Ok() throws PatientNotFoundException {
        Patient p = new Patient(1, "Ana", "López", "ana@example.com", LocalDate.of(1990,1,1), true, 60.5f, null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(p));

        patientService.remove(1L);

        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRemove_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> patientService.remove(99L));
        verify(patientRepository, never()).deleteById(anyLong());
    }
}
