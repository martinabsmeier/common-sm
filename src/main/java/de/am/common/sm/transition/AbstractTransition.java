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
package de.am.common.sm.transition;

import de.am.common.sm.State;
import de.am.common.sm.StateMachine;
import de.am.common.sm.event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.lang.String.valueOf;

/**
 * Abstract {@link Transition} implementation. Takes care of matching the current {@link Event}'s id against the id of
 * the {@link Event} this {@link Transition} handles. To handle any {@link Event} the id should be set to
 * {@link Event#WILDCARD_EVENT_ID}.
 *
 * @author Martin Absmeier
 */
@EqualsAndHashCode
public abstract class AbstractTransition implements Transition {

    @Getter
    private final Object eventId;
    @Getter
    private final State nextState;

    /**
     * Creates a new instance which will loopback to the same {@link State} for the specified {@link Event} id.
     *
     * @param eventId the {@link Event} id.
     */
    protected AbstractTransition(Object eventId) {
        this(eventId, null);
    }

    /**
     * Creates a new instance with the specified {@link State} as next state and for the specified {@link Event} id.
     *
     * @param eventId   the {@link Event} id.
     * @param nextState the next {@link State}.
     */
    protected AbstractTransition(Object eventId, State nextState) {
        this.eventId = eventId;
        this.nextState = nextState;
    }

    @Override
    public boolean execute(Event event) {
        if (!eventId.equals(Event.WILDCARD_EVENT_ID) && !eventId.equals(event.getId())) {
            return false;
        }

        return doExecute(event);
    }

    /**
     * Executes this {@link Transition}. This method doesn't have to check if the {@link Event}'s id matches because
     * {@link #execute(Event)} has already made sure that that is the case.
     *
     * @param event the current {@link Event}.
     * @return <code>true</code> if the {@link Transition} has been executed successfully and the {@link StateMachine}
     * should move to the next {@link State}. <code>false</code> otherwise.
     */
    protected abstract boolean doExecute(Event event);

    @Override
    public String toString() {
        return "eventId: ".concat(valueOf(eventId)).concat(" | nextState: ").concat(valueOf(nextState));
    }
}