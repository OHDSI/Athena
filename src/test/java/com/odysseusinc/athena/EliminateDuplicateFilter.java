package com.odysseusinc.athena;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class EliminateDuplicateFilter extends FilteringTokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final Set<String> terms = new HashSet<>();

    public EliminateDuplicateFilter(TokenStream in) {
        super(in);
    }

    @Override
    public boolean accept() {

        if (termAtt.toString() == null) {
            return true;
        }
        return terms.add(termAtt.toString().toLowerCase());
    }

    @Override
    public void end() throws IOException {

        super.end();
        terms.clear();
    }
}