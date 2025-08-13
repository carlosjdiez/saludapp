package com.svalero.saludapp.controller;

import com.svalero.saludapp.domain.Patient;
import com.svalero.saludapp.domain.dto.PatientInDto;
import com.svalero.saludapp.domain.dto.PatientOutDto;
import com.svalero.saludapp.domain.dto.PatientRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.PatientNotFoundException;
import com.svalero.saludapp.service.PatientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PatientController {

    @Autowired
    private PatientService patientService;
    private final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @GetMapping("/patients")
    public ResponseEntity<List<PatientOutDto>> getAll(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "surname", defaultValue = "") String surname) {
        logger.info("BEGIN getAll patients");
        List<PatientOutDto> list = patientService.getAll(name, surname);
        logger.info("END getAll patients");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<Patient> get(@PathVariable long patientId) throws PatientNotFoundException {
        logger.info("BEGIN get patient");
        Patient patient = patientService.get(patientId);
        logger.info("END get patient");
        return new ResponseEntity<>(patient, HttpStatus.OK);
    }

    @PostMapping("/patients")
    public ResponseEntity<PatientOutDto> add(@Valid @RequestBody PatientRegistrationDto dto) {
        logger.info("BEGIN add patient");
        PatientOutDto created = patientService.add(dto);
        logger.info("END add patient");
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/patients/{patientId}")
    public ResponseEntity<PatientOutDto> modify(@PathVariable long patientId,
                                                @Valid @RequestBody PatientInDto dto)
            throws PatientNotFoundException {
        logger.info("BEGIN modify patient");
        PatientOutDto updated = patientService.modify(patientId, dto);
        logger.info("END modify patient");
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/patients/{patientId}")
    public ResponseEntity<Void> remove(@PathVariable long patientId) throws PatientNotFoundException {
        logger.info("BEGIN remove patient");
        patientService.remove(patientId);
        logger.info("END remove patient");
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
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
