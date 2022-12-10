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

import de.am.common.sm.event.Event;
import de.am.common.sm.transition.SelfTransition;
import de.am.common.sm.transition.Transition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a state in a {@link StateMachine}. Normally you wouldn't create instances of this class directly but rather
 * use the {@link de.am.common.sm.annotation.State} annotation to define your states and then let {@link StateMachineFactory}
 * create a {@link StateMachine} for you.
 * <p>
 * {@link State}s inherits {@link Transition}s from their parent. A {@link State} can override any of the parents {@link Transition}s.
 * When an {@link Event} is processed the {@link Transition}s of the current {@link State} will be searched for a {@link Transition}
 * which can handle the event. If none is found the {@link State}'s parent will be searched and so on.
 * </p>
 *
 * @author Martin Absmeier
 */
@Data
public class State {

    private final String id;
    @EqualsAndHashCode.Exclude
    private final State parent;
    @EqualsAndHashCode.Exclude
    private final List<TransitionHolder> transitionHolders = new ArrayList<>();
    @EqualsAndHashCode.Exclude
    private List<Transition> transitions = Collections.emptyList();
    @EqualsAndHashCode.Exclude
    private final List<SelfTransition> onEntries = new ArrayList<>();
    @EqualsAndHashCode.Exclude
    private final List<SelfTransition> onExits = new ArrayList<>();

    /**
     * Creates a new {@link State} with the specified id.
     *
     * @param id the unique id of this {@link State}.
     */
    public State(String id) {
        this(id, null);
    }

    /**
     * Creates a new {@link State} with the specified id and parent.
     *
     * @param id     the unique id of this {@link State}.
     * @param parent the parent {@link State}.
     */
    public State(String id, State parent) {
        this.id = id;
        this.parent = parent;
    }


    /**
     * Returns an unmodifiable {@link List} of {@link Transition}s going out from this {@link State}.
     *
     * @return the {@link Transition}s.
     */
    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    /**
     * Returns an unmodifiable {@link List} of entry {@link SelfTransition}s
     *
     * @return the {@link SelfTransition}s.
     */
    public List<SelfTransition> getOnEntrySelfTransitions() {
        return Collections.unmodifiableList(onEntries);
    }

    /**
     * Returns an unmodifiable {@link List} of exit {@link SelfTransition}s
     *
     * @return the {@link SelfTransition}s.
     */
    public List<SelfTransition> getOnExitSelfTransitions() {
        return Collections.unmodifiableList(onExits);
    }

    /**
     * Adds an entry {@link SelfTransition} to this {@link State}
     *
     * @param onEntrySelfTransaction the {@link SelfTransition} to add.
     * @return this {@link State}.
     */
    public State addOnEntrySelfTransaction(SelfTransition onEntrySelfTransaction) {
        Objects.requireNonNull(onEntrySelfTransaction, "NULL is not permitted as value for 'onEntrySelfTransaction' parameter.");

        onEntries.add(onEntrySelfTransaction);
        return this;
    }

    /**
     * Adds an exit {@link SelfTransition} to this {@link State}
     *
     * @param onExitSelfTransaction The {@link SelfTransition} to add.
     * @return this {@link State}.
     */
    public State addOnExitSelfTransaction(SelfTransition onExitSelfTransaction) {
        Objects.requireNonNull(onExitSelfTransaction, "NULL is not permitted as value for 'onExitSelfTransaction' parameter.");

        onExits.add(onExitSelfTransaction);
        return this;
    }

    private void updateTransitions() {
        transitions = new ArrayList<>(transitionHolders.size());
        for (TransitionHolder holder : transitionHolders) {
            transitions.add(holder.transition);
        }
    }

    /**
     * Adds an outgoing {@link Transition} to this {@link State} with weight 0.
     *
     * @param transition the {@link Transition} to add.
     * @return this {@link State}.
     * @see #addTransition(Transition, int)
     */
    public State addTransition(Transition transition) {
        return addTransition(transition, 0);
    }

    /**
     * Adds an outgoing {@link Transition} to this {@link State} with the specified weight. The higher the weight the
     * less important a {@link Transition} is. If two {@link Transition}s match the same {@link Event} the
     * {@link Transition} with the lower weight will be executed.
     *
     * @param transition the {@link Transition} to add.
     * @param weight
     * @return this {@link State}.
     */
    public State addTransition(Transition transition, int weight) {
        Objects.requireNonNull(transition, "NULL is not permitted as value for 'transition' parameter.");

        transitionHolders.add(new TransitionHolder(transition, weight));
        Collections.sort(transitionHolders);
        updateTransitions();

        return this;
    }

    @Override
    public String toString() {
        return "id: ".concat(id);
    }

    @EqualsAndHashCode
    private static class TransitionHolder implements Comparable<TransitionHolder> {

        @EqualsAndHashCode.Exclude
        Transition transition;
        int weight;

        TransitionHolder(Transition transition, int weight) {
            this.transition = transition;
            this.weight = weight;
        }

        @Override
        public int compareTo(TransitionHolder other) {
            return Integer.compare(weight, other.weight);
        }

    }
}