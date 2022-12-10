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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JUnit test cases of {@link StateMachineCreationException} class.
 *
 * @author Martin Absmeier
 */
class StateMachineCreationExceptionTest {

    @Test
    void constructorOneParameter() {
        StateMachineCreationException ex = new StateMachineCreationException("message");

        assertNotNull(ex, "We expect an instance.");
        assertFalse(ex.getMessage().isEmpty());
    }

    @Test
    void constructorTwoParameter() {
        StateMachineCreationException ex = new StateMachineCreationException("message", new RuntimeException());

        assertNotNull(ex, "We expect an instance.");
        assertFalse(ex.getMessage().isEmpty());
    }
}