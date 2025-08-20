package com.svalero.saludapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.saludapp.controller.AppointmentController;
import com.svalero.saludapp.domain.Appointment;
import com.svalero.saludapp.domain.dto.AppointmentInDto;
import com.svalero.saludapp.domain.dto.AppointmentOutDto;
import com.svalero.saludapp.domain.dto.AppointmentRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.AppointmentNotFoundException;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.service.AppointmentService;
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

@WebMvcTest(AppointmentController.class)
public class AppointmentControllerTests {

    private static final String APPOINTMENT_NOT_FOUND_MSG = "The appointment does not exist";
    private static final String PATIENT_NOT_FOUND_MSG = "The patient does not exist";
    private static final String DOCTOR_NOT_FOUND_MSG = "The doctor does not exist";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    // ---------- GET /appointments ----------

    @Test
    public void testGetAllWithoutParametersReturnOk() throws Exception {
        List<AppointmentOutDto> mockList = List.of(
                new AppointmentOutDto(1, LocalDate.now(), "Dolor cabeza", true, 50f, 30, 10, 100),
                new AppointmentOutDto(2, LocalDate.now().plusDays(1), "Revisión", false, 0f, 15, 11, 101)
        );
        when(appointmentService.getAll(null, null)).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<AppointmentOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("Dolor cabeza", list.getFirst().getReason());
    }

    @Test
    public void testGetAllByDateReturnOk() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 10);
        List<AppointmentOutDto> mockList = List.of(
                new AppointmentOutDto(1, date, "Consulta", true, 30f, 20, 10, 100)
        );
        when(appointmentService.getAll(date, null)).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments")
                                .queryParam("date", date.toString())
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<AppointmentOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(1, list.size());
        assertEquals(date, list.getFirst().getDate());
    }

    @Test
    public void testGetAllByConfirmedReturnOk() throws Exception {
        List<AppointmentOutDto> mockList = List.of(
                new AppointmentOutDto(1, LocalDate.now(), "Urgente", true, 70f, 40, 10, 100)
        );
        when(appointmentService.getAll(null, true)).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments")
                                .queryParam("confirmed", "true")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<AppointmentOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(1, list.size());
        assertTrue(list.getFirst().isConfirmed());
    }

    @Test
    public void testGetAllByDateAndConfirmedReturnOk() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 10);
        List<AppointmentOutDto> mockList = List.of(
                new AppointmentOutDto(1, date, "Consulta", false, 20f, 10, 10, 100)
        );
        when(appointmentService.getAll(date, false)).thenReturn(mockList);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments")
                                .queryParam("date", date.toString())
                                .queryParam("confirmed", "false")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        List<AppointmentOutDto> list = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertEquals(1, list.size());
        assertEquals(date, list.getFirst().getDate());
        assertFalse(list.getFirst().isConfirmed());
    }

    // ---------- GET /appointments/{id} ----------

    @Test
    public void testGetAppointmentReturnOk() throws Exception {
        Appointment mock = new Appointment(1, LocalDate.now(), "Chequeo", true, 25f, 15, null, null, null);
        when(appointmentService.get(1)).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments/{appointmentId}", "1")
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        Appointment result = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertNotNull(result);
        assertEquals("Chequeo", result.getReason());
    }

    @Test
    public void testGetAppointmentNotFound() throws Exception {
        when(appointmentService.get(1)).thenThrow(new AppointmentNotFoundException(APPOINTMENT_NOT_FOUND_MSG));

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments/{appointmentId}", "1"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(
                response.getResponse().getContentAsString(), new TypeReference<>(){});

        assertNotNull(error);
        assertEquals(404, error.getCode());
        assertEquals(APPOINTMENT_NOT_FOUND_MSG, error.getMessage());
    }

    // ---------- POST /patients/{patientId}/doctors/{doctorId}/appointments ----------

    @Test
    public void testAddAppointmentCreated() throws Exception {
        long patientId = 10, doctorId = 100;
        LocalDate date = LocalDate.now().plusDays(1);

        AppointmentRegistrationDto dto = new AppointmentRegistrationDto(date, "Fiebre", true, 35f, 20);
        AppointmentOutDto out = new AppointmentOutDto(1, date, "Fiebre", true, 35f, 20, patientId, doctorId);

        when(appointmentService.add(patientId, doctorId, dto)).thenReturn(out);

        String body = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/patients/{patientId}/doctors/{doctorId}/appointments", patientId, doctorId)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.date").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("Fiebre"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.patientId").value((int) patientId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.doctorId").value((int) doctorId));
    }

    @Test
    public void testAddAppointmentPatientNotFound() throws Exception {
        long patientId = 999, doctorId = 100;
        LocalDate date = LocalDate.now().plusDays(1);
        AppointmentRegistrationDto dto = new AppointmentRegistrationDto(date, "X", false, 0f, 10);

        when(appointmentService.add(patientId, doctorId, dto))
                .thenThrow(new PatientNotFoundException(PATIENT_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/patients/{patientId}/doctors/{doctorId}/appointments", patientId, doctorId)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(404, error.getCode());
        assertEquals(PATIENT_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testAddAppointmentDoctorNotFound() throws Exception {
        long patientId = 10, doctorId = 999;
        LocalDate date = LocalDate.now().plusDays(1);
        AppointmentRegistrationDto dto = new AppointmentRegistrationDto(date, "X", false, 0f, 10);

        when(appointmentService.add(patientId, doctorId, dto))
                .thenThrow(new DoctorNotFoundException(DOCTOR_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/patients/{patientId}/doctors/{doctorId}/appointments", patientId, doctorId)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(404, error.getCode());
        assertEquals(DOCTOR_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testAddAppointmentValidationError() throws Exception {
        // fecha null -> @NotNull("La fecha de la cita es obligatoria")
        AppointmentRegistrationDto dto = new AppointmentRegistrationDto(null, "X", null, -5f, 0);

        String body = objectMapper.writeValueAsString(dto);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/patients/{patientId}/doctors/{doctorId}/appointments", 1, 1)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(400, error.getCode());
        assertEquals("Bad Request", error.getMessage());
        assertEquals("La fecha de la cita es obligatoria", error.getErrorMessages().get("date"));
        // Estos mensajes dependen de tus anotaciones @Min
        assertEquals("El coste no puede ser negativo", error.getErrorMessages().get("cost"));
        assertEquals("La duración debe ser mayor que 0", error.getErrorMessages().get("durationMinutes"));
    }

    // ---------- PUT /appointments/{id} ----------

    @Test
    public void testModifyAppointmentOk() throws Exception {
        long id = 1;
        AppointmentInDto in = new AppointmentInDto(LocalDate.now().plusDays(2), "Cambio", true, 40f, 25);
        AppointmentOutDto out = new AppointmentOutDto(id, in.getDate(), "Cambio", true, 40f, 25, 10, 100);

        when(appointmentService.modify(id, in)).thenReturn(out);

        String body = objectMapper.writeValueAsString(in);
        mockMvc.perform(
                        MockMvcRequestBuilders.put("/appointments/{appointmentId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value((int) id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("Cambio"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.confirmed").value(true));
    }

    @Test
    public void testModifyAppointmentNotFound() throws Exception {
        long id = 99;
        AppointmentInDto in = new AppointmentInDto(LocalDate.now(), "x", true, 10f, 10);

        when(appointmentService.modify(id, in)).thenThrow(new AppointmentNotFoundException(APPOINTMENT_NOT_FOUND_MSG));

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/appointments/{appointmentId}", id)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(404, error.getCode());
        assertEquals(APPOINTMENT_NOT_FOUND_MSG, error.getMessage());
    }

    @Test
    public void testModifyAppointmentValidationError() throws Exception {
        // Violamos @Min en cost y duration
        AppointmentInDto in = new AppointmentInDto(LocalDate.now(), "x", true, -1f, 0);

        String body = objectMapper.writeValueAsString(in);
        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.put("/appointments/{appointmentId}", 1)
                                .content(body)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(400, error.getCode());
        assertEquals("El coste no puede ser negativo", error.getErrorMessages().get("cost"));
        assertEquals("La duración debe ser mayor que 0", error.getErrorMessages().get("durationMinutes"));
    }

    // ---------- DELETE /appointments/{id} ----------

    @Test
    public void testDeleteNoContent() throws Exception {
        // No hace falta stub cuando no devuelve nada y no lanza excepción
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/appointments/{appointmentId}", 1))
                .andExpect(status().isNoContent());
    }



    // ---------- Endpoints JPQL / Native (200 OK) ----------

    @Test
    public void testByPatientEmailJPQLOk() throws Exception {
        List<AppointmentOutDto> mock = List.of(
                new AppointmentOutDto(1, LocalDate.now(), "A", true, 10f, 10, 10, 100)
        );
        when(appointmentService.findByPatientEmailJPQL("p@e.com")).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments/jpql/by-patient-email")
                                .queryParam("email", "p@e.com"))
                .andExpect(status().isOk())
                .andReturn();

        List<AppointmentOutDto> list = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(1, list.size());
    }

    @Test
    public void testByDoctorNameNativeOk() throws Exception {
        List<AppointmentOutDto> mock = List.of(
                new AppointmentOutDto(2, LocalDate.now(), "B", false, 0f, 5, 11, 101)
        );
        when(appointmentService.findByDoctorNameNative("House")).thenReturn(mock);

        MvcResult response = mockMvc.perform(
                        MockMvcRequestBuilders.get("/appointments/native/by-doctor-name")
                                .queryParam("name", "House"))
                .andExpect(status().isOk())
                .andReturn();

        List<AppointmentOutDto> list = objectMapper.readValue(response.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(1, list.size());
    }
}
