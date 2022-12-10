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

import de.am.common.sm.State;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract {@link StateContext} which uses a {@link Map} to store the attributes.
 *
 * @author Martin Absmeier
 */
@Data
public abstract class AbstractStateContext implements StateContext {

    private static final long serialVersionUID = -9163548435437416302L;

    private transient State currentState = null;

    private transient Map<Object, Object> attributes = new HashMap<>();

    @Override
    public Object getAttribute(Object key) {
        return getAttributes().get(key);
    }

    @Override
    public void setAttribute(Object key, Object value) {
        getAttributes().put(key, value);
    }

}