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

    private static final TestClass TEST_CLASS = new TestClass();

    @Test
    void testLambdaChoiceRendererWithoutExpression() {
        final LambdaChoiceRenderer<TestClass> renderer = new LambdaChoiceRenderer<>();
        assertEquals(String.valueOf(0), renderer.getIdValue(TEST_CLASS, 0));
        assertSame(TEST_CLASS, renderer.getDisplayValue(TEST_CLASS));
    }

    @Test
    void testLambdaChoiceRendererWithNullObjectAndWithoutExpression() {
        final LambdaChoiceRenderer<TestClass> renderer = new LambdaChoiceRenderer<>();
        assertEquals(String.valueOf(0), renderer.getIdValue(null, 0));
        assertEquals("", renderer.getDisplayValue(null));
    }

    @Test
    void testLambdaChoiceRendererWithDisplayExpression() {
        final LambdaChoiceRenderer<TestClass> renderer = new LambdaChoiceRenderer<>(TestClass::getName);
        assertEquals(String.valueOf(0), renderer.getIdValue(TEST_CLASS, 0));
        assertEquals(TEST_CLASS.getName(), renderer.getDisplayValue(TEST_CLASS));
    }

    @Test
    void testLambdaChoiceRendererWithDisplayAndIdExpression() {
        final LambdaChoiceRenderer<TestClass> renderer = new LambdaChoiceRenderer<>(TestClass::getName, TestClass::getId);
        assertEquals(String.valueOf(TEST_CLASS.getId()), renderer.getIdValue(TEST_CLASS, 0));
        assertEquals(TEST_CLASS.getName(), renderer.getDisplayValue(TEST_CLASS));
    }

    @Test
    void testLambdaChoiceRendererWithNullObjectAndDisplayAndIdExpression() {
        final LambdaChoiceRenderer<TestClass> renderer = new LambdaChoiceRenderer<>(TestClass::getName, TestClass::getId);
        assertEquals("", renderer.getIdValue(null, 0));
        assertEquals("", renderer.getDisplayValue(null));
    }

    @Test
    void testLambdaChoiceRendererWithNullReturnValueAndDisplayAndIdExpression() {
        final LambdaChoiceRenderer<TestClass> renderer = new LambdaChoiceRenderer<>(TestClass::getNull, TestClass::getNull);
        assertEquals("", renderer.getIdValue(TEST_CLASS, 0));
        assertEquals("", renderer.getDisplayValue(TEST_CLASS));
    }

    /**
     * Class for testing
     */
    private static class TestClass {
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
