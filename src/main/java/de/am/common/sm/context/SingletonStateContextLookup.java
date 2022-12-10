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

/**
 * {@code  SingletonStateContextLookup} implementation which always returns the same {@link StateContext} instance.
 *
 * @author Martin Absmeier
 */
public class SingletonStateContextLookup implements StateContextLookup {
    private final StateContext context;

    /**
     * Creates a new instance which always returns the same
     * {@link DefaultStateContext} instance.
     */
    public SingletonStateContextLookup() {
        context = new DefaultStateContext();
    }

    /**
     * Creates a new instance which uses the specified {@link StateContextFactory}
     * to create the single instance.
     *
     * @param contextFactory the {@link StateContextFactory} to use to create the singleton instance.
     */
    public SingletonStateContextLookup(StateContextFactory contextFactory) {
        if (contextFactory == null) {
            throw new IllegalArgumentException("contextFactory");
        }
        context = contextFactory.create();
    }

    @Override
    public StateContext lookup(Object[] eventArgs) {
        return context;
    }
}