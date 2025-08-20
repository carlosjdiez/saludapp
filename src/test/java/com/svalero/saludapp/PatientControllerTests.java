package com.svalero.saludapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.saludapp.controller.PatientController;
import com.svalero.saludapp.domain.Patient;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.domain.dto.PatientInDto;
import com.svalero.saludapp.domain.dto.PatientOutDto;
import com.svalero.saludapp.domain.dto.PatientRegistrationDto;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.service.PatientService;
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

@WebMvcTest(PatientController.class)
public class PatientControllerTests {

    private static final String PATIENT_NOT_FOUND_MSG = "The patient does not exist";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    // ---------- GET /patients ----------

    @Test
    public void testGetAllWithoutParametersReturnOk() throws Exception {
        List<PatientOutDto> mockList = List.of(
                new PatientOutDto(1, "Ana", "López", "ana@example.com", LocalDate.of(1990,1,1), true, 60.5f),
                new PatientOutDto(2, "Juan", "Pérez", "juan@example.com", LocalDate.of(1985,5,10), false, 80f)
        );
        when(patientService.getAll("", "")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/patients")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PatientOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(2, list.size());
        assertEquals("Ana", list.getFirst().getName());
    }

    @Test
    public void testGetAllByNameReturnOk() throws Exception {
        List<PatientOutDto> mockList = List.of(
                new PatientOutDto(3, "Ana", "López", "ana@example.com", LocalDate.of(1990,1,1), true, 60.5f)
        );
        when(patientService.getAll("Ana", "")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/patients")
                                .queryParam("name", "Ana")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PatientOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, list.size());
        assertEquals("Ana", list.getFirst().getName());
    }

    @Test
    public void testGetAllBySurnameReturnOk() throws Exception {
        List<PatientOutDto> mockList = List.of(
                new PatientOutDto(4, "Luis", "García", "luis@example.com", LocalDate.of(1992,3,3), true, 70f)
        );
        when(patientService.getAll("", "García")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/patients")
                                .queryParam("surname", "García")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PatientOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, list.size());
        assertEquals("García", list.getFirst().getSurname());
    }

    @Test
    public void testGetAllByNameAndSurnameReturnOk() throws Exception {
        List<PatientOutDto> mockList = List.of(
                new PatientOutDto(5, "Marta", "Ruiz", "marta@example.com", LocalDate.of(2000,7,7), false, 55f)
        );
        when(patientService.getAll("Marta", "Ruiz")).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/patients")
                                .queryParam("name", "Marta")
                                .queryParam("surname", "Ruiz")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<PatientOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(1, list.size());
        assertEquals("Marta", list.getFirst().getName());
        assertEquals("Ruiz", list.getFirst().getSurname());
    }

    // ---------- GET /patients/{id} ----------

    @Test
    public void testGetPatientReturnOk() throws Exception {
        Patient mock = new Patient(1, "Ana", "López", "ana@example.com",
                LocalDate.of(1990,1,1), true, 60.5f, null);
        when(patientService.get(1)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/patients/{patientId}", "1")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        Patient result = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals("Ana", result.getName());
    }

    @Test
    public void testGetPatientNotFound() throws Exception {
        when(patientService.get(1)).thenThrow(new PatientNotFoundException(PATIENT_NOT_FOUND_MSG));

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/patients/{patientId}", "1"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(PATIENT_NOT_FOUND_MSG, error.getMessage());
    }

    // ---------- POST /patients ----------

    @Test
    public void testAddPatientCreated() throws Exception {
        LocalDate birth = LocalDate.of(1995, 6, 15);
        PatientRegistrationDto dto = new PatientRegistrationDto(
                "Ana", "López", "ana@example.com", birth, true, 60.5f
        );
        PatientOutDto out = new PatientOutDto(1, "Ana", "López", "ana@example.com", birth, true, 60.5f);

        when(patientService.add(dto)).thenReturn(out);

        String body = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/patients")
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Ana"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("ana@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(true));
    }

    @Test
    public void testAddPatientValidationError() throws Exception {
        // NotBlank en name/surname/email; Email inválido; birthDate y active obligatorios; weightKg negativo
        PatientRegistrationDto dto = new PatientRegistrationDto(
                "", "", "noemail", null, null, -1f
        );

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/patients")
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
        assertEquals("Formato de email inválido", error.getErrorMessages().get("email"));
        assertEquals("La fecha de nacimiento es obligatoria", error.getErrorMessages().get("birthDate"));
        assertEquals("El campo active es obligatorio", error.getErrorMessages().get("active"));
        assertEquals("El peso no puede ser negativo", error.getErrorMessages().get("weightKg"));
    }

    // ---------- PUT /patients/{id} ----------

    @Test
    public void testModifyPatientOk() throws Exception {
        long id = 1;
        PatientInDto in = new PatientInDto("Ana María", "López", "ana.maria@example.com",
                LocalDate.of(1995,6,15), true, 61.2f);
        PatientOutDto out = new PatientOutDto(id, "Ana María", "López", "ana.maria@example.com",
                in.getBirthDate(), true, 61.2f);

        when(patientService.modify(id, in)).thenReturn(out);

        String body = objectMapper.writeValueAsString(in);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/patients/{patientId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((int) id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Ana María"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("ana.maria@example.com"));
    }

    @Test
    public void testModifyPatientNotFound() throws Exception {
        long id = 99;
        PatientInDto in = new PatientInDto("X", "Y", "x@y.com", LocalDate.of(2000,1,1), false, 50f);

        when(patientService.modify(id, in)).thenThrow(new PatientNotFoundException(PATIENT_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/patients/{patientId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(404, error.getCode());
        assertEquals(PATIENT_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testModifyPatientValidationError() throws Exception {
        // Email inválido y weightKg negativo → @Email y @PositiveOrZero
        PatientInDto in = new PatientInDto("Ana", "López", "noemail",
                LocalDate.of(1995,6,15), true, -2f);

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/patients/{patientId}", 1)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>() {});
        assertEquals(400, error.getCode());
        assertEquals("Formato de email inválido", error.getErrorMessages().get("email"));
        assertEquals("El peso no puede ser negativo", error.getErrorMessages().get("weightKg"));
    }

    // ---------- DELETE /patients/{id} ----------

    @Test
    public void testDeleteNoContent() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/patients/{patientId}", 1))
                .andExpect(status().isNoContent());
    }

}
