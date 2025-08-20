package com.svalero.saludapp;

import com.svalero.saludapp.domain.Medicine;
import com.svalero.saludapp.domain.dto.MedicineInDto;
import com.svalero.saludapp.domain.dto.MedicineOutDto;
import com.svalero.saludapp.domain.dto.MedicineRegistrationDto;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.repository.MedicineRepository;
import com.svalero.saludapp.service.MedicineService;
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
public class MedicineServiceTests {

    @InjectMocks
    private MedicineService medicineService;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------- getAll combinaciones ----------

    @Test
    public void testGetAll_NoFilters() {
        List<Medicine> mock = List.of(
                new Medicine(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100, null),
                new Medicine(2, "Paracetamol", "FarmCo", 2.2f, false, LocalDate.now().plusMonths(6), 50, null)
        );
        List<MedicineOutDto> out = List.of(
                new MedicineOutDto(1, "Ibuprofeno", "ACME", 3.5f, false, mock.get(0).getExpiryDate(), 100),
                new MedicineOutDto(2, "Paracetamol", "FarmCo", 2.2f, false, mock.get(1).getExpiryDate(), 50)
        );

        when(medicineRepository.findAll()).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<MedicineOutDto> result = medicineService.getAll("", "");

        assertEquals(2, result.size());
        verify(medicineRepository, times(1)).findAll();
        verify(medicineRepository, never()).findByName(anyString());
        verify(medicineRepository, never()).findByManufacturer(anyString());
        verify(medicineRepository, never()).findByNameAndManufacturer(anyString(), anyString());
    }

    @Test
    public void testGetAll_ByName() {
        List<Medicine> mock = List.of(
                new Medicine(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100, null)
        );
        List<MedicineOutDto> out = List.of(
                new MedicineOutDto(1, "Ibuprofeno", "ACME", 3.5f, false, mock.get(0).getExpiryDate(), 100)
        );

        when(medicineRepository.findByName("Ibuprofeno")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<MedicineOutDto> result = medicineService.getAll("Ibuprofeno", "");

        assertEquals(1, result.size());
        verify(medicineRepository, times(1)).findByName("Ibuprofeno");
    }

    @Test
    public void testGetAll_ByManufacturer() {
        List<Medicine> mock = List.of(
                new Medicine(2, "Paracetamol", "FarmCo", 2.2f, false, LocalDate.now().plusMonths(6), 50, null)
        );
        List<MedicineOutDto> out = List.of(
                new MedicineOutDto(2, "Paracetamol", "FarmCo", 2.2f, false, mock.get(0).getExpiryDate(), 50)
        );

        when(medicineRepository.findByManufacturer("FarmCo")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<MedicineOutDto> result = medicineService.getAll("", "FarmCo");

        assertEquals(1, result.size());
        verify(medicineRepository, times(1)).findByManufacturer("FarmCo");
    }

    @Test
    public void testGetAll_ByNameAndManufacturer() {
        List<Medicine> mock = List.of(
                new Medicine(3, "Amoxicilina", "PharmaX", 5.0f, true, LocalDate.now().plusYears(2), 30, null)
        );
        List<MedicineOutDto> out = List.of(
                new MedicineOutDto(3, "Amoxicilina", "PharmaX", 5.0f, true, mock.get(0).getExpiryDate(), 30)
        );

        when(medicineRepository.findByNameAndManufacturer("Amoxicilina", "PharmaX")).thenReturn(mock);
        when(modelMapper.map(eq(mock), any(Type.class))).thenReturn(out);

        List<MedicineOutDto> result = medicineService.getAll("Amoxicilina", "PharmaX");

        assertEquals(1, result.size());
        verify(medicineRepository, times(1)).findByNameAndManufacturer("Amoxicilina", "PharmaX");
    }

    // ---------- get(id) ----------

    @Test
    public void testGet_ReturnsMedicine() throws MedicineNotFoundException {
        Medicine m = new Medicine(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100, null);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));

        Medicine result = medicineService.get(1L);

        assertEquals(1, result.getId());
    }

    @Test
    public void testGet_NotFound() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MedicineNotFoundException.class, () -> medicineService.get(99L));
    }

    // ---------- add(dto) ----------

    @Test
    public void testAdd_Ok() {
        LocalDate expiry = LocalDate.now().plusYears(1);
        MedicineRegistrationDto in = new MedicineRegistrationDto("Ibuprofeno", "ACME", 3.5f, false, expiry, 100);

        Medicine mapped = new Medicine(0, "Ibuprofeno", "ACME", 3.5f, false, expiry, 100, null);
        Medicine saved = new Medicine(1, "Ibuprofeno", "ACME", 3.5f, false, expiry, 100, null);
        MedicineOutDto out = new MedicineOutDto(1, "Ibuprofeno", "ACME", 3.5f, false, expiry, 100);

        when(modelMapper.map(in, Medicine.class)).thenReturn(mapped);
        when(medicineRepository.save(mapped)).thenReturn(saved);
        when(modelMapper.map(saved, MedicineOutDto.class)).thenReturn(out);

        MedicineOutDto result = medicineService.add(in);

        assertEquals(1, result.getId());
        assertEquals("Ibuprofeno", result.getName());
        verify(medicineRepository, times(1)).save(mapped);
    }

    // ---------- modify(id, dto) ----------

    @Test
    public void testModify_Ok() throws MedicineNotFoundException {
        long id = 1;
        MedicineInDto in = new MedicineInDto("Ibuprofeno", "ACME", 4.0f, false, LocalDate.now().plusYears(2), 120);
        Medicine entity = new Medicine(id, "Old", "OldCo", 1.0f, true, LocalDate.now(), 1, null);
        MedicineOutDto out = new MedicineOutDto(id, "Ibuprofeno", "ACME", 4.0f, false, in.getExpiryDate(), 120);

        when(medicineRepository.findById(id)).thenReturn(Optional.of(entity));

        // Simula el map(in, entity) mutando entity
        doAnswer(inv -> {
            MedicineInDto src = inv.getArgument(0);
            Medicine dest = inv.getArgument(1);
            if (src.getName() != null) dest.setName(src.getName());
            if (src.getManufacturer() != null) dest.setManufacturer(src.getManufacturer());
            if (src.getPrice() != null) dest.setPrice(src.getPrice());
            if (src.getPrescriptionRequired() != null) dest.setPrescriptionRequired(src.getPrescriptionRequired());
            if (src.getExpiryDate() != null) dest.setExpiryDate(src.getExpiryDate());
            if (src.getStock() != null) dest.setStock(src.getStock());
            return null;
        }).when(modelMapper).map(in, entity);

        when(modelMapper.map(entity, MedicineOutDto.class)).thenReturn(out);

        MedicineOutDto result = medicineService.modify(id, in);

        assertEquals(4.0f, result.getPrice(), 0.001);
        assertEquals(120, result.getStock());
        verify(medicineRepository, times(1)).save(entity);
    }

    @Test
    public void testModify_NotFound() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());
        MedicineInDto in = new MedicineInDto("X", "Y", 1.0f, false, LocalDate.now(), 10);
        assertThrows(MedicineNotFoundException.class, () -> medicineService.modify(99L, in));
    }

    // ---------- remove(id) ----------

    @Test
    public void testRemove_Ok() throws MedicineNotFoundException {
        Medicine m = new Medicine(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now(), 10, null);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));

        medicineService.remove(1L);

        verify(medicineRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRemove_NotFound() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MedicineNotFoundException.class, () -> medicineService.remove(99L));
        verify(medicineRepository, never()).deleteById(anyLong());
    }
}
