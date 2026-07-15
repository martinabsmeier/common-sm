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
import de.am.common.sm.context.StateContext;
import de.am.common.sm.exception.MethodInvocationException;
import de.am.common.sm.exception.NoSuchMethodException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;

/**
 * {@link SelfTransition} implementation that invokes a target {@link Method}.
 * <p>
 * The target method may declare zero parameters, a single {@link StateContext} or {@link State}, or both in that order.
 * This is the runtime implementation used for {@link de.am.common.sm.annotation.OnEntry} and
 * {@link de.am.common.sm.annotation.OnExit} hooks created by {@link StateMachineFactory}.
 * </p>
 *
 * @author Martin Absmeier
 */
@Log4j2
public class MethodSelfTransition extends AbstractSelfTransition {

    @Getter
    private final Method method;
    private final Object target;
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    /**
     * Creates a new self transition that invokes the specified method on the target object.
     *
     * @param method the hook method to invoke.
     * @param target the object on which the method should be invoked.
     */
    public MethodSelfTransition(Method method, Object target) {
        this.method = method;
        this.target = target;
    }

    /**
     * Creates a new instance by resolving a method with the specified name on the target object.
     *
     * @param methodName the name of the target method.
     * @param target     the target object.
     */
    public MethodSelfTransition(String methodName, Object target) {
        this.target = target;

        Method[] candidates = target.getClass().getMethods();
        Method result = stream(candidates)
            .filter(candidate -> candidate.getName().equals(methodName))
            .findFirst().orElse(null);

        if (isNull(result)) {
            throw new NoSuchMethodException(methodName);
        }

        this.method = result;
    }

    /**
     * Invokes the configured hook method if its signature can be satisfied with the current
     * {@link StateContext} and {@link State}.
     *
     * @param stateContext the active state context.
     * @param state the state whose entry or exit hook is being processed.
     * @return {@code true} if the hook method was invoked, otherwise {@code false}.
     */
    @Override
    public boolean doExecute(StateContext stateContext, State state) {
        Class<?>[] types = method.getParameterTypes();

        if (types.length == 0) {
            invokeMethod(EMPTY_ARGUMENTS);
            return true;
        }

        if (types.length > 2) {
            return false;
        }

        Object[] args = new Object[types.length];

        int i = 0;
        if (matchesParameter(types[i], stateContext)) {
            args[i++] = stateContext;
        }
        if (i < types.length && matchesParameter(types[i], state)) {
            args[i++] = state;
        }

        if (i < types.length) {
            return false;
        }

        invokeMethod(args);

        return true;
    }

    private boolean matchesParameter(Class<?> paramType, Object argument) {
        return argument != null && paramType.isAssignableFrom(argument.getClass());
    }

    private void invokeMethod(Object[] arguments) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing method {} with arguments {}", method, asList(arguments));
            }
            method.invoke(target, arguments);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new MethodInvocationException(method, ex);
        }
    }
}