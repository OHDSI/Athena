package org.odhsi.athena.exceptions;

/**
 * Created by GMalikov on 10.09.2015.
 */
public class VocabularyProcessingException extends Exception {
    public VocabularyProcessingException(String message){
        super(message);
    }

    public VocabularyProcessingException(String message, Throwable throwable){
        super(message, throwable);
    }
}
