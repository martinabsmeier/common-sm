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
package de.am.common.sm;

import de.am.common.sm.event.Event;
import de.am.common.sm.exception.BreakAndCallException;
import de.am.common.sm.exception.BreakAndContinueException;
import de.am.common.sm.exception.BreakAndGotoException;
import de.am.common.sm.exception.BreakAndReturnException;
import de.am.common.sm.transition.Transition;

/**
 * Utility methods for altering state-machine control flow from inside a transition handler.
 * <p>
 * The {@code *Now()} family moves to a new {@link State} immediately and lets that state handle the current
 * {@link Event}. The {@code *Next()} family defers execution until the next event and is therefore the programmatic
 * equivalent of declaring {@link de.am.common.sm.annotation.Transition#next()} on an annotation.
 * </p>
 * <p>
 * The {@code breakAndCall*()} and {@code breakAndReturn*()} methods provide subroutine-like behavior. Calling a state
 * pushes the current state, or an explicit {@code returnTo} state, on an internal call stack. Returning pops the most
 * recently stored state and resumes execution there.
 * </p>
 * <p>
 * These methods intentionally throw internal control-flow exceptions that are interpreted by {@link StateMachine}; they
 * are not expected to return normally.
 * </p>
 *
 * @author Martin Absmeier
 */
public final class StateControl {

    /**
     * Breaks the execution of the current {@link Transition} and tries to find another {@link Transition} with higher
     * weight or a {@link Transition} of a parent {@link State} which can handle the current {@link Event}.
     */
    public static void breakAndContinue() {
        throw new BreakAndContinueException();
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the {@link State} with the specified id handle the
     * <strong>current</strong> {@link Event}.
     *
     * @param state the id of the {@link State} to go to.
     */
    public static void breakAndGotoNow(String state) {
        throw new BreakAndGotoException(state, true);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the {@link State} with the specified id handle the
     * <strong>next</strong> {@link Event}. Using this method is the programmatic equivalent of using the
     * {@link Transition} annotation.
     *
     * @param state the id of the {@link State} to go to.
     */
    public static void breakAndGotoNext(String state) {
        throw new BreakAndGotoException(state, false);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the {@link State} with the specified id handle the
     * <strong>current</strong> {@link Event}. Before moving to the new state the current state will be recorded. The
     * next call to {@link #breakAndReturnNow()} or {@link #breakAndReturnNext()} will return to the current state.
     *
     * @param state the id of the {@link State} to call.
     */
    public static void breakAndCallNow(String state) {
        throw new BreakAndCallException(state, true);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the {@link State} with the specified id handle the
     * <strong>next</strong> {@link Event}. Before moving to the new state the current state will be recorded. The next
     * call to {@link #breakAndReturnNow()} or {@link #breakAndReturnNext()} will return to the current state.
     *
     * @param state the id of the {@link State} to call.
     */
    public static void breakAndCallNext(String state) {
        throw new BreakAndCallException(state, false);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the {@link State} with the specified id handle the
     * <strong>current</strong> {@link Event}. Before moving to the new state the current state will be recorded. The next
     * call to {@link #breakAndReturnNow()} or {@link #breakAndReturnNext()} will return to the specified <code>returnTo</code> state.
     *
     * @param state    the id of the {@link State} to call.
     * @param returnTo the id of the {@link State} to return to.
     */
    public static void breakAndCallNow(String state, String returnTo) {
        throw new BreakAndCallException(state, returnTo, true);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the {@link State} with the specified id handle the
     * <strong>next</strong> {@link Event}. Before moving to the new state the current state will be recorded. The next call to
     * {@link #breakAndReturnNow()} or {@link #breakAndReturnNext()} will return to the specified <code>returnTo</code> state.
     *
     * @param state    the id of the {@link State} to call.
     * @param returnTo the id of the {@link State} to return to.
     */
    public static void breakAndCallNext(String state, String returnTo) {
        throw new BreakAndCallException(state, returnTo, false);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the last recorded {@link State} handle the <strong>current</strong>
     * {@link Event}.
     */
    public static void breakAndReturnNow() {
        throw new BreakAndReturnException(true);
    }

    /**
     * Breaks the execution of the current {@link Transition} and lets the last recorded {@link State} handle the <strong>next</strong>
     * {@link Event}.
     */
    public static void breakAndReturnNext() {
        throw new BreakAndReturnException(false);
    }

    // #################################################################################################################
    private StateControl() {
        // Utility class
    }
}