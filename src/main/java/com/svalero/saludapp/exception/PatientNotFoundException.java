package com.svalero.saludapp.exception;

public class PatientNotFoundException extends Exception {

    public PatientNotFoundException() {
        super("The patient does not exist");
    }

    public PatientNotFoundException(String message) {
        super(message);
    }
}
