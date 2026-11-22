package model;

// Practical 6: USER-DEFINED EXCEPTION
public class PersonNotFoundException extends Exception {
    public PersonNotFoundException(String message) {
        super(message);
    }
}
