package com.svalero.saludapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.saludapp.controller.PrescriptionController;
import com.svalero.saludapp.domain.Prescription;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.domain.dto.PrescriptionInDto;
import com.svalero.saludapp.domain.dto.PrescriptionOutDto;
import com.svalero.saludapp.domain.dto.PrescriptionRegistrationDto;
import com.svalero.saludapp.exception.AppointmentNotFoundException;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.exception.PrescriptionNotFoundException;
import com.svalero.saludapp.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrescriptionController.class)
public class PrescriptionControllerTests {

    private static final String PRESCRIPTION_NOT_FOUND_MSG = "The prescription does not exist";
    private static final String APPOINTMENT_NOT_FOUND_MSG = "The appointment does not exist";
    private static final String MEDICINE_NOT_FOUND_MSG = "The medicine does not exist";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    // ---------- GET /prescriptions ----------

    @Test
    public void testGetAllWithoutParametersReturnOk() throws Exception {
        List<PrescriptionOutDto> mock = List.of(
                new PrescriptionOutDto(1, "Notas A", true, 10, 15f, "1-0-1", 100, 200),
                new PrescriptionOutDto(2, "Notas B", false, 0, 0f, "SOS", 101, 201)
        );
        when(prescriptionService.getAll(null, null)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/prescriptions")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PrescriptionOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(2, list.size());
        assertEquals("Notas A", list.getFirst().getNotes());
    }

    @Test
    public void testGetAllByActiveReturnOk() throws Exception {
        List<PrescriptionOutDto> mock = List.of(
                new PrescriptionOutDto(1, "Notas A", true, 7, 10f, "1-0-1", 100, 200)
        );
        when(prescriptionService.getAll(true, null)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/prescriptions")
                                .queryParam("active", "true")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PrescriptionOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, list.size());
        assertTrue(list.getFirst().isActive());
    }

    @Test
    public void testGetAllByDurationReturnOk() throws Exception {
        List<PrescriptionOutDto> mock = List.of(
                new PrescriptionOutDto(2, "Notas B", false, 14, 20f, "2-0-2", 101, 201)
        );
        when(prescriptionService.getAll(null, 14)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/prescriptions")
                                .queryParam("durationDays", "14")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PrescriptionOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, list.size());
        assertEquals(14, list.getFirst().getDurationDays());
    }

    @Test
    public void testGetAllByActiveAndDurationReturnOk() throws Exception {
        List<PrescriptionOutDto> mock = List.of(
                new PrescriptionOutDto(3, "Notas C", true, 30, 50f, "1-1-1", 102, 202)
        );
        when(prescriptionService.getAll(true, 30)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/prescriptions")
                                .queryParam("active", "true")
                                .queryParam("durationDays", "30")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PrescriptionOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, list.size());
        assertTrue(list.getFirst().isActive());
        assertEquals(30, list.getFirst().getDurationDays());
    }

    // ---------- GET /prescriptions/{id} ----------

    @Test
    public void testGetPrescriptionReturnOk() throws Exception {
        Prescription mock = new Prescription(1, "Notas A", true, 10, 15f, "1-0-1", null, null);
        when(prescriptionService.get(1)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/prescriptions/{prescriptionId}", "1")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        Prescription result = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals("Notas A", result.getNotes());
    }

    @Test
    public void testGetPrescriptionNotFound() throws Exception {
        when(prescriptionService.get(1)).thenThrow(new PrescriptionNotFoundException(PRESCRIPTION_NOT_FOUND_MSG));

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/prescriptions/{prescriptionId}", "1"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(PRESCRIPTION_NOT_FOUND_MSG, error.getMessage());
    }

    // ---------- POST /appointments/{appointmentId}/medicines/{medicineId}/prescriptions ----------

    @Test
    public void testAddPrescriptionCreated() throws Exception {
        long appointmentId = 100, medicineId = 200;
        PrescriptionRegistrationDto dto = new PrescriptionRegistrationDto("Notas A", true, 10, 15f, "1-0-1");
        PrescriptionOutDto out = new PrescriptionOutDto(1, "Notas A", true, 10, 15f, "1-0-1", appointmentId, medicineId);

        when(prescriptionService.add(appointmentId, medicineId, dto)).thenReturn(out);

        String body = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/appointments/{appointmentId}/medicines/{medicineId}/prescriptions", appointmentId, medicineId)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.notes").value("Notas A"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value((int) appointmentId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.medicineId").value((int) medicineId));
    }

    @Test
    public void testAddPrescriptionAppointmentNotFound() throws Exception {
        long appointmentId = 999, medicineId = 200;
        PrescriptionRegistrationDto dto = new PrescriptionRegistrationDto("Notas", true, 5, 5f, "X");

        when(prescriptionService.add(appointmentId, medicineId, dto))
                .thenThrow(new AppointmentNotFoundException(APPOINTMENT_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/appointments/{appointmentId}/medicines/{medicineId}/prescriptions", appointmentId, medicineId)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(APPOINTMENT_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testAddPrescriptionMedicineNotFound() throws Exception {
        long appointmentId = 100, medicineId = 999;
        PrescriptionRegistrationDto dto = new PrescriptionRegistrationDto("Notas", true, 5, 5f, "X");

        when(prescriptionService.add(appointmentId, medicineId, dto))
                .thenThrow(new MedicineNotFoundException(MEDICINE_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/appointments/{appointmentId}/medicines/{medicineId}/prescriptions", appointmentId, medicineId)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(MEDICINE_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testAddPrescriptionValidationError() throws Exception {
        // notes en blanco (NotBlank), durationDays y totalCost negativos (Min)
        PrescriptionRegistrationDto dto = new PrescriptionRegistrationDto(
                "", null, -1, -5f, "X"
        );

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/appointments/{appointmentId}/medicines/{medicineId}/prescriptions", 1, 1)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(400, error.getCode());
        assertEquals("Bad Request", error.getMessage());
        assertEquals("Las notas son obligatorias", error.getErrorMessages().get("notes"));
        assertEquals("La duración no puede ser negativa", error.getErrorMessages().get("durationDays"));
        assertEquals("El coste total no puede ser negativo", error.getErrorMessages().get("totalCost"));
    }

    // ---------- PUT /prescriptions/{id} ----------

    @Test
    public void testModifyPrescriptionOk() throws Exception {
        long id = 1;
        PrescriptionInDto in = new PrescriptionInDto("Actualizado", true, 20, 30f, "1-1-1");
        PrescriptionOutDto out = new PrescriptionOutDto(id, "Actualizado", true, 20, 30f, "1-1-1", 100, 200);

        when(prescriptionService.modify(id, in)).thenReturn(out);

        String body = objectMapper.writeValueAsString(in);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/prescriptions/{prescriptionId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((int) id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.durationDays").value(20))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCost").value(30.0));
    }

    @Test
    public void testModifyPrescriptionNotFound() throws Exception {
        long id = 99;
        PrescriptionInDto in = new PrescriptionInDto("X", false, 5, 10f, "Z");

        when(prescriptionService.modify(id, in)).thenThrow(new PrescriptionNotFoundException(PRESCRIPTION_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/prescriptions/{prescriptionId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(PRESCRIPTION_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testModifyPrescriptionValidationError() throws Exception {
        // durationDays y totalCost negativos
        PrescriptionInDto in = new PrescriptionInDto("X", true, -1, -5f, "Z");

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/prescriptions/{prescriptionId}", 1)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(400, error.getCode());
        assertEquals("La duración no puede ser negativa", error.getErrorMessages().get("durationDays"));
        assertEquals("El coste total no puede ser negativo", error.getErrorMessages().get("totalCost"));
    }

    // ---------- DELETE /prescriptions/{id} ----------

    @Test
    public void testDeleteNoContent() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/prescriptions/{prescriptionId}", 1))
                .andExpect(status().isNoContent());
    }

}
