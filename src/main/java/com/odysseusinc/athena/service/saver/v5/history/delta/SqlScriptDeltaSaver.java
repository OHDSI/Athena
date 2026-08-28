/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Product Owner/Architecture: Gregory Klebanov
 * Author: Yaroslav Molodkov
 * Created: December 7, 2023
 *
 */

package com.odysseusinc.athena.service.saver.v5.history.delta;

import com.odysseusinc.athena.service.saver.SaverV5Delta;
import com.odysseusinc.athena.service.saver.v5.history.HistorySaver;
import org.springframework.stereotype.Service;

@Service
public class SqlScriptDeltaSaver extends HistorySaver implements SaverV5Delta {

    @Override
    public String fileName() {

        return "delta.sql";
    }

    @Override
    protected boolean isIncludeColumnNames() {
        return false;
    }

    @Override
    protected String query() {

        /*
         * A delta can be applied to a vocabulary schema created before CDM 5.5. Keep
         * ordinary statements unchanged, but execute statements for the three optional
         * tables dynamically and only when the target relation is on the search path.
         * Dynamic EXECUTE matters: it prevents PostgreSQL from resolving a missing table
         * while compiling the DO block's unselected branch.
         */
        return "SELECT CASE "
                + "WHEN script_text ~ '^(INSERT INTO|UPDATE|DELETE FROM) concept_metadata ' "
                + "THEN format($guard$DO $athena$ BEGIN "
                + "IF to_regclass('concept_metadata') IS NOT NULL THEN EXECUTE %L; END IF; "
                + "END $athena$;$guard$, script_text) "
                + "WHEN script_text ~ '^(INSERT INTO|UPDATE|DELETE FROM) concept_relationship_metadata ' "
                + "THEN format($guard$DO $athena$ BEGIN "
                + "IF to_regclass('concept_relationship_metadata') IS NOT NULL THEN EXECUTE %L; END IF; "
                + "END $athena$;$guard$, script_text) "
                + "WHEN script_text ~ '^(INSERT INTO|UPDATE|DELETE FROM) pack_content ' "
                + "THEN format($guard$DO $athena$ BEGIN "
                + "IF to_regclass('pack_content') IS NOT NULL THEN EXECUTE %L; END IF; "
                + "END $athena$;$guard$, script_text) "
                + "ELSE script_text END "
                + "FROM get_sql_statements_delta(:version, :versionDelta, :vocabularyArr)";
    }
}
