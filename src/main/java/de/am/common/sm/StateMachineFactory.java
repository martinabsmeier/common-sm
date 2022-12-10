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

import de.am.common.sm.annotation.OnEntry;
import de.am.common.sm.annotation.OnExit;
import de.am.common.sm.annotation.Transition;
import de.am.common.sm.annotation.TransitionAnnotation;
import de.am.common.sm.annotation.Transitions;
import de.am.common.sm.event.Event;
import de.am.common.sm.exception.StateMachineCreationException;
import de.am.common.sm.transition.MethodSelfTransition;
import de.am.common.sm.transition.MethodTransition;
import de.am.common.sm.transition.SelfTransition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

/**
 * Creates {@link StateMachine}s by reading {@link de.am.common.sm.annotation.State}, {@link Transition} and
 * {@link Transitions} (or equivalent) and {@link SelfTransition} annotations from one or more arbitrary objects.
 *
 * @author Martin Absmeier
 */
public class StateMachineFactory {

    private static final String ERROR_MESSAGE_METHOD = "Error encountered when processing method ";
    private static final String ERROR_MESSAGE_FIELD = "Error encountered when processing field ";

    private final Class<? extends Annotation> transitionAnnotation;

    private final Class<? extends Annotation> transitionsAnnotation;

    private final Class<? extends Annotation> entrySelfTransitionsAnnotation;

    private final Class<? extends Annotation> exitSelfTransitionsAnnotation;

    protected StateMachineFactory(Class<? extends Annotation> transitionAnnotation,
                                  Class<? extends Annotation> transitionsAnnotation,
                                  Class<? extends Annotation> entrySelfTransitionsAnnotation,
                                  Class<? extends Annotation> exitSelfTransitionsAnnotation) {

        this.transitionAnnotation = transitionAnnotation;
        this.transitionsAnnotation = transitionsAnnotation;
        this.entrySelfTransitionsAnnotation = entrySelfTransitionsAnnotation;
        this.exitSelfTransitionsAnnotation = exitSelfTransitionsAnnotation;
    }

    /**
     * Returns a new {@link StateMachineFactory} instance which creates {@link StateMachine}s by reading the specified
     * {@link Transition} equivalent annotation.
     *
     * @param transitionAnnotation the {@link Transition} equivalent annotation.
     * @return the {@link StateMachineFactory}.
     */
    public static StateMachineFactory create(Class<? extends Annotation> transitionAnnotation) {
        TransitionAnnotation a = transitionAnnotation.getAnnotation(TransitionAnnotation.class);
        if (a == null) {
            throw new IllegalArgumentException("The annotation class " + transitionAnnotation + " has not been annotated with the " + TransitionAnnotation.class.getName() + " annotation");
        }

        return new StateMachineFactory(transitionAnnotation, a.value(), OnEntry.class, OnExit.class);
    }

    /**
     * Creates a new {@link StateMachine} from the specified handler object and using a start state with id
     * <code>start</code>.
     *
     * @param handler the object containing the annotations describing the state machine.
     * @return the {@link StateMachine} object.
     */
    public StateMachine create(Object handler) {
        return create(handler, new Object[0]);
    }

    /**
     * Creates a new {@link StateMachine} from the specified handler object and using the {@link State} with the
     * specified id as start state.
     *
     * @param start   the id of the start {@link State} to use.
     * @param handler the object containing the annotations describing the state machine.
     * @return the {@link StateMachine} object.
     */
    public StateMachine create(String start, Object handler) {
        return create(start, handler, new Object[0]);
    }

    /**
     * Creates a new {@link StateMachine} from the specified handler objects and using a start state with id
     * <code>start</code>.
     *
     * @param handler  the first object containing the annotations describing the state machine.
     * @param handlers zero or more additional objects containing the annotations describing the state machine.
     * @return the {@link StateMachine} object.
     */
    public StateMachine create(Object handler, Object... handlers) {
        return create("start", handler, handlers);
    }

    /**
     * Creates a new {@link StateMachine} from the specified handler objects and using the {@link State} with the
     * specified id as start state.
     *
     * @param start    the id of the start {@link State} to use.
     * @param handler  the first object containing the annotations describing the state machine.
     * @param handlers zero or more additional objects containing the annotations describing the state machine.
     * @return the {@link StateMachine} object.
     */
    public StateMachine create(String start, Object handler, Object... handlers) {

        Map<String, State> states = new HashMap<>();
        List<Object> handlersList = new ArrayList<>(1 + handlers.length);
        handlersList.add(handler);
        handlersList.addAll(asList(handlers));

        LinkedList<Field> fields = new LinkedList<>();
        for (Object h : handlersList) {
            fields.addAll(getFields(h instanceof Class ? (Class<?>) h : h.getClass()));
        }
        for (State state : createStates(fields)) {
            states.put(state.getId(), state);
        }

        if (!states.containsKey(start)) {
            throw new StateMachineCreationException("Start state '" + start + "' not found.");
        }

        setupTransitions(transitionAnnotation, transitionsAnnotation, entrySelfTransitionsAnnotation, exitSelfTransitionsAnnotation, states, handlersList);

        return new StateMachine(states.values(), start);
    }

    private static void setupTransitions(Class<? extends Annotation> transitionAnnotation,
                                         Class<? extends Annotation> transitionsAnnotation, Class<? extends Annotation> onEntrySelfTransitionAnnotation,
                                         Class<? extends Annotation> onExitSelfTransitionAnnotation, Map<String, State> states, List<Object> handlers) {
        for (Object handler : handlers) {
            setupTransitions(transitionAnnotation, transitionsAnnotation, onEntrySelfTransitionAnnotation, onExitSelfTransitionAnnotation,
                             states, handler);
        }
    }

    private static void setupSelfTransitions(Method m, Class<? extends Annotation> onEntrySelfTransitionAnnotation,
                                             Class<? extends Annotation> onExitSelfTransitionAnnotation, Map<String, State> states, Object handler) {
        if (m.isAnnotationPresent(OnEntry.class)) {
            OnEntry onEntryAnnotation = (OnEntry) m.getAnnotation(onEntrySelfTransitionAnnotation);
            State state = states.get(onEntryAnnotation.value());
            if (state == null) {
                throw new StateMachineCreationException("Error encountered when processing onEntry annotation in method " + m + ". state " + onEntryAnnotation.value() + " not Found.");

            }
            state.addOnEntrySelfTransaction(new MethodSelfTransition(m, handler));
        }

        if (m.isAnnotationPresent(OnExit.class)) {
            OnExit onExitAnnotation = (OnExit) m.getAnnotation(onExitSelfTransitionAnnotation);
            State state = states.get(onExitAnnotation.value());
            if (state == null) {
                throw new StateMachineCreationException("Error encountered when processing onExit annotation in method " + m + ". state " + onExitAnnotation.value() + " not Found.");

            }
            state.addOnExitSelfTransaction(new MethodSelfTransition(m, handler));
        }
    }

    private static void setupTransitions(Class<? extends Annotation> transitionAnnotation,
                                         Class<? extends Annotation> transitionsAnnotation,
                                         Class<? extends Annotation> onEntrySelfTransitionAnnotation,
                                         Class<? extends Annotation> onExitSelfTransitionAnnotation,
                                         Map<String, State> states,
                                         Object handler) {

        Method[] methods = getAndSortMethods(handler);
        for (Method method : methods) {
            setupSelfTransitions(method, onEntrySelfTransitionAnnotation, onExitSelfTransitionAnnotation, states, handler);

            List<TransitionWrapper> transitionAnnotations = getTransitionAnnotations(transitionAnnotation, transitionsAnnotation, method);
            if (!transitionAnnotations.isEmpty()) {
                for (TransitionWrapper annotation : transitionAnnotations) {
                    Object[] eventIds = annotation.on();
                    checkIdsAndAnnotation(method, eventIds, annotation);
                    
                    for (Object event : eventIds) {
                        event = getEvent(event);

                        extracted(states, handler, method, annotation, event);
                    }
                }
            }
        }
    }

    public static List<Field> getFields(Class<?> clazz) {
        LinkedList<Field> fields = new LinkedList<>();

        for (Field f : clazz.getDeclaredFields()) {
            if (!f.isAnnotationPresent(de.am.common.sm.annotation.State.class)) {
                continue;
            }

            if ((f.getModifiers() & Modifier.STATIC) == 0 || (f.getModifiers() & Modifier.FINAL) == 0 || !f.getType().equals(String.class)) {
                throw new StateMachineCreationException(ERROR_MESSAGE_FIELD + f + ". Only static final String fields can be used with the @State annotation.");
            }

            fields.add(f);
        }

        return fields;
    }

    public static State[] createStates(List<Field> fields) {
        LinkedHashMap<String, State> states = new LinkedHashMap<>();

        while (!fields.isEmpty()) {
            int size = fields.size();
            int numStates = states.size();
            for (int i = 0; i < size; i++) {
                Field f = fields.remove(0);

                String value;
                try {
                    value = (String) f.get(null);
                } catch (IllegalAccessException iae) {
                    throw new StateMachineCreationException(ERROR_MESSAGE_FIELD + f + ".", iae);
                }

                de.am.common.sm.annotation.State stateAnnotation = f.getAnnotation(de.am.common.sm.annotation.State.class);
                if (stateAnnotation.value().equals(de.am.common.sm.annotation.State.ROOT)) {
                    states.put(value, new State(value));
                } else if (states.containsKey(stateAnnotation.value())) {
                    states.put(value, new State(value, states.get(stateAnnotation.value())));
                } else {
                    // Move to the back of the list of fields for later processing
                    fields.add(f);
                }
            }

            /*
             * If no new states were added to states during this iteration it means that all fields in fields specify non-existent parents.
             */
            if (states.size() == numStates) {
                throw new StateMachineCreationException("Error encountered while creating FSM. The following fields specify non-existing parent states: " + fields);
            }
        }

        return states.values().toArray(State[]::new);
    }

    // #################################################################################################################
    private static Method[] getAndSortMethods(Object handler) {
        Method[] methods = handler.getClass().getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::toString));
        return methods;
    }

    private static List<TransitionWrapper> getTransitionAnnotations(Class<? extends Annotation> transitionAnnotation,
                                                                    Class<? extends Annotation> transitionsAnnotation,
                                                                    Method method) {

        List<TransitionWrapper> transitionAnnotations = new ArrayList<>();

        if (method.isAnnotationPresent(transitionAnnotation)) {
            transitionAnnotations.add(
                new TransitionWrapper(transitionAnnotation, method.getAnnotation(transitionAnnotation)));
        }
        if (method.isAnnotationPresent(transitionsAnnotation)) {
            transitionAnnotations.addAll(
                asList(new TransitionsWrapper(transitionAnnotation, transitionsAnnotation, method
                    .getAnnotation(transitionsAnnotation)).value()));
        }

        return transitionAnnotations;
    }

    private static void checkIdsAndAnnotation(Method method, Object[] eventIds, TransitionWrapper annotation) {
        if (eventIds.length == 0) {
            throw new StateMachineCreationException(ERROR_MESSAGE_METHOD + method + ". No event ids specified.");
        }
        if (annotation.in().length == 0) {
            throw new StateMachineCreationException(ERROR_MESSAGE_METHOD + method + ". No states specified.");
        }
    }

    private static Object getEvent(Object event) {
        if (isNull(event)) {
            event = Event.WILDCARD_EVENT_ID;
        }
        if (!(event instanceof String)) {
            event = event.toString();
        }
        return event;
    }

    private static void extracted(Map<String, State> states, Object handler, Method method, TransitionWrapper annotation, Object event) {
        for (String in : annotation.in()) {
            State state = states.get(in);
            if (isNull(state)) {
                throw new StateMachineCreationException(ERROR_MESSAGE_METHOD + method + ". Unknown state: " + in + ".");
            }

            State next = getState(states, method, annotation);
            state.addTransition(new MethodTransition(event, next, method, handler), annotation.weight());
        }
    }

    private static State getState(Map<String, State> states, Method method, TransitionWrapper annotation) {
        State next = null;
        if (!annotation.next().equals(Transition.SELF)) {
            next = states.get(annotation.next());
            if (isNull(next)) {
                throw new StateMachineCreationException(ERROR_MESSAGE_METHOD + method + ". Unknown next state: " + annotation.next() + ".");
            }
        }
        return next;
    }

    private static class TransitionWrapper {

        private final Class<? extends Annotation> transitionClazz;

        private final Annotation annotation;

        public TransitionWrapper(Class<? extends Annotation> transitionClazz, Annotation annotation) {
            this.transitionClazz = transitionClazz;
            this.annotation = annotation;
        }

        Object[] on() {
            return getParameter("on", Object[].class);
        }

        String[] in() {
            return getParameter("in", String[].class);
        }

        String next() {
            return getParameter("next", String.class);
        }

        int weight() {
            return getParameter("weight", Integer.TYPE);
        }

        @SuppressWarnings("unchecked")
        private <T> T getParameter(String name, Class<T> returnType) {
            try {
                Method m = transitionClazz.getMethod(name);
                if (!returnType.isAssignableFrom(m.getReturnType())) {
                    throw new NoSuchMethodException();
                }
                return (T) m.invoke(annotation);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException |
                     InvocationTargetException t) {
                String message = "Could not get parameter '" + name + "' from Transition annotation " + transitionClazz;
                throw new StateMachineCreationException(message, t);
            }
        }
    }

    private static class TransitionsWrapper {

        private final Class<? extends Annotation> transitionsClazz;

        private final Class<? extends Annotation> transitionClazz;

        private final Annotation annotation;

        protected TransitionsWrapper(Class<? extends Annotation> transitionClazz,
                                  Class<? extends Annotation> transitionsClazz,
                                  Annotation annotation) {
            this.transitionClazz = transitionClazz;
            this.transitionsClazz = transitionsClazz;
            this.annotation = annotation;
        }

        TransitionWrapper[] value() {
            Annotation[] annos = getParameter("value", Annotation[].class);
            TransitionWrapper[] wrappers = new TransitionWrapper[annos.length];
            for (int i = 0; i < annos.length; i++) {
                wrappers[i] = new TransitionWrapper(transitionClazz, annos[i]);
            }
            return wrappers;
        }

        private <T> T getParameter(String name, Class<T> returnType) {
            try {
                Method method = transitionsClazz.getMethod(name);
                if (!returnType.isAssignableFrom(method.getReturnType())) {
                    throw new NoSuchMethodException();
                }
                return (T) method.invoke(annotation);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                String message = "Could not get parameter '" + name + "' from Transitions annotation " + transitionsClazz;
                throw new StateMachineCreationException(message, ex);
            }
        }
    }
}