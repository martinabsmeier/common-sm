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
package de.am.common.sm.context;

import static java.util.Objects.isNull;

/**
 * Abstract {@link StateContextLookup} implementation. The {@link #lookup(Object[])} method will
 * loop through the event arguments and call the {@link #supports(Class)} method for each of them.
 * The first argument that this method returns <code>true</code> for will be passed to the abstract
 * {@link #lookup(Object)} method which should try to extract a {@link StateContext} from the
 * argument. If none is found a new {@link StateContext} will be created and stored in the event
 * argument using the {@link #store(Object, StateContext)} method.
 *
 * @author Martin Absmeier
 */
public abstract class AbstractStateContextLookup implements StateContextLookup {

    private final StateContextFactory contextFactory;

    /**
     * Creates a new instance which uses the specified {@link StateContextFactory} to create
     * {@link StateContext} objects.
     *
     * @param contextFactory the factory.
     */
    protected AbstractStateContextLookup(StateContextFactory contextFactory) {
        if (isNull(contextFactory)) {
            throw new IllegalArgumentException("contextFactory");
        }
        this.contextFactory = contextFactory;
    }

    @Override
    public StateContext lookup(Object[] eventArgs) {
        for (Object eventArg : eventArgs) {
            if (supports(eventArg.getClass())) {
                StateContext sc = lookup(eventArg);
                if (isNull(sc)) {
                    sc = contextFactory.create();
                    store(eventArg, sc);
                }
                return sc;
            }
        }
        return null;
    }

    /**
     * Extracts a {@link StateContext} from the specified event argument which is an instance of a
     * class {@link #supports(Class)} returns <code>true</code> for.
     *
     * @param eventArg the event argument.
     * @return the {@link StateContext}.
     */
    protected abstract StateContext lookup(Object eventArg);

    /**
     * Stores a new {@link StateContext} in the specified event argument which is an instance of a
     * class {@link #supports(Class)} returns <code>true</code> for.
     *
     * @param eventArg the event argument.
     * @param context  the {@link StateContext} to be stored.
     */
    protected abstract void store(Object eventArg, StateContext context);

    /**
     * Must return <code>true</code> for any {@link Class} that this {@link StateContextLookup} can
     * use to store and lookup {@link StateContext} objects.
     *
     * @param c the class.
     * @return <code>true</code> or <code>false</code>.
     */
    protected abstract boolean supports(Class<?> c);

}