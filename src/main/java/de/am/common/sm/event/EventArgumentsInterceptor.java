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

import de.am.common.sm.StateMachine;

/**
 * Intercepts the {@link Event} arguments before the {@link Event} is passed to the {@link StateMachine} and allows for
 * the arguments to be modified. This is for advanced uses only.
 *
 * @author Martin Absmeier
 */
public interface EventArgumentsInterceptor {

    /**
     * Modifies the specified array of event arguments.
     *
     * @param arguments the original arguments.
     * @return the new arguments. Should return the original array if no modification is needed.
     */
    Object[] modify(Object[] arguments);

}