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

import de.am.common.sm.annotation.State;
import de.am.common.sm.annotation.Transitions;
import de.am.common.sm.exception.StateMachineCreationException;
import de.am.common.sm.transition.MethodTransition;
import de.am.common.sm.transition.Transition;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link StateMachineFactory}.
 *
 * @author Martin Absmeier
 */
class StateMachineFactoryTest {
    private Method barInA;

    private Method error;

    private Method fooInA;

    private Method fooInB;

    private Method barInC;

    private Method fooOrBarInCOrFooInD;

    @BeforeEach
    void setUp() throws Exception {
        barInA = States.class.getDeclaredMethod("barInA");
        error = States.class.getDeclaredMethod("error");
        fooInA = States.class.getDeclaredMethod("fooInA");
        fooInB = States.class.getDeclaredMethod("fooInB");
        barInC = States.class.getDeclaredMethod("barInC");
        fooOrBarInCOrFooInD = States.class.getDeclaredMethod("fooOrBarInCOrFooInD");
    }

    @AfterEach
    void tearDown() throws Exception {
        barInA = null;
        error = null;
        fooInA = null;
        fooInB = null;
        barInC = null;
        fooOrBarInCOrFooInD = null;
    }

    @Test
    void testCreate() throws Exception {
        States states = new States();
        StateMachine sm = StateMachineFactory.create(de.am.common.sm.annotation.Transition.class).create(States.A, states);

        de.am.common.sm.State a = sm.getState(States.A);
        de.am.common.sm.State b = sm.getState(States.B);
        de.am.common.sm.State c = sm.getState(States.C);
        de.am.common.sm.State d = sm.getState(States.D);

        assertEquals(States.A, a.getId());
        assertNull(a.getParent());
        assertEquals(States.B, b.getId());
        assertSame(a, b.getParent());
        assertEquals(States.C, c.getId());
        assertSame(b, c.getParent());
        assertEquals(States.D, d.getId());
        assertSame(a, d.getParent());

        List<Transition> trans = a.getTransitions();
        assertEquals(3, trans.size());
        assertEquals(new MethodTransition("bar", barInA, states), trans.get(0));
        assertEquals(new MethodTransition("*", error, states), trans.get(1));
        assertEquals(new MethodTransition("foo", b, fooInA, states), trans.get(2));

        trans = b.getTransitions();
        assertEquals(1, trans.size());
        assertEquals(new MethodTransition("foo", c, fooInB, states), trans.get(0));

        trans = c.getTransitions();
        assertEquals(3, trans.size());
        assertEquals(new MethodTransition("bar", a, barInC, states), trans.get(0));
        assertEquals(new MethodTransition("foo", d, fooOrBarInCOrFooInD, states), trans.get(1));
        assertEquals(new MethodTransition("bar", d, fooOrBarInCOrFooInD, states), trans.get(2));

        trans = d.getTransitions();
        assertEquals(1, trans.size());
        assertEquals(new MethodTransition("foo", fooOrBarInCOrFooInD, states), trans.get(0));
    }

    @Test
    void testCreateStates() throws Exception {
        de.am.common.sm.State[] states = StateMachineFactory.createStates(StateMachineFactory.getFields(States.class));
        assertEquals(States.A, states[0].getId());
        assertNull(states[0].getParent());
        assertEquals(States.B, states[1].getId());
        assertEquals(states[0], states[1].getParent());
        assertEquals(States.C, states[2].getId());
        assertEquals(states[1], states[2].getParent());
        assertEquals(States.D, states[3].getId());
        assertEquals(states[0], states[3].getParent());
    }

    @Test
    void testCreateStatesMissingParents() {
        List<Field> fields = StateMachineFactory.getFields(StatesWithMissingParents.class);
        assertThrows(StateMachineCreationException.class, () -> StateMachineFactory.createStates(fields));

    }

    public static class States {
        @State
        protected static final String A = "a";

        @State(A)
        protected static final String B = "b";

        @State(B)
        protected static final String C = "c";

        @State(A)
        protected static final String D = "d";

        @de.am.common.sm.annotation.Transition(on = "bar", in = A)
        protected void barInA() {
        }

        @de.am.common.sm.annotation.Transition(on = "bar", in = C, next = A)
        protected void barInC() {
        }

        @de.am.common.sm.annotation.Transition(in = A)
        protected void error() {
        }

        @de.am.common.sm.annotation.Transition(on = "foo", in = A, next = B)
        protected void fooInA() {
        }

        @de.am.common.sm.annotation.Transition(on = "foo", in = B, next = C)
        protected void fooInB() {
        }

        @Transitions({@de.am.common.sm.annotation.Transition(on = {"foo", "bar"}, in = C, next = D), @de.am.common.sm.annotation.Transition(on = "foo", in = D)})
        protected void fooOrBarInCOrFooInD() {
        }

    }

    @Getter
    public static class StatesWithMissingParents {
        @State("b")
        public static final String A = "a";

        @State("c")
        public static final String B = "b";

        @State("d")
        public static final String C = "c";

        @State("e")
        public static final String D = "d";
    }
}