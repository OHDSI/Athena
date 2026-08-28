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
 * Company: Odysseus Data Services, Inc. (EPAM Systems company)
 * Created: August 28, 2026
 *
 */

package com.odysseusinc.athena.service.saver.v5.history.delta;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SqlScriptDeltaSaverTest {

    @Test
    public void guardsCdm55StatementsForSchemasThatDoNotHaveThoseTables() {

        String query = new SqlScriptDeltaSaver().query();

        assertTrue(query.contains("to_regclass('concept_metadata')"));
        assertTrue(query.contains("to_regclass('concept_relationship_metadata')"));
        assertTrue(query.contains("to_regclass('pack_content')"));
        assertTrue(query.contains("EXECUTE %L"));
        assertTrue(query.contains("ELSE script_text"));
    }
}
