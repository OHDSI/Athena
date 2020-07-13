package com.odysseusinc.athena;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class EliminateDuplicateFilterFactory extends TokenFilterFactory {

    /** Creates a new EliminateDuplicateFilterFactory */
    public EliminateDuplicateFilterFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public EliminateDuplicateFilter create(TokenStream input) {
        final EliminateDuplicateFilter filter = new EliminateDuplicateFilter(input);
        return filter;
    }
}