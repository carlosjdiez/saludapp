package com.svalero.saludapp.exception;

public class MedicineNotFoundException extends Exception {

    public MedicineNotFoundException() {
        super("The medicine does not exist");
    }

    public MedicineNotFoundException(String message) {
        super(message);
    }
}