package com.svalero.saludapp.exception;

public class DoctorNotFoundException extends Exception {

    public DoctorNotFoundException() {
        super("The doctor does not exist");
    }

    public DoctorNotFoundException(String message) {
        super(message);
    }
}