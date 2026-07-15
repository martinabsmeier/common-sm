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
import de.am.common.sm.context.DefaultStateContext;
import de.am.common.sm.context.StateContext;
import de.am.common.sm.exception.NoSuchMethodException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test cases of {@link MethodSelfTransition} class.
 *
 * @author Martin Absmeier
 */
class MethodSelfTransitionTest extends AbstractTransitionTest {

    @Test
    void constructorException() {
        Assertions.assertThrows(NoSuchMethodException.class, () -> new MethodSelfTransition("methodName", target));
    }

    @Test
    void getName() {
        String methodName = "noArgs";
        MethodSelfTransition transition = new MethodSelfTransition(methodName, target);
        Method method = transition.getMethod();
        assertNotNull(method);
        assertEquals(methodName, method.getName());
    }

    @Test
    void executeWithCustomStateContextSubtype() {
        CustomContext context = new CustomContext();
        State state = new State("active");
        CustomContextHandler handler = new CustomContextHandler();

        MethodSelfTransition transition = new MethodSelfTransition("onEntry", handler);

        assertTrue(transition.doExecute(context, state));
        assertSame(context, handler.context);
    }

    @Test
    void executeWithCustomStateContextSubtypeAndState() {
        CustomContext context = new CustomContext();
        State state = new State("active");
        CustomContextHandler handler = new CustomContextHandler();

        MethodSelfTransition transition = new MethodSelfTransition("onExit", handler);

        assertTrue(transition.doExecute(context, state));
        assertSame(context, handler.context);
        assertSame(state, handler.state);
    }

    @Test
    void rejectUnsupportedParameterOrder() {
        CustomContext context = new CustomContext();
        State state = new State("active");
        UnsupportedSignatureHandler handler = new UnsupportedSignatureHandler();

        MethodSelfTransition transition = new MethodSelfTransition("wrongOrder", handler);

        assertFalse(transition.doExecute(context, state));
    }

    static class CustomContext extends DefaultStateContext {
        private static final long serialVersionUID = 1L;
    }

    static class CustomContextHandler {
        private StateContext context;
        private State state;

        public void onEntry(CustomContext context) {
            this.context = context;
        }

        public void onExit(CustomContext context, State state) {
            this.context = context;
            this.state = state;
        }
    }

    static class UnsupportedSignatureHandler {
        public void wrongOrder(State state, CustomContext context) {
            throw new AssertionError("Unsupported signature should not be invoked");
        }
    }
}