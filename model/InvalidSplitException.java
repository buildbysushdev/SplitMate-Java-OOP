package model;

// Practical 6: USER-DEFINED EXCEPTION
public class InvalidSplitException extends Exception {
    public InvalidSplitException(String message) {
        super(message);
    }
}
