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

import de.am.common.sm.transition.Transition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link State}.
 *
 * @author Martin Absmeier
 */
class StateTest {
    State state;

    Transition transition1;

    Transition transition2;

    Transition transition3;

    @BeforeEach
    void setUp() {
        state = new State("test");
        transition1 = Mockito.mock(Transition.class);
        transition2 = Mockito.mock(Transition.class);
        transition3 = Mockito.mock(Transition.class);
    }

    @AfterEach
    void tearDown() {
        state = null;
        transition1 = null;
        transition2 = null;
        transition3 = null;
    }

    @Test
    void testAddFirstTransition() {
        assertTrue(state.getTransitions().isEmpty());
        state.addTransition(transition1);
        assertFalse(state.getTransitions().isEmpty());
        assertEquals(1, state.getTransitions().size());
        assertSame(transition1, state.getTransitions().get(0));
    }

    @Test
    void testUnweightedTransitions() {
        assertTrue(state.getTransitions().isEmpty());
        state.addTransition(transition1);
        state.addTransition(transition2);
        state.addTransition(transition3);
        assertEquals(3, state.getTransitions().size());
        assertSame(transition1, state.getTransitions().get(0));
        assertSame(transition2, state.getTransitions().get(1));
        assertSame(transition3, state.getTransitions().get(2));
    }

    @Test
    void testWeightedTransitions() {
        assertTrue(state.getTransitions().isEmpty());
        state.addTransition(transition1, 10);
        state.addTransition(transition2, 5);
        state.addTransition(transition3, 7);
        assertEquals(3, state.getTransitions().size());
        assertSame(transition2, state.getTransitions().get(0));
        assertSame(transition3, state.getTransitions().get(1));
        assertSame(transition1, state.getTransitions().get(2));
    }

    @Test
    void testAddTransitionReturnsSelf() {
        assertSame(state, state.addTransition(transition1));
    }

    @Test
    void testAddNullTransitionThrowsException() {
        assertThrows(NullPointerException.class, () -> state.addTransition(null));
    }
}