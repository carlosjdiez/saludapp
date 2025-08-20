package com.svalero.saludapp;

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
import com.svalero.saludapp.service.PrescriptionService;
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
public class PrescriptionServiceTests {

    @InjectMocks
    private PrescriptionService prescriptionService;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------- getAll combinaciones ----------

    @Test
    public void testGetAll_NoFilters() {
        List<Prescription> mock = List.of(
                new Prescription(1, "A", true, 10, 15f, "1-0-1", null, null),
                new Prescription(2, "B", false, 0, 0f, "SOS", null, null)
        );
        List<PrescriptionOutDto> out = List.of(
                new PrescriptionOutDto(1, "A", true, 10, 15f, "1-0-1", 100, 200),
                new PrescriptionOutDto(2, "B", false, 0, 0f, "SOS", 101, 201)
        );

        when(prescriptionRepository.findAll()).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        var result = prescriptionService.getAll(null, null);

        assertEquals(2, result.size());
        verify(prescriptionRepository, times(1)).findAll();
        verify(prescriptionRepository, never()).findByActive(anyBoolean());
        verify(prescriptionRepository, never()).findByDurationDays(anyInt());
        verify(prescriptionRepository, never()).findByActiveAndDurationDays(anyBoolean(), anyInt());
    }

    @Test
    public void testGetAll_ByActive() {
        List<Prescription> mock = List.of(
                new Prescription(1, "A", true, 7, 10f, "1-0-1", null, null)
        );
        List<PrescriptionOutDto> out = List.of(
                new PrescriptionOutDto(1, "A", true, 7, 10f, "1-0-1", 100, 200)
        );

        when(prescriptionRepository.findByActive(true)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        var result = prescriptionService.getAll(true, null);

        assertEquals(1, result.size());
        verify(prescriptionRepository, times(1)).findByActive(true);
    }

    @Test
    public void testGetAll_ByDuration() {
        List<Prescription> mock = List.of(
                new Prescription(2, "B", false, 14, 20f, "2-0-2", null, null)
        );
        List<PrescriptionOutDto> out = List.of(
                new PrescriptionOutDto(2, "B", false, 14, 20f, "2-0-2", 101, 201)
        );

        when(prescriptionRepository.findByDurationDays(14)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        var result = prescriptionService.getAll(null, 14);

        assertEquals(1, result.size());
        verify(prescriptionRepository, times(1)).findByDurationDays(14);
    }

    @Test
    public void testGetAll_ByActiveAndDuration() {
        List<Prescription> mock = List.of(
                new Prescription(3, "C", true, 30, 50f, "1-1-1", null, null)
        );
        List<PrescriptionOutDto> out = List.of(
                new PrescriptionOutDto(3, "C", true, 30, 50f, "1-1-1", 102, 202)
        );

        when(prescriptionRepository.findByActiveAndDurationDays(true, 30)).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        var result = prescriptionService.getAll(true, 30);

        assertEquals(1, result.size());
        verify(prescriptionRepository, times(1)).findByActiveAndDurationDays(true, 30);
    }

    // ---------- get(id) ----------

    @Test
    public void testGet_ReturnsPrescription() throws PrescriptionNotFoundException {
        Prescription p = new Prescription(1, "A", true, 10, 15f, "1-0-1", null, null);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        Prescription result = prescriptionService.get(1L);

        assertEquals(1, result.getId());
    }

    @Test
    public void testGet_NotFound() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PrescriptionNotFoundException.class, () -> prescriptionService.get(99L));
    }

    // ---------- add(appointmentId, medicineId, dto) ----------

    @Test
    public void testAdd_Ok() throws AppointmentNotFoundException, MedicineNotFoundException {
        long appointmentId = 100, medicineId = 200;

        // Objetos mínimos de apoyo
        Appointment ap = new Appointment(appointmentId, LocalDate.now(), "R", true, 0f, 10, null, null, null);
        Medicine med = new Medicine(medicineId, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100, null);

        PrescriptionRegistrationDto in = new PrescriptionRegistrationDto("Notas", true, 10, 15f, "1-0-1");
        Prescription toSave = new Prescription(0, "Notas", true, 10, 15f, "1-0-1", ap, med);
        Prescription saved = new Prescription(1, "Notas", true, 10, 15f, "1-0-1", ap, med);
        PrescriptionOutDto out = new PrescriptionOutDto(1, "Notas", true, 10, 15f, "1-0-1", appointmentId, medicineId);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(ap));
        when(medicineRepository.findById(medicineId)).thenReturn(Optional.of(med));
        when(modelMapper.map(in, Prescription.class)).thenReturn(toSave);
        when(prescriptionRepository.save(toSave)).thenReturn(saved);
        when(modelMapper.map(saved, PrescriptionOutDto.class)).thenReturn(out);

        var result = prescriptionService.add(appointmentId, medicineId, in);

        assertEquals(1, result.getId());
        assertEquals(appointmentId, result.getAppointmentId());
        assertEquals(medicineId, result.getMedicineId());
        verify(prescriptionRepository, times(1)).save(toSave);
    }

    @Test
    public void testAdd_AppointmentNotFound() {
        long appointmentId = 999, medicineId = 200;
        PrescriptionRegistrationDto in = new PrescriptionRegistrationDto("Notas", true, 5, 5f, "X");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> prescriptionService.add(appointmentId, medicineId, in));
        verify(medicineRepository, never()).findById(anyLong());
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    public void testAdd_MedicineNotFound() {
        long appointmentId = 100, medicineId = 999;
        PrescriptionRegistrationDto in = new PrescriptionRegistrationDto("Notas", true, 5, 5f, "X");

        Appointment ap = new Appointment(appointmentId, LocalDate.now(), "R", true, 0f, 10, null, null, null);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(ap));
        when(medicineRepository.findById(medicineId)).thenReturn(Optional.empty());

        assertThrows(MedicineNotFoundException.class, () -> prescriptionService.add(appointmentId, medicineId, in));
        verify(prescriptionRepository, never()).save(any());
    }

    // ---------- modify(id, dto) ----------

    @Test
    public void testModify_Ok() throws PrescriptionNotFoundException {
        long id = 1;
        PrescriptionInDto in = new PrescriptionInDto("Act", true, 20, 30f, "1-1-1");
        Prescription entity = new Prescription(id, "Old", false, 5, 0f, "x", null, null);
        PrescriptionOutDto out = new PrescriptionOutDto(id, "Act", true, 20, 30f, "1-1-1", 100, 200);

        when(prescriptionRepository.findById(id)).thenReturn(Optional.of(entity));

        // Simulamos el map(in, entity) mutando el entity
        doAnswer(inv -> {
            PrescriptionInDto src = inv.getArgument(0);
            Prescription dest = inv.getArgument(1);
            if (src.getNotes() != null) dest.setNotes(src.getNotes());
            if (src.getActive() != null) dest.setActive(src.getActive());
            if (src.getDurationDays() != null) dest.setDurationDays(src.getDurationDays());
            if (src.getTotalCost() != null) dest.setTotalCost(src.getTotalCost());
            if (src.getDosageInstructions() != null) dest.setDosageInstructions(src.getDosageInstructions());
            return null;
        }).when(modelMapper).map(in, entity);

        when(modelMapper.map(entity, PrescriptionOutDto.class)).thenReturn(out);

        var result = prescriptionService.modify(id, in);

        assertEquals("Act", result.getNotes());
        assertEquals(20, result.getDurationDays());
        verify(prescriptionRepository, times(1)).save(entity);
    }

    @Test
    public void testModify_NotFound() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());
        PrescriptionInDto in = new PrescriptionInDto("X", false, 5, 10f, "Z");
        assertThrows(PrescriptionNotFoundException.class, () -> prescriptionService.modify(99L, in));
    }

    // ---------- remove(id) ----------

    @Test
    public void testRemove_Ok() throws PrescriptionNotFoundException {
        Prescription p = new Prescription(1, "A", true, 10, 15f, "1-0-1", null, null);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        prescriptionService.remove(1L);

        verify(prescriptionRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRemove_NotFound() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PrescriptionNotFoundException.class, () -> prescriptionService.remove(99L));
        verify(prescriptionRepository, never()).deleteById(anyLong());
    }
}
