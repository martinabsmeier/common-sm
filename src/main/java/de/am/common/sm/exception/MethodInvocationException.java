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

import de.am.common.sm.transition.MethodTransition;

import java.lang.reflect.Method;

/**
 * {@code MethodInvocationException} is thrown by {@link MethodTransition} if the target method couldn't be invoked
 * or threw an exception.
 *
 * @author Martin Absmeier
 */
public class MethodInvocationException extends RuntimeException {
    private static final long serialVersionUID = 4288548621384649704L;

    /**
     * Creates a new instance for the specified {@link Method} and {@link Throwable}.
     *
     * @param method the {@link Method}.
     * @param cause  the reason.
     */
    public MethodInvocationException(Method method, Throwable cause) {
        super("Invoking method: " + method, cause);
    }

}