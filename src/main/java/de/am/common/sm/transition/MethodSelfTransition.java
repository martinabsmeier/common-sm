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
 * {@link SelfTransition} which invokes a {@link Method}. The {@link Method} can have zero or any number of StateContext
 * and State regarding order
 * <p>
 * Normally you wouldn't create instances of this class directly but rather use the {@link SelfTransition} annotation to
 * define the methods which should be used as transitions in your state machine and then let {@link StateMachineFactory}
 * create a {@link StateMachine} for you.
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

    public MethodSelfTransition(Method method, Object target) {
        this.method = method;
        this.target = target;
    }

    /**
     * Creates a new instance
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
        if (types[i].isAssignableFrom(StateContext.class)) {
            args[i++] = stateContext;
        }
        if (i < types.length && types[i].isAssignableFrom(State.class)) {
            args[i++] = state;
        }

        invokeMethod(args);

        return true;
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