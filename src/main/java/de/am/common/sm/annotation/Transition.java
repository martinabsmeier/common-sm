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
package de.am.common.sm.annotation;

import de.am.common.sm.StateMachine;
import de.am.common.sm.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code Transition} annotation is used on methods to indicate that the method handles a specific kind of event when
 * in a specific state.
 *
 * @author Martin Absmeier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TransitionAnnotation(Transitions.class)
public @interface Transition {

    String SELF = "__self__";

    /**
     * Specifies the ids of one or more events handled by the annotated method. If not specified the
     * handler method will be executed for any event.
     *
     * @return the new state
     */
    String[] on() default Event.WILDCARD_EVENT_ID;

    /**
     * The id of the state or states that this handler applies to. Must be specified.
     *
     * @return the new state
     */
    String[] in();

    /**
     * The id of the state the {@link StateMachine} should move to next after executing the
     * annotated method. If not specified the {@link StateMachine} will remain in the same state.
     *
     * @return the new state
     */
    String next() default SELF;

    /**
     * The weight used to order handler annotations which match the same event in the same state.
     * Transitions with lower weight will be matched first. The default weight is 0.
     *
     * @return the new state
     */
    int weight() default 0;

}