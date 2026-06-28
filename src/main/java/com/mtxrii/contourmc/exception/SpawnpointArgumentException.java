package com.mtxrii.contourmc.exception;

public class SpawnpointArgumentException extends IllegalArgumentException {

    public SpawnpointArgumentException(String message) {
        super("[Spawn] " + message);
    }
}
