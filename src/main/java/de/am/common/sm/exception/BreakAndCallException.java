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

import de.am.common.sm.StateControl;
import de.am.common.sm.StateMachine;
import lombok.Getter;

import static java.util.Objects.isNull;

/**
 * {@code BreakAndCallException} is used by {@link StateControl}.
 * <p>
 * If this exception is used to change the execution of {@link StateControl} methods of a {@link StateMachine} you must
 * ensure that exceptions are caught by your code and not swallowed.
 * </p>
 *
 * @author Martin Absmeier
 */
public class BreakAndCallException extends RuntimeException {
    private static final long serialVersionUID = -5973306926764652458L;

    @Getter
    private final String stateId;
    @Getter
    private final String returnToStateId;
    @Getter
    private final boolean now;

    /**
     * Creates a new {@code BreakAndCallException} exception.
     *
     * @param stateId the id of the state
     * @param now     the now parameter
     */
    public BreakAndCallException(String stateId, boolean now) {
        this(stateId, null, now);
    }

    /**
     * Creates a new {@code BreakAndCallException} exception.
     *
     * @param stateId         the id of the state
     * @param returnToStateId the id of the return state
     * @param now             the now parameter
     */
    public BreakAndCallException(String stateId, String returnToStateId, boolean now) {
        if (isNull(stateId)) {
            throw new IllegalArgumentException("stateId");
        }
        this.stateId = stateId;
        this.returnToStateId = returnToStateId;
        this.now = now;
    }
}