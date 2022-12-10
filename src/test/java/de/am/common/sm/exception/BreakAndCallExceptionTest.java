/*
 * Copyright 2022 Martin Absmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.am.common.sm.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit test cases of {@link BreakAndCallException} class.
 *
 * @author Martin Absmeier
 */
class BreakAndCallExceptionTest {

    @Test
    void constructorException() {
        assertThrows(IllegalArgumentException.class, () -> new BreakAndCallException(null, "", true));
    }

    @Test
    void constructorTwoParameter() {
        String stateId = "testStateId";
        boolean now = true;
        BreakAndCallException ex = new BreakAndCallException(stateId, now);

        assertNotNull(ex, "We expect an instance.");
    }

    @Test
    void constructorThreeParameter() {
        String stateId = "testStateId";
        String returnStateId = "testReturnStateId";
        boolean now = true;
        BreakAndCallException ex = new BreakAndCallException(stateId, returnStateId, now);

        assertNotNull(ex, "We expect an instance.");
    }

    @Test
    void getter() {
        String stateId = "testStateId";
        String returnStateId = "testReturnStateId";
        boolean now = true;
        BreakAndCallException ex = new BreakAndCallException(stateId, returnStateId, now);

        assertEquals(stateId, ex.getStateId());
        assertEquals(returnStateId, ex.getReturnToStateId());
        assertEquals(now, ex.isNow());
    }
}