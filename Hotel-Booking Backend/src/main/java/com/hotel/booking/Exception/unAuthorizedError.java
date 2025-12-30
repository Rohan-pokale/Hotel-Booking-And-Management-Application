package com.hotel.booking.Exception;

public class unAuthorizedError extends RuntimeException{
    public unAuthorizedError(String message) {
        super(message);
    }
}
