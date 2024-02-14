package com.odysseusinc.athena.api.v1.controller.converter.vocabulary;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class VocabularyVersionConverterTest {

    @Test
    public void testToOldFormat() {
        assertEquals("v5 31-AUG-23", VocabularyVersionConverter.toOldFormat(20230831));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToOldFormatWithInvalidFormat() {
        VocabularyVersionConverter.toOldFormat(1231231231);
    }

    @Test
    public void testToNewFormat() {
        assertEquals("v20230831", VocabularyVersionConverter.toNewFormat(20230831));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToNewFormatWithInvalidFormat() {
        VocabularyVersionConverter.toNewFormat(123);
    }

    @Test
    public void testFromNewToId() {
        assertEquals(20230831, VocabularyVersionConverter.fromNewToId("v20230831"));
    }

    @Test
    public void testFromOldToId() {
        assertEquals(20230831, VocabularyVersionConverter.fromOldToId("v5 31-AUG-23"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testFromNewToIdWithInvalidFormat() {
        VocabularyVersionConverter.fromNewToId("invalid_formatted_version");
    }

    @Test
    public void testFromOldToNewFormat() {
        assertEquals("v20230831", VocabularyVersionConverter.fromOldToNew("v5 31-AUG-23"));
    }

}