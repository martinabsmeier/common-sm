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

import java.lang.reflect.Method;

import de.am.common.sm.context.StateContext;

/**
 * Default {@link EventFactory} implementation. Uses the method's name as event id.
 * 
 * @author Martin Absmeier
 */
public class DefaultEventFactory implements EventFactory {

    @Override
    public Event create(StateContext context, Method method, Object[] arguments) {
        return new Event(method.getName(), context, arguments);
    }
}