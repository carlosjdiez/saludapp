package com.svalero.saludapp.controller;

import com.svalero.saludapp.domain.Appointment;
import com.svalero.saludapp.domain.dto.AppointmentInDto;
import com.svalero.saludapp.domain.dto.AppointmentOutDto;
import com.svalero.saludapp.domain.dto.AppointmentRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.AppointmentNotFoundException;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.service.AppointmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    private final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentOutDto>> getAll(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "confirmed", required = false) Boolean confirmed) {
        logger.info("BEGIN getAll appointments");
        List<AppointmentOutDto> list = appointmentService.getAll(date, confirmed);
        logger.info("END getAll appointments");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/{appointmentId}")
    public ResponseEntity<Appointment> get(@PathVariable long appointmentId) throws AppointmentNotFoundException {
        logger.info("BEGIN get appointment");
        Appointment appointment = appointmentService.get(appointmentId);
        logger.info("END get appointment");
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    @PostMapping("/patients/{patientId}/doctors/{doctorId}/appointments")
    public ResponseEntity<AppointmentOutDto> add(@PathVariable long patientId,
                                                 @PathVariable long doctorId,
                                                 @Valid @RequestBody AppointmentRegistrationDto dto)
            throws PatientNotFoundException, DoctorNotFoundException {
        logger.info("BEGIN add appointment");
        AppointmentOutDto created = appointmentService.add(patientId, doctorId, dto);
        logger.info("END add appointment");
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/appointments/{appointmentId}")
    public ResponseEntity<AppointmentOutDto> modify(@PathVariable long appointmentId,
                                                    @Valid @RequestBody AppointmentInDto dto)
            throws AppointmentNotFoundException {
        logger.info("BEGIN modify appointment");
        AppointmentOutDto updated = appointmentService.modify(appointmentId, dto);
        logger.info("END modify appointment");
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> remove(@PathVariable long appointmentId) throws AppointmentNotFoundException {
        logger.info("BEGIN remove appointment");
        appointmentService.remove(appointmentId);
        logger.info("END remove appointment");
        return ResponseEntity.noContent().build();
    }

    // ===== Endpoints JPQL =====
    @GetMapping("/appointments/jpql/by-patient-email")
    public ResponseEntity<List<AppointmentOutDto>> byPatientEmailJPQL(@RequestParam String email) {
        logger.info("BEGIN byPatientEmailJPQL");
        var list = appointmentService.findByPatientEmailJPQL(email);
        logger.info("END byPatientEmailJPQL");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/jpql/by-doctor-name")
    public ResponseEntity<List<AppointmentOutDto>> byDoctorNameJPQL(@RequestParam String name) {
        logger.info("BEGIN byDoctorNameJPQL");
        var list = appointmentService.findByDoctorNameJPQL(name);
        logger.info("END byDoctorNameJPQL");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/jpql/with-cost-greater-than")
    public ResponseEntity<List<AppointmentOutDto>> withCostGreaterThanJPQL(@RequestParam float cost) {
        logger.info("BEGIN withCostGreaterThanJPQL");
        var list = appointmentService.findByCostGreaterThanJPQL(cost);
        logger.info("END withCostGreaterThanJPQL");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // ===== Endpoints SQL Nativos =====
    @GetMapping("/appointments/native/by-patient-email")
    public ResponseEntity<List<AppointmentOutDto>> byPatientEmailNative(@RequestParam String email) {
        logger.info("BEGIN byPatientEmailNative");
        var list = appointmentService.findByPatientEmailNative(email);
        logger.info("END byPatientEmailNative");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/native/by-doctor-name")
    public ResponseEntity<List<AppointmentOutDto>> byDoctorNameNative(@RequestParam String name) {
        logger.info("BEGIN byDoctorNameNative");
        var list = appointmentService.findByDoctorNameNative(name);
        logger.info("END byDoctorNameNative");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/native/with-cost-greater-than")
    public ResponseEntity<List<AppointmentOutDto>> withCostGreaterThanNative(@RequestParam float cost) {
        logger.info("BEGIN withCostGreaterThanNative");
        var list = appointmentService.findByCostGreaterThanNative(cost);
        logger.info("END withCostGreaterThanNative");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // ==== handlers ====
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        ErrorResponse error = ErrorResponse.generalError(404, ex.getMessage());
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        ErrorResponse error = ErrorResponse.generalError(404, ex.getMessage());
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDoctorNotFound(DoctorNotFoundException ex) {
        ErrorResponse error = ErrorResponse.generalError(404, ex.getMessage());
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String message = err.getDefaultMessage();
            errors.put(fieldName, message);
        });
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(ErrorResponse.validationError(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = ErrorResponse.generalError(500, "Internal Server Error");
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
