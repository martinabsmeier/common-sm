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

import de.am.common.sm.annotation.State;
import de.am.common.sm.annotation.Transition;
import de.am.common.sm.annotation.Transitions;
import lombok.Getter;
import lombok.Synchronized;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.isNull;

/**
 * Example annotated handler for the {@link TapeDeck} state machine.
 * <p>
 * The class demonstrates how {@link State} and {@link Transition} annotations describe the runtime model that
 * {@link de.am.common.sm.StateMachineFactory} turns into a {@link de.am.common.sm.StateMachine}. The current state is
 * also mirrored in {@link #currentSate} so the example tests can assert behavior directly.
 * </p>
 *
 * @author Martin Absmeier
 */
public class TapeDeckManager {

    private static final Logger LOGGER = LogManager.getLogger(TapeDeckManager.class);
    private static TapeDeckManager INSTANCE;

    @State
    public static final String STATE_EMPTY = "Empty";
    @State
    public static final String STATE_LOADED = "Loaded";
    @State
    public static final String STATE_PLAYING = "Playing";
    @State
    public static final String STATE_PAUSED = "Paused";

    /**
     * Creates a new {@code TapeDeckManager} instance if necessary or returns the existing singleton.
     *
     * @return the TapeDeckManager instance
     */
    @Synchronized
    public static TapeDeckManager getInstance() {
        if (isNull(INSTANCE)) {
            INSTANCE = new TapeDeckManager();
        }
        return INSTANCE;
    }

    @Getter
    private String currentSate;

    /**
     * Handles the {@code load} event and moves the deck from {@link #STATE_EMPTY} to {@link #STATE_LOADED}.
     *
     * @param nameOfTape the name of the tape to load.
     */
    @Transition(on = "load", in = STATE_EMPTY, next = STATE_LOADED)
    public void loadTape(String nameOfTape) {
        currentSate = STATE_LOADED;
        LOGGER.info("Tape '{}' loaded", nameOfTape);
    }

    /**
     * Handles the {@code play} event from both the loaded and paused states and enters
     * {@link #STATE_PLAYING}.
     */
    @Transitions({ 
        @Transition(on = "play", in = STATE_LOADED, next = STATE_PLAYING),
        @Transition(on = "play", in = STATE_PAUSED, next = STATE_PLAYING) 
    })
    public void playTape() {
        currentSate = STATE_PLAYING;
        LOGGER.info("Playing tape");
    }

    /**
     * Handles the {@code pause} event and moves the deck into {@link #STATE_PAUSED}.
     */
    @Transition(on = "pause", in = STATE_PLAYING, next = STATE_PAUSED)
    public void pauseTape() {
        currentSate = STATE_PAUSED;
        LOGGER.info("Tape paused");
    }

    /**
     * Handles the {@code stop} event and returns to {@link #STATE_LOADED}.
     */
    @Transition(on = "stop", in = STATE_PLAYING, next = STATE_LOADED)
    public void stopTape() {
        currentSate = STATE_LOADED;
        LOGGER.info("Tape stopped");
    }

    /**
     * Handles the {@code eject} event and returns to {@link #STATE_EMPTY}.
     */
    @Transition(on = "eject", in = STATE_LOADED, next = STATE_EMPTY)
    public void ejectTape() {
        currentSate = STATE_EMPTY;
        LOGGER.info("Tape ejected");
    }

    // #################################################################################################################
    private TapeDeckManager() {
        this.currentSate = STATE_EMPTY;
    }
}