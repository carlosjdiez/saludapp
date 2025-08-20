package com.svalero.saludapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.saludapp.controller.MedicineController;
import com.svalero.saludapp.domain.Medicine;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.domain.dto.MedicineInDto;
import com.svalero.saludapp.domain.dto.MedicineOutDto;
import com.svalero.saludapp.domain.dto.MedicineRegistrationDto;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicineController.class)
public class MedicineControllerTests {

    private static final String MEDICINE_NOT_FOUND_MSG = "The medicine does not exist";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicineService medicineService;

    // ---------- GET /medicines ----------

    @Test
    public void testGetAllWithoutParametersReturnOk() throws Exception {
        List<MedicineOutDto> mockList = List.of(
                new MedicineOutDto(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100),
                new MedicineOutDto(2, "Paracetamol", "FarmCo", 2.2f, false, LocalDate.now().plusMonths(6), 50)
        );
        when(medicineService.getAll("", "")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/medicines")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<MedicineOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(2, list.size());
        assertEquals("Ibuprofeno", list.getFirst().getName());
    }

    @Test
    public void testGetAllByNameReturnOk() throws Exception {
        List<MedicineOutDto> mockList = List.of(
                new MedicineOutDto(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100)
        );
        when(medicineService.getAll("Ibuprofeno", "")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/medicines")
                                .queryParam("name", "Ibuprofeno")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<MedicineOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(1, list.size());
        assertEquals("Ibuprofeno", list.getFirst().getName());
    }

    @Test
    public void testGetAllByManufacturerReturnOk() throws Exception {
        List<MedicineOutDto> mockList = List.of(
                new MedicineOutDto(2, "Paracetamol", "FarmCo", 2.2f, false, LocalDate.now().plusMonths(6), 50)
        );
        when(medicineService.getAll("", "FarmCo")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/medicines")
                                .queryParam("manufacturer", "FarmCo")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<MedicineOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(1, list.size());
        assertEquals("FarmCo", list.getFirst().getManufacturer());
    }

    @Test
    public void testGetAllByNameAndManufacturerReturnOk() throws Exception {
        List<MedicineOutDto> mockList = List.of(
                new MedicineOutDto(3, "Amoxicilina", "PharmaX", 5.0f, true, LocalDate.now().plusYears(2), 30)
        );
        when(medicineService.getAll("Amoxicilina", "PharmaX")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/medicines")
                                .queryParam("name", "Amoxicilina")
                                .queryParam("manufacturer", "PharmaX")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<MedicineOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(1, list.size());
        assertEquals("Amoxicilina", list.getFirst().getName());
        assertEquals("PharmaX", list.getFirst().getManufacturer());
    }

    // ---------- GET /medicines/{id} ----------

    @Test
    public void testGetMedicineReturnOk() throws Exception {
        Medicine mock = new Medicine(1, "Ibuprofeno", "ACME", 3.5f, false, LocalDate.now().plusYears(1), 100, null);
        when(medicineService.get(1)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/medicines/{medicineId}", "1")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        Medicine result = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals("Ibuprofeno", result.getName());
    }

    @Test
    public void testGetMedicineNotFound() throws Exception {
        when(medicineService.get(1)).thenThrow(new MedicineNotFoundException(MEDICINE_NOT_FOUND_MSG));

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/medicines/{medicineId}", "1"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(404, error.getCode());
        assertEquals(MEDICINE_NOT_FOUND_MSG, error.getMessage());
    }

    // ---------- POST /medicines ----------

    @Test
    public void testAddMedicineCreated() throws Exception {
        LocalDate expiry = LocalDate.now().plusYears(1);
        MedicineRegistrationDto dto = new MedicineRegistrationDto("Ibuprofeno", "ACME", 3.5f, false, expiry, 100);
        MedicineOutDto out = new MedicineOutDto(1, "Ibuprofeno", "ACME", 3.5f, false, expiry, 100);

        when(medicineService.add(dto)).thenReturn(out);

        String body = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/medicines")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Ibuprofeno"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.manufacturer").value("ACME"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(3.5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stock").value(100));
    }

    @Test
    public void testAddMedicineValidationError() throws Exception {
        // name vacío (NotBlank), price null (NotNull), stock negativo (Min)
        MedicineRegistrationDto dto = new MedicineRegistrationDto("", "ACME", null, false, null, -1);

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/medicines")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(400, error.getCode());
        assertEquals("Bad Request", error.getMessage());
        assertEquals("El nombre es obligatorio", error.getErrorMessages().get("name"));
        assertEquals("El precio es obligatorio", error.getErrorMessages().get("price"));
        assertEquals("El stock no puede ser negativo", error.getErrorMessages().get("stock"));
    }

    // ---------- PUT /medicines/{id} ----------

    @Test
    public void testModifyMedicineOk() throws Exception {
        long id = 1;
        MedicineInDto in = new MedicineInDto("Ibuprofeno", "ACME", 4.0f, false, LocalDate.now().plusYears(2), 120);
        MedicineOutDto out = new MedicineOutDto(id, "Ibuprofeno", "ACME", 4.0f, false, in.getExpiryDate(), 120);

        when(medicineService.modify(id, in)).thenReturn(out);

        String body = objectMapper.writeValueAsString(in);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/medicines/{medicineId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((int) id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(4.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stock").value(120));
    }

    @Test
    public void testModifyMedicineNotFound() throws Exception {
        long id = 99;
        MedicineInDto in = new MedicineInDto("X", "Y", 1.0f, false, LocalDate.now(), 10);

        when(medicineService.modify(id, in)).thenThrow(new MedicineNotFoundException(MEDICINE_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/medicines/{medicineId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(404, error.getCode());
        assertEquals(MEDICINE_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testModifyMedicineValidationError() throws Exception {
        // Violamos @Min en price y stock
        MedicineInDto in = new MedicineInDto("Ibuprofeno", "ACME", -1f, false, LocalDate.now(), -5);

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/medicines/{medicineId}", 1)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(400, error.getCode());
        assertEquals("El precio no puede ser negativo", error.getErrorMessages().get("price"));
        assertEquals("El stock no puede ser negativo", error.getErrorMessages().get("stock"));
    }

    // ---------- DELETE /medicines/{id} ----------

    @Test
    public void testDeleteNoContent() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/medicines/{medicineId}", 1))
                .andExpect(status().isNoContent());
    }
}
