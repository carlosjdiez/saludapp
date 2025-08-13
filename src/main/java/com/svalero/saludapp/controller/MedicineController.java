package com.svalero.saludapp.controller;

import com.svalero.saludapp.domain.Medicine;
import com.svalero.saludapp.domain.dto.MedicineInDto;
import com.svalero.saludapp.domain.dto.MedicineOutDto;
import com.svalero.saludapp.domain.dto.MedicineRegistrationDto;
import com.svalero.saludapp.domain.dto.ErrorResponse;
import com.svalero.saludapp.exception.MedicineNotFoundException;
import com.svalero.saludapp.service.MedicineService;
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
public class MedicineController {

    @Autowired
    private MedicineService medicineService;
    private final Logger logger = LoggerFactory.getLogger(MedicineController.class);

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineOutDto>> getAll(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "manufacturer", defaultValue = "") String manufacturer) {
        logger.info("BEGIN getAll medicines");
        List<MedicineOutDto> list = medicineService.getAll(name, manufacturer);
        logger.info("END getAll medicines");
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/medicines/{medicineId}")
    public ResponseEntity<Medicine> get(@PathVariable long medicineId) throws MedicineNotFoundException {
        logger.info("BEGIN get medicine");
        Medicine medicine = medicineService.get(medicineId);
        logger.info("END get medicine");
        return new ResponseEntity<>(medicine, HttpStatus.OK);
    }

    @PostMapping("/medicines")
    public ResponseEntity<MedicineOutDto> add(@Valid @RequestBody MedicineRegistrationDto dto) {
        logger.info("BEGIN add medicine");
        MedicineOutDto created = medicineService.add(dto);
        logger.info("END add medicine");
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/medicines/{medicineId}")
    public ResponseEntity<MedicineOutDto> modify(@PathVariable long medicineId,
                                                 @Valid @RequestBody MedicineInDto dto)
            throws MedicineNotFoundException {
        logger.info("BEGIN modify medicine");
        MedicineOutDto updated = medicineService.modify(medicineId, dto);
        logger.info("END modify medicine");
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/medicines/{medicineId}")
    public ResponseEntity<Void> remove(@PathVariable long medicineId) throws MedicineNotFoundException {
        logger.info("BEGIN remove medicine");
        medicineService.remove(medicineId);
        logger.info("END remove medicine");
        return ResponseEntity.noContent().build();
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
