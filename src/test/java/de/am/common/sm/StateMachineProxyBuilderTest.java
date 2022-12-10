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

import de.am.common.sm.annotation.OnEntry;
import de.am.common.sm.annotation.OnExit;
import de.am.common.sm.annotation.State;
import de.am.common.sm.annotation.Transition;
import de.am.common.sm.annotation.Transitions;
import de.am.common.sm.context.StateContext;
import de.am.common.sm.event.Event;
import de.am.common.sm.transition.MethodSelfTransition;
import de.am.common.sm.transition.MethodTransition;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link StateMachineProxyBuilder}.
 *
 * @author Martin Absmeier
 */
class StateMachineProxyBuilderTest {
    @Test
    void testReentrantStateMachine() {
        ReentrantStateMachineHandler handler = new ReentrantStateMachineHandler();

        de.am.common.sm.State s1 = new de.am.common.sm.State("s1");
        de.am.common.sm.State s2 = new de.am.common.sm.State("s2");
        de.am.common.sm.State s3 = new de.am.common.sm.State("s3");

        s1.addTransition(new MethodTransition("call1", s2, handler));
        s2.addTransition(new MethodTransition("call2", s3, handler));
        s3.addTransition(new MethodTransition("call3", handler));

        StateMachine sm = new StateMachine(new de.am.common.sm.State[]{s1, s2, s3}, "s1");
        Reentrant reentrant = new StateMachineProxyBuilder().create(Reentrant.class, sm);
        reentrant.call1(reentrant);
        assertTrue(handler.finished);
    }

    @Test
    void testTapeDeckStateMachine() {
        TapeDeckStateMachineHandler handler = new TapeDeckStateMachineHandler();

        de.am.common.sm.State parent = new de.am.common.sm.State("parent");
        de.am.common.sm.State s1 = new de.am.common.sm.State("s1", parent);
        de.am.common.sm.State s2 = new de.am.common.sm.State("s2", parent);
        de.am.common.sm.State s3 = new de.am.common.sm.State("s3", parent);
        de.am.common.sm.State s4 = new de.am.common.sm.State("s4", parent);
        de.am.common.sm.State s5 = new de.am.common.sm.State("s5", parent);

        parent.addTransition(new MethodTransition("*", "error", handler));
        s1.addTransition(new MethodTransition("insert", s2, "inserted", handler));
        s2.addTransition(new MethodTransition("start", s3, "playing", handler));
        s3.addTransition(new MethodTransition("stop", s4, "stopped", handler));
        s3.addTransition(new MethodTransition("pause", s5, "paused", handler));
        s4.addTransition(new MethodTransition("eject", s1, "ejected", handler));
        s5.addTransition(new MethodTransition("pause", s3, "playing", handler));

        s2.addOnEntrySelfTransaction(new MethodSelfTransition("onEntryS2", handler));
        s2.addOnExitSelfTransaction(new MethodSelfTransition("onExitS2", handler));

        s3.addOnEntrySelfTransaction(new MethodSelfTransition("onEntryS3", handler));
        s3.addOnExitSelfTransaction(new MethodSelfTransition("onExitS3", handler));

        s4.addOnEntrySelfTransaction(new MethodSelfTransition("onEntryS4", handler));
        s4.addOnExitSelfTransaction(new MethodSelfTransition("onExitS4", handler));

        StateMachine sm = new StateMachine(new de.am.common.sm.State[]{s1, s2, s3, s4, s5}, "s1");
        TapeDeck player = new StateMachineProxyBuilder().create(TapeDeck.class, sm);
        player.insert("Kings of convenience - Riot on an empty street");
        player.start();
        player.pause();
        player.pause();
        player.eject();
        player.stop();
        player.eject();

        LinkedList<String> messages = handler.messages;
        assertEquals("Tape 'Kings of convenience - Riot on an empty street' inserted", messages.removeFirst());
        assertEquals("S2 entered", messages.removeFirst());
        assertEquals("Playing", messages.removeFirst());
        assertEquals("S2 exited", messages.removeFirst());
        assertEquals("S3 entered with stateContext", messages.removeFirst());
        assertEquals("Paused", messages.removeFirst());
        assertEquals("S3 exited with stateContext", messages.removeFirst());
        assertEquals("Playing", messages.removeFirst());
        assertEquals("S3 entered with stateContext", messages.removeFirst());
        assertEquals("Error: Cannot eject at this time", messages.removeFirst());
        assertEquals("Stopped", messages.removeFirst());
        assertEquals("S3 exited with stateContext", messages.removeFirst());
        assertEquals("S4 entered with stateContext and state", messages.removeFirst());
        assertEquals("Tape ejected", messages.removeFirst());
        assertEquals("S4 exited with stateContext and state", messages.removeFirst());

        assertTrue(messages.isEmpty());
    }

    @Test
    void testTapeDeckStateMachineAnnotations() {
        TapeDeckStateMachineHandler handler = new TapeDeckStateMachineHandler();

        StateMachine sm = StateMachineFactory.create(Transition.class).create(TapeDeckStateMachineHandler.S1, handler);

        TapeDeck player = new StateMachineProxyBuilder().create(TapeDeck.class, sm);
        player.insert("Kings of convenience - Riot on an empty street");
        player.start();
        player.pause();
        player.pause();
        player.eject();
        player.stop();
        player.eject();

        LinkedList<String> messages = handler.messages;
        assertEquals("Tape 'Kings of convenience - Riot on an empty street' inserted", messages.removeFirst());
        assertEquals("S2 entered", messages.removeFirst());
        assertEquals("Playing", messages.removeFirst());
        assertEquals("S2 exited", messages.removeFirst());
        assertEquals("S3 entered with stateContext", messages.removeFirst());
        assertEquals("Paused", messages.removeFirst());
        assertEquals("S3 exited with stateContext", messages.removeFirst());
        assertEquals("Playing", messages.removeFirst());
        assertEquals("S3 entered with stateContext", messages.removeFirst());
        assertEquals("Error: Cannot eject at this time", messages.removeFirst());
        assertEquals("Stopped", messages.removeFirst());
        assertEquals("S3 exited with stateContext", messages.removeFirst());
        assertEquals("S4 entered with stateContext and state", messages.removeFirst());
        assertEquals("Tape ejected", messages.removeFirst());
        assertEquals("S4 exited with stateContext and state", messages.removeFirst());

        assertTrue(messages.isEmpty());
    }

    public interface Reentrant {
        void call1(Reentrant proxy);

        void call2(Reentrant proxy);

        void call3(Reentrant proxy);
    }

    public static class ReentrantStateMachineHandler {
        private boolean finished = false;

        public void call1(Reentrant proxy) {
            proxy.call2(proxy);
        }

        public void call2(Reentrant proxy) {
            proxy.call3(proxy);
        }

        public void call3(Reentrant proxy) {
            finished = true;
        }
    }

    public interface TapeDeck {
        void insert(String name);

        void eject();

        void start();

        void pause();

        void stop();
    }

    public static class TapeDeckStateMachineHandler {
        @State
        public static final String PARENT = "parent";

        @State(PARENT)
        public static final String S1 = "s1";

        @State(PARENT)
        public static final String S2 = "s2";

        @State(PARENT)
        public static final String S3 = "s3";

        @State(PARENT)
        public static final String S4 = "s4";

        @State(PARENT)
        public static final String S5 = "s5";

        private final LinkedList<String> messages = new LinkedList<>();

        @OnEntry(S2)
        public void onEntryS2() {
            messages.add("S2 entered");
        }

        @OnExit(S2)
        public void onExitS2() {
            messages.add("S2 exited");
        }

        @OnEntry(S3)
        public void onEntryS3(StateContext stateContext) {
            messages.add("S3 entered with stateContext");
        }

        @OnExit(S3)
        public void onExitS3(StateContext stateContext) {
            messages.add("S3 exited with stateContext");
        }

        @OnEntry(S4)
        public void onEntryS4(StateContext stateContext, de.am.common.sm.State state) {
            messages.add("S4 entered with stateContext and state");
        }

        @OnExit(S4)
        public void onExitS4(StateContext stateContext, de.am.common.sm.State state) {
            messages.add("S4 exited with stateContext and state");
        }

        @Transition(on = "insert", in = "s1", next = "s2")
        public void inserted(String name) {
            messages.add("Tape '" + name + "' inserted");
        }

        @Transition(on = "eject", in = "s4", next = "s1")
        public void ejected() {
            messages.add("Tape ejected");
        }

        @Transitions({@Transition(on = "start", in = "s2", next = "s3"),
            @Transition(on = "pause", in = "s5", next = "s3")})
        public void playing() {
            messages.add("Playing");
        }

        @Transition(on = "pause", in = "s3", next = "s5")
        public void paused() {
            messages.add("Paused");
        }

        @Transition(on = "stop", in = "s3", next = "s4")
        public void stopped() {
            messages.add("Stopped");
        }

        @Transition(on = "*", in = "parent")
        public void error(Event event) {
            messages.add("Error: Cannot " + event.getId() + " at this time");
        }
    }
}