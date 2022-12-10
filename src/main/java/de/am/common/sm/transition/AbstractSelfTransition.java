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
package de.am.common.sm.transition;

import de.am.common.sm.State;
import de.am.common.sm.context.StateContext;

/**
 * Abstract {@link SelfTransition} implementation.
 *
 * @author Martin Absmeier
 */
public abstract class AbstractSelfTransition implements SelfTransition {

    /**
     * Executes this {@link SelfTransition}.
     * 
     * @param stateContext The context of the state.
     * @param state The state
     * @return <code>true</code> if the {@link SelfTransition} has been executed
     *         successfully
     */
    protected abstract boolean doExecute(StateContext stateContext, State state);

    @Override
    public boolean execute(StateContext stateContext, State state) {
        return doExecute(stateContext, state);
    }

}