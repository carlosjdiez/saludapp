package com.svalero.saludapp.controller;

import com.svalero.saludapp.domain.Doctor;
import com.svalero.saludapp.domain.dto.DoctorInDto;
import com.svalero.saludapp.domain.dto.DoctorOutDto;
import com.svalero.saludapp.domain.dto.DoctorRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.DoctorNotFoundException;
import com.svalero.saludapp.service.DoctorService;
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
public class DoctorController {

    @Autowired
    private DoctorService doctorService;
    private final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorOutDto>> getAll(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "specialty", defaultValue = "") String specialty) {
        logger.info("BEGIN getAll doctors");
        List<DoctorOutDto> list = doctorService.getAll(name, specialty);
        logger.info("END getAll doctors");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<Doctor> get(@PathVariable long doctorId) throws DoctorNotFoundException {
        logger.info("BEGIN get doctor");
        Doctor doctor = doctorService.get(doctorId);
        logger.info("END get doctor");
        return new ResponseEntity<>(doctor, HttpStatus.OK);
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorOutDto> add(@Valid @RequestBody DoctorRegistrationDto dto) {
        logger.info("BEGIN add doctor");
        DoctorOutDto created = doctorService.add(dto);
        logger.info("END add doctor");
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<DoctorOutDto> modify(@PathVariable long doctorId,
                                               @Valid @RequestBody DoctorInDto dto)
            throws DoctorNotFoundException {
        logger.info("BEGIN modify doctor");
        DoctorOutDto updated = doctorService.modify(doctorId, dto);
        logger.info("END modify doctor");
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/doctors/{doctorId}")
    public ResponseEntity<Void> remove(@PathVariable long doctorId) throws DoctorNotFoundException {
        logger.info("BEGIN remove doctor");
        doctorService.remove(doctorId);
        logger.info("END remove doctor");
        return ResponseEntity.noContent().build();
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
