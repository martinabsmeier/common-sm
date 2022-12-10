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
import de.am.common.sm.context.StateContext;
import de.am.common.sm.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;

/**
 *
 */
public abstract class AbstractTransitionTest {

    protected State nextState;
    protected TestStateContext context;
    protected Target target;
    protected Method subsetAllArgsMethod1;
    protected Method subsetAllArgsMethod2;
    protected Event noArgsEvent;
    protected Event argsEvent;
    protected Object[] args;

    @BeforeAll
    public static void setLogger() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");
    }

    @BeforeEach
    void setUp() throws Exception {
        nextState = new State("next");
        target = mock(Target.class);
        subsetAllArgsMethod1 = Target.class.getMethod("subsetAllArgs", TestStateContext.class, B.class, A.class, Integer.TYPE);
        subsetAllArgsMethod2 = Target.class.getMethod("subsetAllArgs", Event.class, B.class, B.class, Boolean.TYPE);
        args = new Object[]{new A(), new B(), new C(), 627438, Boolean.TRUE};
        context = mock(TestStateContext.class);
        noArgsEvent = new Event("event", context, new Object[0]);
        argsEvent = new Event("event", context, args);
    }

    @AfterEach
    void tearDown() {
        nextState = null;
        target = null;
        subsetAllArgsMethod1 = null;
        subsetAllArgsMethod2 = null;
        args = null;
        context = null;
        noArgsEvent = null;
        argsEvent = null;
    }

    interface Target {
        void noArgs();

        void exactArgs(A a, B b, C c, int integer, boolean bool);

        void allArgs(Event event, StateContext ctxt, A a, B b, C c, int integer, boolean bool);

        void subsetExactArgs(A a, A b, int integer);

        void subsetAllArgs(TestStateContext ctxt, B b, A c, int integer);

        void subsetAllArgs(Event event, B b, B c, boolean bool);
    }

    interface TestStateContext extends StateContext {
    }

    static class A {
    }

    static class B extends A {
    }

    static class C extends B {
    }
}