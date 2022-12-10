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

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JUnit test cases of {@link MethodInvocationException} class.
 *
 * @author Martin Absmeier
 */
class MethodInvocationExceptionTest {

    @Test
    void constructor() {
        Method method = this.getClass().getDeclaredMethods()[0];
        MethodInvocationException ex = new MethodInvocationException(method, new RuntimeException("The cause"));

        assertNotNull(ex, "We expect an instance.");
        assertFalse(ex.getMessage().isEmpty());
    }
}