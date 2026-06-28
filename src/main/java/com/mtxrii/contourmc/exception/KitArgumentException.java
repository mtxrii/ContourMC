package com.mtxrii.contourmc.exception;

public class KitArgumentException extends IllegalArgumentException {

    public KitArgumentException(String message) {
        super("[Kit] " + message);
    }
}
