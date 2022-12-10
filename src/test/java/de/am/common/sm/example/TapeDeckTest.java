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
package de.am.common.sm.example;

import de.am.common.sm.StateMachine;
import de.am.common.sm.StateMachineFactory;
import de.am.common.sm.StateMachineProxyBuilder;
import de.am.common.sm.annotation.Transition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JUnit test cases of {@link TapeDeck}.
 *
 * @author Martin Absmeier
 */
class TapeDeckTest {

    private TapeDeck deck;
    private TapeDeckManager manager;

    @BeforeEach
    void setUp() {
        manager = TapeDeckManager.getInstance();
        StateMachineFactory factory = StateMachineFactory.create(Transition.class);
        StateMachine sm = factory.create(TapeDeckManager.STATE_EMPTY, manager);
        deck = new StateMachineProxyBuilder().create(TapeDeck.class, sm);
    }

    @Test
    void testInit() {
        assertNotNull(deck);
    }

    @Test
    void testTapeDeck() {
        deck.load("The Knife - Silent Shout");
        assertEquals("Loaded", manager.getCurrentSate(), "We expect 'Loaded' as state.");
        deck.play();
        deck.pause();
        deck.play();
        deck.stop();
        deck.eject();
        assertEquals("Empty", manager.getCurrentSate(), "We expect 'Empty' as state.");
    }
}