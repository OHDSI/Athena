package com.odysseusinc.athena.api.v1.controller.converter.vocabulary;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ReleaseVocabularyVersionConverterTest {

    @Test
    public void testToOldFormat() {
        assertEquals("v5 31-AUG-23", ReleaseVocabularyVersionConverter.toOldFormat(20230831));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToOldFormatWithInvalidFormat() {
        ReleaseVocabularyVersionConverter.toOldFormat(1231231231);
    }

    @Test
    public void testToNewFormat() {
        assertEquals("v20230831", ReleaseVocabularyVersionConverter.toNewFormat(20230831));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToNewFormatWithInvalidFormat() {
        ReleaseVocabularyVersionConverter.toNewFormat(123);
    }

    @Test
    public void testFromNewToId() {
        assertEquals(20230831, ReleaseVocabularyVersionConverter.fromNewToId("v20230831"));
    }

    @Test
    public void testFromOldToId() {
        assertEquals(20230831, ReleaseVocabularyVersionConverter.fromOldToId("v5 31-AUG-23"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testFromNewToIdWithInvalidFormat() {
        ReleaseVocabularyVersionConverter.fromNewToId("invalid_formatted_version");
    }

    @Test
    public void testFromOldToNewFormat() {
        assertEquals("v20230831", ReleaseVocabularyVersionConverter.fromOldToNew("v5 31-AUG-23"));
    }

}