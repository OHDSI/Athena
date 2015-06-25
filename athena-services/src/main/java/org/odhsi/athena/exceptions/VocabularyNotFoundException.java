package org.odhsi.athena.exceptions;

/**
 * Created by GMalikov on 22.06.2015.
 */
public class VocabularyNotFoundException extends Exception {

    public VocabularyNotFoundException(String message){
        super(message);
    }

    public VocabularyNotFoundException(String message, Throwable throwable){
        super(message, throwable);
    }

}
