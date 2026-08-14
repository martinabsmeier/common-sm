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

import de.am.common.sm.context.StateContext;
import de.am.common.sm.event.Event;
import de.am.common.sm.exception.BreakAndContinueException;
import de.am.common.sm.exception.BreakAndStateChangeException;
import de.am.common.sm.exception.NoSuchStateException;
import de.am.common.sm.exception.UnhandledEventException;
import de.am.common.sm.transition.SelfTransition;
import de.am.common.sm.transition.Transition;
import lombok.Synchronized;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Runtime engine for a state machine composed of {@link State states}, {@link Transition transitions},
 * and a per-invocation {@link StateContext}.
 * <p>
 * In typical usage, a state machine is built through {@link StateMachineFactory} from
 * {@link de.am.common.sm.annotation.State}, {@link de.am.common.sm.annotation.Transition},
 * {@link de.am.common.sm.annotation.OnEntry}, and {@link de.am.common.sm.annotation.OnExit} annotations,
 * then driven through {@link StateMachineProxyBuilder}.
 * </p>
 * <p>
 * The engine supports hierarchical state lookup, entry and exit hooks, re-entrant event delivery through
 * an event queue, and call/return style control flow via {@link StateControl}.
 * </p>
 *
 * @author Martin Absmeier
 */
public final class StateMachine {

    private static final Logger LOGGER = LogManager.getLogger(StateMachine.class);

    private static final String CALL_STACK = StateMachine.class.getName() + ".callStack";

    private final State startState;

    private final Map<String, State> states;

    private final ThreadLocal<Boolean> processingThreadLocal;

    private final ThreadLocal<LinkedList<Event>> eventQueueThreadLocal;

    /**
     * Creates a new instance using the specified {@link State}s and start state.
     *
     * @param states       the {@link State}s.
     * @param startStateId the id of the start {@link State}.
     */
    public StateMachine(State[] states, String startStateId) {
        this.processingThreadLocal = new ThreadLocal<>();
        this.processingThreadLocal.remove();
        this.processingThreadLocal.set(Boolean.FALSE);

        this.eventQueueThreadLocal = new ThreadLocal<>();
        this.eventQueueThreadLocal.remove();
        this.eventQueueThreadLocal.set(new LinkedList<>());

        this.states = new HashMap<>();
        for (State state : states) {
            this.states.put(state.getId(), state);
        }
        this.startState = getState(startStateId);
    }

    /**
     * Creates a new instance using the specified {@link State}s and start state.
     *
     * @param states       the {@link State}s.
     * @param startStateId the id of the start {@link State}.
     */
    public StateMachine(Collection<State> states, String startStateId) {
        this(states.toArray(State[]::new), startStateId);
    }

    /**
     * Returns the {@link State} with the specified id.
     *
     * @param id the id of the {@link State} to return.
     * @return the {@link State}
     * @throws NoSuchStateException if no matching {@link State} could be found.
     */
    public State getState(String id) throws NoSuchStateException {
        State state = states.get(id);
        if (state == null) {
            throw new NoSuchStateException(id);
        }
        return state;
    }

    /**
     * Processes the specified {@link Event} through this {@link StateMachine}.
     * <p>
     * This method is usually invoked indirectly by a proxy created with {@link StateMachineProxyBuilder}, but it may
     * also be called directly when integrating the state machine into a custom event source.
     * </p>
     * <p>
     * If the same thread re-enters the state machine while an event is already being processed, the nested event is
     * queued and handled after the current transition completes.
     * </p>
     *
     * @param event the {@link Event} to be handled.
     */
    @Synchronized
    public void handle(Event event) {
        StateContext context = event.getContext();

        LinkedList<Event> eventQueue = eventQueueThreadLocal.get();
        eventQueue.addLast(event);

        boolean isProcessing = processingThreadLocal.get();
        if (isProcessing) {
            /* This thread is already processing an event. Queue this event. */
            LOGGER.debug("State machine called recursively. Queuing event {} for later processing.", event);
        } else {
            processingThreadLocal.set(true);
            try {
                if (context.getCurrentState() == null) {
                    context.setCurrentState(startState);
                }
                processEvents(eventQueue);
            } finally {
                processingThreadLocal.set(false);
            }
        }
    }

    // #################################################################################################################
    private void processEvents(LinkedList<Event> eventQueue) {
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.removeFirst();
            StateContext context = event.getContext();
            handle(context.getCurrentState(), event);
        }
    }

    private void handle(State state, Event event) {
        StateContext context = event.getContext();

        for (Transition transition : state.getTransitions()) {
            LOGGER.debug("Trying transition {}", transition);
            try {
                if (transition.execute(event)) {
                    LOGGER.debug("Transition {} executed successfully.", transition);
                    dispatchToState(context, transition.getNextState(), null, false);
                    return;
                }
            } catch (BreakAndContinueException bace) {
                LOGGER.debug("BreakAndContinueException thrown in transition {}. Continuing with next transition.", transition);
            } catch (BreakAndStateChangeException basce) {
                State newState = basce.resolveTarget(this, context);
                Stack<State> callStack = getCallStack(context);
                State fromStack = basce.applyStackEffect(this, context, callStack);
                if (newState == null) {
                    newState = fromStack;
                }
                logStateChange(transition, newState, basce.isNow(), basce.actionName());
                dispatchToState(context, newState, event, basce.isNow());
                return;
            }
        }

        handleParentOrFail(state, event);
    }

    /**
     * Moves the state machine to {@code target} and, if {@code now} is true, lets that state handle {@code event}
     * immediately. Performs the onExit/onEntry side effects and updates {@code context.getCurrentState()}.
     *
     * @param context the state context of the event in flight
     * @param target  the state to move to; may be {@code null} (in which case nothing happens)
     * @param event   the current event; only required when {@code now} is true
     * @param now     whether {@code target} should handle the current event immediately
     */
    private void dispatchToState(StateContext context, State target, Event event, boolean now) {
        setCurrentState(context, target);
        if (now) {
            handle(target, event);
        }
    }

    /**
     * Falls back to the parent state if there is one, otherwise fails with {@link UnhandledEventException}.
     */
    private void handleParentOrFail(State state, Event event) {
        if (state.getParent() != null) {
            handle(state.getParent(), event);
        } else {
            throw new UnhandledEventException(event);
        }
    }

    private void logStateChange(Transition t, State newState, boolean now, String action) {
        LOGGER.debug("BreakAnd{}Exception thrown in transition {}. Moving to state {} {}.",
                action, t, newState.getId(), now ? "now" : "next");
    }

    private Stack<State> getCallStack(StateContext context) {
        Stack<State> callStack = (Stack<State>) context.getAttribute(CALL_STACK);
        if (callStack == null) {
            callStack = new Stack<>();
            context.setAttribute(CALL_STACK, callStack);
        }
        return callStack;
    }

    private void setCurrentState(StateContext context, State newState) {
        if (newState != null) {
            if (newState != context.getCurrentState()) {
                LOGGER.debug("Leaving state {}", context.getCurrentState().getId());
                LOGGER.debug("Entering state {}", newState.getId());
            }
            executeOnExits(context, context.getCurrentState());
            executeOnEntries(context, newState);
            context.setCurrentState(newState);
        }
    }

    void executeOnExits(StateContext context, State state) {
        boolean isExecuted = false;

        List<SelfTransition> onExits = state.getOnExitSelfTransitions();
        if (onExits != null) {
            for (SelfTransition selfTransition : onExits) {
                selfTransition.execute(context, state);
                isExecuted = true;
                LOGGER.debug("Executing onExit action for {}", state.getId());
            }
        }
        if (!isExecuted) {
            LOGGER.debug("No onExit action for {}", state.getId());
        }
    }

    void executeOnEntries(StateContext context, State state) {
        boolean isExecuted = false;

        List<SelfTransition> onEntries = state.getOnEntrySelfTransitions();
        if (onEntries != null) {
            for (SelfTransition selfTransition : onEntries) {
                selfTransition.execute(context, state);
                isExecuted = true;
                LOGGER.debug("Executing onEntry action for {}", state.getId());
            }
        }
        if (!isExecuted) {
            LOGGER.debug("No onEntry action for {}", state.getId());
        }
    }
}