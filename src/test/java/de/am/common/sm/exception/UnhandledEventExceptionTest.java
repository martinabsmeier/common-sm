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

import de.am.common.sm.context.DefaultStateContext;
import de.am.common.sm.event.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JUnit test cases of {@link UnhandledEventException} class.
 *
 * @author Martin Absmeier
 */
class UnhandledEventExceptionTest {

    @Test
    void constructor() {
        Event event = new Event("id", new DefaultStateContext());
        UnhandledEventException ex = new UnhandledEventException(event);

        assertNotNull(ex, "We expect an instance.");
        assertEquals(event, ex.getEvent());
    }
}