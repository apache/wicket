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
package org.apache.wicket.csp;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CSPDirectiveTest {

    @Test
    void scriptSrcAttrAndStyleSrcAttributesSupportValues() 
    {
        CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of());
        CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_INLINE, List.of());
        CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of());
        CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_INLINE, List.of());
    }

    @Test
    void scriptSrcAttrAndStyleSrcAttributesOnlySupportOneValue() 
    {
        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of(CSPDirectiveSrcValue.UNSAFE_INLINE)));
        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_INLINE, List.of(CSPDirectiveSrcValue.UNSAFE_INLINE)));
    }

    @Test
    void scriptSrcAttrAndStyleSrcAttributesOnlySupportNoneAndUnsafeInline() 
    {
        for (CSPDirectiveSrcValue value : CSPDirectiveSrcValue.values()) {
            if (value == CSPDirectiveSrcValue.NONE || value == CSPDirectiveSrcValue.UNSAFE_INLINE) {
                CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(value, List.of());
                CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(value, List.of());
            } else {
                assertThrows(IllegalArgumentException.class, () -> CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(value, List.of()));
            }
        }
    }

}
