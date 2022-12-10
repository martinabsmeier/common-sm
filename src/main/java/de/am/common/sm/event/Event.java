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
package de.am.common.sm.event;

import de.am.common.sm.context.StateContext;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;

/**
 * Represents an event which typically corresponds to a method call on a proxy. An event has an id and zero or more
 * arguments typically corresponding to the method arguments.
 *
 * @author Martin Absmeier
 */
public class Event implements Serializable {
    private static final long serialVersionUID = -7224996357207464822L;

    public static final String WILDCARD_EVENT_ID = "*";
	@Getter
    private final transient Object id;
	@Getter
    private final StateContext context;
	@Getter
    private final transient Object[] arguments;

    /**
     * Creates a new {@link Event} with the specified id and no arguments.
     *
     * @param id      the event id.
     * @param context the {@link StateContext} the event was triggered for.
     */
    public Event(Object id, StateContext context) {
        this(id, context, new Object[0]);
    }

    /**
     * Creates a new {@link Event} with the specified id and arguments.
     *
     * @param id        the event id.
     * @param context   the {@link StateContext} the event was triggered for.
     * @param arguments the event arguments.
     */
    public Event(Object id, StateContext context, Object[] arguments) {
		requireNonNull(id, "NULL is not permitted value for 'id' parameter.");
		requireNonNull(context, "NULL is not permitted value for 'context' parameter.");
		requireNonNull(arguments, "NULL is not permitted value for 'arguments' parameter.");

        this.id = id;
        this.context = context;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "id: ".concat(valueOf(id)).concat(" | ")
            .concat("context: ").concat(valueOf(context)).concat(" | ")
            .concat("arguments: ").concat(Arrays.toString(arguments));
    }
}