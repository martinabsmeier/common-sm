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

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link AbstractStateContextLookup}.
 *
 * @author Martin Absmeier
 */
class AbstractStateContextLookupTest {

    @Test
    void constructorFactoryNull() {
        assertThrows(IllegalArgumentException.class, () -> new AbstractStateContextLookup(null){
            @Override
            protected StateContext lookup(Object eventArg) {
                return null;
            }
            @Override
            protected void store(Object eventArg, StateContext context) {

            }
            @Override
            protected boolean supports(Class<?> c) {
                return false;
            }
        });
    }

    @Test
    void testLookup() {
        Map<String, StateContext> map = new HashMap<>();
        AbstractStateContextLookup lookup = new AbstractStateContextLookup(new DefaultStateContextFactory()) {
            @Override
            protected boolean supports(Class<?> c) {
                return Map.class.isAssignableFrom(c);
            }

            @Override
            protected StateContext lookup(Object eventArg) {
                Map<String, StateContext> map = (Map<String, StateContext>) eventArg;
                return map.get("context");
            }

            @Override
            protected void store(Object eventArg, StateContext context) {
                Map<String, StateContext> map = (Map<String, StateContext>) eventArg;
                map.put("context", context);
            }
        };

        Object[] args1 = new Object[]{new Object(), map, new Object()};
        Object[] args2 = new Object[]{map, new Object()};
        StateContext sc = lookup.lookup(args1);
        assertSame(map.get("context"), sc);
        assertSame(map.get("context"), lookup.lookup(args1));
        assertSame(map.get("context"), lookup.lookup(args2));
    }
}