package org.odhsi.athena.exceptions;

/**
 * Created by GMalikov on 22.06.2015.
 */
public class MissingVocabularyAttributeException extends Exception {

    public MissingVocabularyAttributeException(String message){
        super(message);
    }

    public MissingVocabularyAttributeException(String message, Throwable throwable){
        super(message, throwable);
    }
}
