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
import de.am.common.sm.exception.BreakAndCallException;
import de.am.common.sm.exception.BreakAndContinueException;
import de.am.common.sm.exception.BreakAndGotoException;
import de.am.common.sm.exception.BreakAndReturnException;
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
 * Represents a complete state machine. Contains a collection of {@link State} objects connected by {@link Transition}s.
 * Normally you wouldn't create instances of this class directly but rather use the
 * {@link de.am.common.sm.annotation.State} annotation to define your states and then let {@link StateMachineFactory}
 * create a {@link StateMachine} for you.
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
     * Processes the specified {@link Event} through this {@link StateMachine}. Normally you wouldn't call this directly
     * but rather use {@link StateMachineProxyBuilder} to create a proxy for an interface of your choice. Any method
     * calls on the proxy will be translated into {@link Event} objects and then fed to the {@link StateMachine} by the
     * proxy using this method.
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("State machine called recursively. Queuing event {} for later processing.", event);
            }
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

        for (Transition t : state.getTransitions()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying transition {}", t);
            }

            try {
                if (t.execute(event)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Transition {} executed successfully.", t);
                    }
                    setCurrentState(context, t.getNextState());

                    return;
                }
            } catch (BreakAndContinueException bace) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("BreakAndContinueException thrown in transition {}. Continuing with next transition.", t);
                }
            } catch (BreakAndGotoException bage) {
                State newState = getState(bage.getStateId());

                if (bage.isNow()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("BreakAndGotoException thrown in transition {}. Moving to state {} now.", t, newState.getId());
                    }
                    setCurrentState(context, newState);
                    handle(newState, event);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("BreakAndGotoException thrown in transition {}. Moving to state {} next.", t, newState.getId());
                    }
                    setCurrentState(context, newState);
                }
                return;
            } catch (BreakAndCallException bace) {
                State newState = getState(bace.getStateId());

                Stack<State> callStack = getCallStack(context);
                State returnTo = bace.getReturnToStateId() != null ? getState(bace.getReturnToStateId()) : context.getCurrentState();
                callStack.push(returnTo);

                if (bace.isNow()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("BreakAndCallException thrown in transition {}. Moving to state {} noe.", t, newState.getId());
                    }
                    setCurrentState(context, newState);
                    handle(newState, event);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("BreakAndCallException thrown in transition {}. Moving to state {} next.", t, newState.getId());
                    }
                    setCurrentState(context, newState);
                }
                return;
            } catch (BreakAndReturnException bare) {
                Stack<State> callStack = getCallStack(context);
                State newState = callStack.pop();

                if (bare.isNow()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("BreakAndReturnException thrown in transition {}. Moving to state {} now.", t, newState.getId());
                    }
                    setCurrentState(context, newState);
                    handle(newState, event);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("BreakAndReturnException thrown in transition {}. Moving to state {} next", t, newState.getId());
                    }
                    setCurrentState(context, newState);
                }
                return;
            }
        }

        /* No transition could handle the event. Try with the parent state if there is one. */
        if (state.getParent() != null) {
            handle(state.getParent(), event);
        } else {
            throw new UnhandledEventException(event);
        }
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
            if (LOGGER.isDebugEnabled() && newState != context.getCurrentState()) {
                LOGGER.debug("Leaving state {}", context.getCurrentState().getId());
                LOGGER.debug("Entering state {}", newState.getId());
            }
            executeOnExits(context, context.getCurrentState());
            executeOnEntries(context, newState);
            context.setCurrentState(newState);
        }
    }

    void executeOnExits(StateContext context, State state) {
        List<SelfTransition> onExits = state.getOnExitSelfTransitions();
        boolean isExecuted = false;

        if (onExits != null) {
            for (SelfTransition selfTransition : onExits) {
                selfTransition.execute(context, state);
                if (LOGGER.isDebugEnabled()) {
                    isExecuted = true;
                    LOGGER.debug("Executing onEntry action for {}", state.getId());
                }
            }
        }
        if (LOGGER.isDebugEnabled() && !isExecuted) {
            LOGGER.debug("No onEntry action for {}", state.getId());
        }
    }

    void executeOnEntries(StateContext context, State state) {
        boolean isExecuted = false;

        List<SelfTransition> onEntries = state.getOnEntrySelfTransitions();
        if (onEntries != null) {
            for (SelfTransition selfTransition : onEntries) {
                selfTransition.execute(context, state);
                if (LOGGER.isDebugEnabled()) {
                    isExecuted = true;
                    LOGGER.debug("Executing onExit action for {}", state.getId());
                }
            }
        }
        if (LOGGER.isDebugEnabled() && !isExecuted) {
            LOGGER.debug("No onEntry action for {}", state.getId());
        }
    }
}