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
import de.am.common.sm.StateMachineFactory;
import de.am.common.sm.annotation.Transition;
import de.am.common.sm.context.StateContext;
import de.am.common.sm.event.Event;
import de.am.common.sm.exception.AmbiguousMethodException;
import de.am.common.sm.exception.MethodInvocationException;
import de.am.common.sm.exception.NoSuchMethodException;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

/**
 * {@link Transition} implementation that invokes a target {@link Method}.
 * <p>
 * The method is invoked only if its parameter list can be bound from the current {@link Event}. Parameter matching is
 * positional: the method may optionally accept the current {@link Event} as its first parameter, the current
 * {@link StateContext} as its second parameter, and then a subset of the event arguments in their original order.
 * </p>
 * <p>
 * This class underpins the annotation-driven API exposed by {@link StateMachineFactory}, but it can also be instantiated
 * directly when transitions need to be assembled programmatically.
 * </p>
 *
 * @author Martin Absmeier
 */
@EqualsAndHashCode(callSuper = true)
@Log4j2
public class MethodTransition extends AbstractTransition {

    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    private final Method method;
    private final Object target;

    /**
     * Creates a new instance which will loopback to the same {@link State} for the specified {@link Event} id.
     *
     * @param eventId the {@link Event} id.
     * @param method  the target method.
     * @param target  the target object.
     */
    public MethodTransition(Object eventId, Method method, Object target) {
        this(eventId, null, method, target);
    }

    /**
     * Creates a new instance which will loopback to the same {@link State} for the specified {@link Event} id. The target
     * {@link Method} will be the method in the specified target object with the same name as the specified {@link Event} id.
     *
     * @param eventId the {@link Event} id.
     * @param target  the target object.
     * @throws NoSuchMethodException    if no method could be found with a name equal to the {@link Event} id.
     * @throws AmbiguousMethodException if more than one method was found with a name equal to the {@link Event} id.
     */
    public MethodTransition(Object eventId, Object target) {
        this(eventId, eventId.toString(), target);
    }

    /**
     * Creates a new instance with the specified {@link State} as next state and for the specified {@link Event} id.
     *
     * @param eventId   the {@link Event} id.
     * @param nextState the next {@link State}.
     * @param method    the target method.
     * @param target    the target object.
     */
    public MethodTransition(Object eventId, State nextState, Method method, Object target) {
        super(eventId, nextState);
        this.method = method;
        this.target = target;
    }

    /**
     * Creates a new instance with the specified {@link State} as next state and for the specified {@link Event} id. The
     * target {@link Method} will be the method in the specified target object with the same name as the specified
     * {@link Event} id.
     *
     * @param eventId   the {@link Event} id.
     * @param nextState the next {@link State}.
     * @param target    the target object.
     * @throws NoSuchMethodException    if no method could be found with a name equal to the {@link Event} id.
     * @throws AmbiguousMethodException if more than one method was found with a name equal to the {@link Event} id.
     */
    public MethodTransition(Object eventId, State nextState, Object target) {
        this(eventId, nextState, eventId.toString(), target);
    }

    /**
     * Creates a new instance with the specified {@link State} as next state and for the specified {@link Event} id.
     *
     * @param eventId    the {@link Event} id.
     * @param nextState  the next {@link State}.
     * @param methodName the name of the target {@link Method}.
     * @param target     the target object.
     * @throws NoSuchMethodException    if the method could not be found.
     * @throws AmbiguousMethodException if there are more than one method with the specified name.
     */
    public MethodTransition(Object eventId, State nextState, String methodName, Object target) {
        super(eventId, nextState);

        this.target = target;

        Method[] candidates = target.getClass().getMethods();
        Method result = Arrays.stream(candidates)
            .filter(candidate -> candidate.getName().equals(methodName))
            .findAny().orElse(null);

        if (isNull(result)) {
            throw new NoSuchMethodException(methodName);
        }

        this.method = result;
    }

    /**
     * Creates a new instance which will loopback to the same {@link State} for the specified {@link Event} id.
     *
     * @param eventId    the {@link Event} id.
     * @param methodName the name of the target {@link Method}.
     * @param target     the target object.
     * @throws NoSuchMethodException    if the method could not be found.
     * @throws AmbiguousMethodException if there are more than one method with the specified name.
     */
    public MethodTransition(Object eventId, String methodName, Object target) {
        this(eventId, null, methodName, target);
    }

    /**
     * Attempts to bind the current {@link Event} to the target method and invokes it if a complete argument list can be
     * constructed.
     *
     * @param event the event currently being processed.
     * @return {@code true} if the method was invoked, otherwise {@code false}.
     */
    public boolean doExecute(Event event) {
        Class<?>[] types = method.getParameterTypes();

        if (types.length == 0) {
            invokeMethod(EMPTY_ARGUMENTS);
            return true;
        }

        if (types.length > 2 + event.getArguments().length) {
            return false;
        }

        Object[] args = new Object[types.length];

        int i = 0;
        if (match(types[i], event, Event.class)) {
            args[i++] = event;
        }
        if (i < args.length && match(types[i], event.getContext(), StateContext.class)) {
            args[i++] = event.getContext();
        }
        if (!bindEventArguments(types, args, i, event.getArguments(), 0)) {
            return false;
        }

        invokeMethod(args);

        return true;
    }

    /**
     * Returns the target {@link Method}.
     *
     * @return the method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the target object.
     *
     * @return the target object.
     */
    public Object getTarget() {
        return target;
    }

    private void invokeMethod(Object[] arguments) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing method {} with arguments {}.", method, asList(arguments));
            }
            method.invoke(target, arguments);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new MethodInvocationException(method, ex);
        }
    }

    private boolean bindEventArguments(Class<?>[] paramTypes, Object[] boundArgs, int paramIndex, Object[] eventArgs, int eventArgIndex) {
        if (paramIndex == boundArgs.length) {
            return true;
        }
        if (eventArgIndex == eventArgs.length) {
            return false;
        }

        Object eventArg = eventArgs[eventArgIndex];
        if (eventArg == null) {
            if (bindEventArguments(paramTypes, boundArgs, paramIndex, eventArgs, eventArgIndex + 1)) {
                return true;
            }
        }

        if (match(paramTypes[paramIndex], eventArg, Object.class)) {
            boundArgs[paramIndex] = eventArg;
            if (bindEventArguments(paramTypes, boundArgs, paramIndex + 1, eventArgs, eventArgIndex + 1)) {
                return true;
            }
            boundArgs[paramIndex] = null;
        }

        return bindEventArguments(paramTypes, boundArgs, paramIndex, eventArgs, eventArgIndex + 1);
    }

    private boolean match(Class<?> paramType, Object arg, Class<?> argType) {
        if (paramType.isPrimitive()) {
            return isMatchingPrimitive(paramType, arg);
        }
        if (arg == null) {
            return !paramType.isPrimitive() && argType.isAssignableFrom(paramType);
        }
        return argType.isAssignableFrom(paramType) && paramType.isAssignableFrom(arg.getClass());
    }

    private boolean isMatchingPrimitive(Class<?> paramType, Object arg) {
        Class<?> wrapperType = getWrapperType(paramType);
        return wrapperType != null && wrapperType.isInstance(arg);
    }

    private Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType.equals(Boolean.TYPE)) {
            return Boolean.class;
        }
        if (primitiveType.equals(Integer.TYPE)) {
            return Integer.class;
        }
        if (primitiveType.equals(Long.TYPE)) {
            return Long.class;
        }
        if (primitiveType.equals(Short.TYPE)) {
            return Short.class;
        }
        if (primitiveType.equals(Byte.TYPE)) {
            return Byte.class;
        }
        if (primitiveType.equals(Double.TYPE)) {
            return Double.class;
        }
        if (primitiveType.equals(Float.TYPE)) {
            return Float.class;
        }
        if (primitiveType.equals(Character.TYPE)) {
            return Character.class;
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString().concat(" | method: ").concat(valueOf(method)).concat(" | target: ").concat(valueOf(target));
    }
}