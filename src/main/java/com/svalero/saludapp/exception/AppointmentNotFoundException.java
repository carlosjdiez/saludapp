package com.svalero.saludapp.exception;

public class AppointmentNotFoundException extends Exception {

    public AppointmentNotFoundException() {
        super("The appointment does not exist");
    }

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}