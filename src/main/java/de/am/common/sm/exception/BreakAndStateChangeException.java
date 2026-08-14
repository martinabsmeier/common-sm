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

import de.am.common.sm.State;
import de.am.common.sm.StateControl;
import de.am.common.sm.StateMachine;
import de.am.common.sm.context.StateContext;
import lombok.Getter;

import java.io.Serial;
import java.util.Stack;

/**
 * Common superclass for control-flow exceptions raised by {@link StateControl} that cause the
 * {@link StateMachine} to switch to a different {@link State}.
 * <p>
 * This base class unifies {@link BreakAndGotoException}, {@link BreakAndCallException}, and
 * {@link BreakAndReturnException}. The engine catches a single instance of this type and dispatches
 * to the target state. {@link BreakAndContinueException} is intentionally <strong>not</strong> a
 * subclass of this type because it does not cause a state change.
 * </p>
 *
 * @author Martin Absmeier
 */
public abstract class BreakAndStateChangeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Whether the target state should handle the current event immediately.
     */
    @Getter
    private final boolean now;

    /**
     * Creates a new {@code BreakAndStateChangeException}.
     *
     * @param now whether the target state should handle the current event immediately
     */
    protected BreakAndStateChangeException(boolean now) {
        this.now = now;
    }

    /**
     * Resolves the target state the state machine should switch to.
     * <p>
     * For Goto and Call this looks up the configured state id. For Return this returns {@code null}
     * because the target state is determined by the call-stack pop in
     * {@link #applyStackEffect(StateMachine, StateContext, Stack)}.
     * </p>
     *
     * @param sm      the state machine used to look up state ids
     * @param context the current state context
     * @return the target {@link State}, or {@code null} if the target is determined by stack effect
     */
    public abstract State resolveTarget(StateMachine sm, StateContext context);

    /**
     * Applies any call-stack side effect (push for Call, pop for Return, nothing for Goto) and
     * returns the target state if it can only be determined by the stack effect (Return).
     *
     * @param sm        the state machine used to look up state ids
     * @param context   the current state context
     * @param callStack the state machine's call stack
     * @return the target state if the target is determined here, otherwise {@code null}
     */
    public abstract State applyStackEffect(StateMachine sm, StateContext context, Stack<State> callStack);

    /**
     * Returns a short human-readable name of the action, used in log messages
     * (e.g. {@code "Goto"}, {@code "Call"}, {@code "Return"}).
     *
     * @return the action name
     */
    public abstract String actionName();
}
