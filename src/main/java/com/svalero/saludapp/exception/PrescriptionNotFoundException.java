package com.svalero.saludapp.exception;

public class PrescriptionNotFoundException extends Exception {

    public PrescriptionNotFoundException() {
        super("The prescription does not exist");
    }

    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}