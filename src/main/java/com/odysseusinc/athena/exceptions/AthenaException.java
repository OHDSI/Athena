package com.odysseusinc.athena.exceptions;

public class AthenaException extends RuntimeException {

    public AthenaException() {
    }

    public AthenaException(String message, Throwable cause) {

        super(message, cause);
    }
}
