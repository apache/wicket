/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.form;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link LambdaChoiceRenderer}
 */
class LambdaChoiceRendererTest {

    private final Choice choice = new Choice();

    @Test
    void testLambdaChoiceRendererWithoutExpression() {
        final LambdaChoiceRenderer<Choice> renderer = new LambdaChoiceRenderer<>();
        assertEquals(String.valueOf(0), renderer.getIdValue(choice, 0));
        assertSame(choice, renderer.getDisplayValue(choice));
    }

    @Test
    void testLambdaChoiceRendererWithNullObjectAndWithoutExpression() {
        final LambdaChoiceRenderer<Choice> renderer = new LambdaChoiceRenderer<>();
        assertEquals(String.valueOf(0), renderer.getIdValue(null, 0));
        assertEquals("", renderer.getDisplayValue(null));
    }

    @Test
    void testLambdaChoiceRendererWithDisplayExpression() {
        final LambdaChoiceRenderer<Choice> renderer = new LambdaChoiceRenderer<>(Choice::getName);
        assertEquals(String.valueOf(0), renderer.getIdValue(choice, 0));
        assertEquals(choice.getName(), renderer.getDisplayValue(choice));
    }

    @Test
    void testLambdaChoiceRendererWithDisplayAndIdExpression() {
        final LambdaChoiceRenderer<Choice> renderer = new LambdaChoiceRenderer<>(Choice::getName, Choice::getId);
        assertEquals(String.valueOf(choice.getId()), renderer.getIdValue(choice, 0));
        assertEquals(choice.getName(), renderer.getDisplayValue(choice));
    }

    @Test
    void testLambdaChoiceRendererWithNullObjectAndDisplayAndIdExpression() {
        final LambdaChoiceRenderer<Choice> renderer = new LambdaChoiceRenderer<>(Choice::getName, Choice::getId);
        assertEquals("", renderer.getIdValue(null, 0));
        assertEquals("", renderer.getDisplayValue(null));
    }

    @Test
    void testLambdaChoiceRendererWithNullReturnValueAndDisplayAndIdExpression() {
        final LambdaChoiceRenderer<Choice> renderer = new LambdaChoiceRenderer<>(Choice::getNull, Choice::getNull);
        assertEquals("", renderer.getIdValue(choice, 0));
        assertEquals("", renderer.getDisplayValue(choice));
    }

    /**
     * Class for testing
     */
    private static class Choice {
        public Long getId() {
            return -1L;
        }

        public String getName() {
            return "name";
        }

        public Object getNull() {
            return null;
        }
    }

}
