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
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

/**
 * The user search builds a Postgres {@code SIMILAR TO} pattern from the caller's
 * query.
 * JUnit 4 on purpose.
 */
public class UserSearchPatternTest {

    private final UserService userService = new UserService();

    private String pattern(String query) {

        return (String) ReflectionTestUtils.invokeMethod(userService, "getSuggestRequest", query);
    }

    @Test
    public void ordinaryNamesAreUnchangedApartFromLowercasing() {

        assertEquals("%(jane)%", pattern("Jane"));
        assertEquals("%(jane|doe)%", pattern("Jane Doe"));
    }

    /** The DoS vector: quantifiers must lose their special meaning. */
    @Test
    public void quantifiersAreEscaped() {

        assertEquals("%(\\(\\(a\\|a\\)\\*\\)\\*)%", pattern("((a|a)*)*"));
    }

    /** The 500 vector: an unbalanced parenthesis must not reach Postgres as syntax. */
    @Test
    public void unbalancedParenthesisIsEscaped() {

        assertEquals("%(\\()%", pattern("("));
    }

    @Test
    public void wildcardsMatchLiterallyRatherThanMatchingEveryUser() {

        assertEquals("%(\\%)%", pattern("%"));
        assertEquals("%(\\_)%", pattern("_"));
    }

    @Test
    public void backslashIsEscapedFirstSoItCannotSmuggleAnEscape() {

        assertEquals("%(\\\\)%", pattern("\\"));
    }

    @Test
    public void bracketsAndBracesAreEscaped() {

        assertEquals("%(\\[a\\-z\\]\\{2\\})%".replace("\\-", "-"), pattern("[a-z]{2}"));
    }

    /** Multi-term queries still split on spaces, with each term escaped independently. */
    @Test
    public void eachTermIsEscapedIndependently() {

        assertEquals("%(jane\\*|doe\\()%", pattern("Jane* Doe("));
    }
}
