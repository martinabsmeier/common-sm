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

import de.am.common.sm.StateMachine;
import de.am.common.sm.StateMachineFactory;

/**
 * {@code StateMachineCreationException} is thrown by {@link StateMachineFactory} when a {@link StateMachine} could not
 * be constructed for some reason.
 *
 * @author Martin Absmeier
 */
public class StateMachineCreationException extends RuntimeException {
    private static final long serialVersionUID = 4103502727376992746L;

    /**
     * Creates a new instance.
     *
     * @param message the message.
     */
    public StateMachineCreationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     *
     * @param message the message.
     * @param cause   the cause.
     */
    public StateMachineCreationException(String message, Throwable cause) {
        super(message, cause);
    }

}