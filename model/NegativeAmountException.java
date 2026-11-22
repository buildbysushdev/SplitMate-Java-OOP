package model;

// Practical 6: USER-DEFINED EXCEPTION
public class NegativeAmountException extends Exception {
    public NegativeAmountException(String message) {
        super(message);
    }
}
