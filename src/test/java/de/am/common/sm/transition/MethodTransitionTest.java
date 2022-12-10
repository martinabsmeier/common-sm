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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test cases of {@link MethodTransition} class.
 *
 * @author Martin Absmeier
 */
class MethodTransitionTest extends  AbstractTransitionTest{

    @Test
    void testExecuteWrongEventId() {
        MethodTransition t = new MethodTransition("otherEvent", nextState, "noArgs", target);
        assertFalse(t.execute(noArgsEvent));
    }

    @Test
    void testExecuteNoArgsMethodOnNoArgsEvent() {
        target.noArgs();
        MethodTransition t = new MethodTransition("event", nextState, "noArgs", target);
        assertTrue(t.execute(noArgsEvent));
    }

    @Test
    void testExecuteNoArgsMethodOnArgsEvent() {
        target.noArgs();
        MethodTransition t = new MethodTransition("event", nextState, "noArgs", target);
        assertTrue(t.execute(argsEvent));
    }

    @Test
    void testExecuteExactArgsMethodOnNoArgsEvent() {
        MethodTransition t = new MethodTransition("event", nextState, "exactArgs", target);
        assertFalse(t.execute(noArgsEvent));
    }

    @Test
    void testExecuteExactArgsMethodOnArgsEvent() {
        target.exactArgs((A) args[0], (B) args[1], (C) args[2], ((Integer) args[3]), ((Boolean) args[4]));
        MethodTransition t = new MethodTransition("event", nextState, "exactArgs", target);
        assertTrue(t.execute(argsEvent));
    }

    @Test
    void testExecuteSubsetExactArgsMethodOnNoArgsEvent() {
        MethodTransition t = new MethodTransition("event", nextState, "subsetExactArgs", target);
        assertFalse(t.execute(noArgsEvent));
    }

    @Test
    void testExecuteSubsetExactArgsMethodOnArgsEvent() {
        target.subsetExactArgs((A) args[0], (A) args[1], ((Integer) args[3]));
        MethodTransition t = new MethodTransition("event", nextState, "subsetExactArgs", target);
        assertTrue(t.execute(argsEvent));
    }

    @Test
    void testExecuteAllArgsMethodOnArgsEvent() {
        target.allArgs(argsEvent, context, (A) args[0], (B) args[1], (C) args[2], ((Integer) args[3]), ((Boolean) args[4]));
        MethodTransition t = new MethodTransition("event", nextState, "allArgs", target);
        assertTrue(t.execute(argsEvent));
    }

    @Test
    void testExecuteSubsetAllArgsMethod1OnArgsEvent() {
        target.subsetAllArgs(context, (B) args[1], (A) args[2], ((Integer) args[3]));
        MethodTransition t = new MethodTransition("event", nextState, subsetAllArgsMethod1, target);
        assertTrue(t.execute(argsEvent));
    }

    @Test
    void testExecuteSubsetAllArgsMethod2OnArgsEvent() {
        target.subsetAllArgs(argsEvent, (B) args[1], (B) args[2], ((Boolean) args[4]));
        MethodTransition t = new MethodTransition("event", nextState, subsetAllArgsMethod2, target);
        assertTrue(t.execute(argsEvent));
    }
}