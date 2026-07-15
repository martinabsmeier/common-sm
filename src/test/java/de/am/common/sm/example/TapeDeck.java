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

/**
 * Simple command interface used by the example state machine.
 * <p>
 * Each method name doubles as an event id when the interface is wrapped by {@link de.am.common.sm.StateMachineProxyBuilder}.
 * </p>
 *
 * @author Martin Absmeier
 */
public interface TapeDeck {

    /**
     * Removes the currently loaded tape.
     */
    void eject();

    /**
     * Loads a tape into the deck.
     *
     * @param nameOfTape the human-readable tape name.
     */
    void load(String nameOfTape);

    /**
     * Pauses playback.
     */
    void pause();

    /**
     * Continues playback from the current position.
     */
    void play();

    /**
     * Starts the deck.
     */
    void start();

    /**
     * Stops playback and returns to the loaded state.
     */
    void stop();
}
