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

/**
 * {@code BreakAndContinueException} is used by {@link StateControl}.
 * <p>
 * If this exception is used to change the execution of {@link StateControl} methods of a {@link StateMachine} you must
 * ensure that exceptions are caught by your code and not swallowed.
 * </p>
 *
 * @author Martin Absmeier
 */
public class BreakAndContinueException extends RuntimeException {
    private static final long serialVersionUID = -6166471981111377775L;
}