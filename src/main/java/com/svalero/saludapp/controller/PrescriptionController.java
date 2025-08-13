package com.svalero.saludapp.controller;

import com.svalero.saludapp.domain.Prescription;
import com.svalero.saludapp.domain.dto.PrescriptionInDto;
import com.svalero.saludapp.domain.dto.PrescriptionOutDto;
import com.svalero.saludapp.domain.dto.PrescriptionRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.AppointmentNotFoundException;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.exception.PrescriptionNotFoundException;
import com.svalero.saludapp.service.PrescriptionService;
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
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;
    private final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    @GetMapping("/prescriptions")
    public ResponseEntity<List<PrescriptionOutDto>> getAll(
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "durationDays", required = false) Integer durationDays) {
        logger.info("BEGIN getAll prescriptions");
        List<PrescriptionOutDto> list = prescriptionService.getAll(active, durationDays);
        logger.info("END getAll prescriptions");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<Prescription> get(@PathVariable long prescriptionId)
            throws PrescriptionNotFoundException {
        logger.info("BEGIN get prescription");
        Prescription prescription = prescriptionService.get(prescriptionId);
        logger.info("END get prescription");
        return new ResponseEntity<>(prescription, HttpStatus.OK);
    }

    @PostMapping("/appointments/{appointmentId}/medicines/{medicineId}/prescriptions")
    public ResponseEntity<PrescriptionOutDto> add(@PathVariable long appointmentId,
                                                  @PathVariable long medicineId,
                                                  @Valid @RequestBody PrescriptionRegistrationDto dto)
            throws AppointmentNotFoundException, MedicineNotFoundException {
        logger.info("BEGIN add prescription");
        PrescriptionOutDto created = prescriptionService.add(appointmentId, medicineId, dto);
        logger.info("END add prescription");
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<PrescriptionOutDto> modify(@PathVariable long prescriptionId,
                                                     @Valid @RequestBody PrescriptionInDto dto)
            throws PrescriptionNotFoundException {
        logger.info("BEGIN modify prescription");
        PrescriptionOutDto updated = prescriptionService.modify(prescriptionId, dto);
        logger.info("END modify prescription");
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/prescriptions/{prescriptionId}")
    public ResponseEntity<Void> remove(@PathVariable long prescriptionId)
            throws PrescriptionNotFoundException {
        logger.info("BEGIN remove prescription");
        prescriptionService.remove(prescriptionId);
        logger.info("END remove prescription");
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(PrescriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePrescriptionNotFound(PrescriptionNotFoundException ex) {
        ErrorResponse error = ErrorResponse.generalError(404, ex.getMessage());
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        ErrorResponse error = ErrorResponse.generalError(404, ex.getMessage());
        logger.error(ex.getMessage(), ex);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MedicineNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMedicineNotFound(MedicineNotFoundException ex) {
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
