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
package org.apache.wicket.markup.head;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JavaScriptDataBlockTypeTest
{
    @Test
    void cannotBeCreatedWithNullType()
    {
        String message = assertThrows(IllegalArgumentException.class,
                () -> new JavaScriptDataBlockType(null))
                .getMessage();
        assertEquals("Argument 'type' may not be null.", message);
    }

    @Test
    void cannotBeCreatedWithTypeThatIsNotAMediaType()
    {
        String message = assertThrows(IllegalArgumentException.class,
                () -> new JavaScriptDataBlockType("Not a media type"))
                .getMessage();
        assertEquals("'type' must be a media type (that is: start with a type followed by a slash).", message);
    }

    @Test
    void cannotBeCreatedWithTypeThatIsAJavaScriptMediaType()
    {
        String message = assertThrows(IllegalArgumentException.class,
                () -> new JavaScriptDataBlockType("text/javascript"))
                .getMessage();
        assertEquals("'type' may not be a JavaScript media type.", message);
    }

    @Test
    void creationWithTypeThatIsNotAJavaScriptMediaTypeSucceeds()
    {
        assertDoesNotThrow(() -> new JavaScriptDataBlockType("application/json"));
    }
}
