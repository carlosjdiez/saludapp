package com.svalero.saludapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.saludapp.controller.DoctorController;
import com.svalero.saludapp.domain.Doctor;
import com.svalero.saludapp.domain.dto.DoctorInDto;
import com.svalero.saludapp.domain.dto.DoctorOutDto;
import com.svalero.saludapp.domain.dto.DoctorRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.service.DoctorService;
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

@WebMvcTest(DoctorController.class)
public class DoctorControllerTests {

    private static final String DOCTOR_NOT_FOUND_MSG = "The doctor does not exist";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    // ---------- GET /doctors ----------

    @Test
    public void testGetAllWithoutParametersReturnOk() throws Exception {
        List<DoctorOutDto> mockList = List.of(
                new DoctorOutDto(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true),
                new DoctorOutDto(2, "Meredith", "Grey", "LIC999", "Cirugía", LocalDate.now().minusYears(1), true)
        );
        when(doctorService.getAll("", "")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/doctors")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<DoctorOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Gregory", list.getFirst().getName());
    }

    @Test
    public void testGetAllByNameReturnOk() throws Exception {
        List<DoctorOutDto> mockList = List.of(
                new DoctorOutDto(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true)
        );
        when(doctorService.getAll("Gregory", "")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/doctors")
                                .queryParam("name", "Gregory")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<DoctorOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(1, list.size());
        assertEquals("Gregory", list.getFirst().getName());
    }

    @Test
    public void testGetAllBySpecialtyReturnOk() throws Exception {
        List<DoctorOutDto> mockList = List.of(
                new DoctorOutDto(2, "Meredith", "Grey", "LIC999", "Cirugía", LocalDate.now(), true)
        );
        when(doctorService.getAll("", "Cirugía")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/doctors")
                                .queryParam("specialty", "Cirugía")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<DoctorOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(1, list.size());
        assertEquals("Cirugía", list.getFirst().getSpecialty());
    }

    @Test
    public void testGetAllByNameAndSpecialtyReturnOk() throws Exception {
        List<DoctorOutDto> mockList = List.of(
                new DoctorOutDto(3, "James", "Wilson", "LIC777", "Oncología", LocalDate.now(), true)
        );
        when(doctorService.getAll("James", "Oncología")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/doctors")
                                .queryParam("name", "James")
                                .queryParam("specialty", "Oncología")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<DoctorOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(1, list.size());
        assertEquals("James", list.getFirst().getName());
        assertEquals("Oncología", list.getFirst().getSpecialty());
    }

    // ---------- GET /doctors/{id} ----------

    @Test
    public void testGetDoctorReturnOk() throws Exception {
        Doctor mock = new Doctor(1, "Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true, null);
        when(doctorService.get(1)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/doctors/{doctorId}", "1")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        Doctor result = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertNotNull(result);
        assertEquals("Gregory", result.getName());
    }

    @Test
    public void testGetDoctorNotFound() throws Exception {
        when(doctorService.get(1)).thenThrow(new DoctorNotFoundException(DOCTOR_NOT_FOUND_MSG));

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/doctors/{doctorId}", "1"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertNotNull(error);
        assertEquals(404, error.getCode());
        assertEquals(DOCTOR_NOT_FOUND_MSG, error.getMessage());
    }

    // ---------- POST /doctors ----------

    @Test
    public void testAddDoctorCreated() throws Exception {
        LocalDate hiring = LocalDate.now();

        DoctorRegistrationDto dto = new DoctorRegistrationDto("Gregory", "House", "LIC123", "Diagnóstico", hiring, true);
        DoctorOutDto out = new DoctorOutDto(1, "Gregory", "House", "LIC123", "Diagnóstico", hiring, true);

        when(doctorService.add(dto)).thenReturn(out);

        String body = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/doctors")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Gregory"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value("House"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.licenseNumber").value("LIC123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(true));
    }

    @Test
    public void testAddDoctorValidationError() throws Exception {
        // NotBlank en name, surname, licenseNumber
        DoctorRegistrationDto dto = new DoctorRegistrationDto(null, null, null, "Cardio", null, null);

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/doctors")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});

        assertEquals(400, error.getCode());
        assertEquals("Bad Request", error.getMessage());
        assertEquals("El campo name es obligatorio", error.getErrorMessages().get("name"));
        assertEquals("El campo surname es obligatorio", error.getErrorMessages().get("surname"));
        assertEquals("El número de licencia es obligatorio", error.getErrorMessages().get("licenseNumber"));
    }

    // ---------- PUT /doctors/{id} ----------

    @Test
    public void testModifyDoctorOk() throws Exception {
        long id = 1;
        DoctorInDto in = new DoctorInDto("Gregory", "House", "LIC123", "Diagnóstico", LocalDate.now(), true);
        DoctorOutDto out = new DoctorOutDto(id, "Gregory", "House", "LIC123", "Diagnóstico", in.getHiringDate(), true);

        when(doctorService.modify(id, in)).thenReturn(out);

        String body = objectMapper.writeValueAsString(in);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/doctors/{doctorId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((int) id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Gregory"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.specialty").value("Diagnóstico"));
    }

    @Test
    public void testModifyDoctorNotFound() throws Exception {
        long id = 99;
        DoctorInDto in = new DoctorInDto("x", "y", "L", "Z", LocalDate.now(), false);

        when(doctorService.modify(id, in)).thenThrow(new DoctorNotFoundException(DOCTOR_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/doctors/{doctorId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(DOCTOR_NOT_FOUND_MSG, error.getMessage());
    }

    // (No test 400 en PUT porque DoctorInDto no tiene anotaciones de validación)

    // ---------- DELETE /doctors/{id} ----------

    @Test
    public void testDeleteNoContent() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/doctors/{doctorId}", 1))
                .andExpect(status().isNoContent());
    }

}
