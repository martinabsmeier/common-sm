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

import de.am.common.sm.exception.BreakAndCallException;
import de.am.common.sm.exception.BreakAndContinueException;
import de.am.common.sm.exception.BreakAndGotoException;
import de.am.common.sm.exception.BreakAndReturnException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit test cases of {@link StateControl} class.
 *
 * @author Martin Absmeier
 */
class StateControlTest {

    @Test
    void breakAndContinue() {
        assertThrows(BreakAndContinueException.class, StateControl::breakAndContinue);
    }

    @Test
    void breakAndGotoNow() {
        assertThrows(BreakAndGotoException.class, () -> StateControl.breakAndGotoNow("theState"));
    }

    @Test
    void breakAndGotoNext() {
        assertThrows(BreakAndGotoException.class, () -> StateControl.breakAndGotoNext("theState"));
    }

    @Test
    void breakAndCallNowOneParameter() {
        assertThrows(BreakAndCallException.class, () -> StateControl.breakAndCallNow("theState"));
    }

    @Test
    void breakAndCallNowTwoParameter() {
        assertThrows(BreakAndCallException.class, () -> StateControl.breakAndCallNow("theState", "returnTo"));
    }


    @Test
    void breakAndCallNextOneParameter() {
        assertThrows(BreakAndCallException.class, () -> StateControl.breakAndCallNext("theState"));
    }

    @Test
    void breakAndCallNextTwoParameter() {
        assertThrows(BreakAndCallException.class, () -> StateControl.breakAndCallNext("theState", "returnTo"));
    }

    @Test
    void breakAndReturnNow() {
        assertThrows(BreakAndReturnException.class, StateControl::breakAndReturnNow);
    }

    @Test
    void breakAndReturnNext() {
        assertThrows(BreakAndReturnException.class, StateControl::breakAndReturnNext);
    }
}