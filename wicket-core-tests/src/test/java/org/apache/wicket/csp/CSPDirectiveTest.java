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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CSPDirectiveTest {

    @Test
    void scriptSrcAttrAndStyleSrcAttributesSupportValuesFromSpec()
    {
        CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of());
        CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_INLINE, List.of());
        CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_HASHES, List.of());
        CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.REPORT_SAMPLE, List.of());

        CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of());
        CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_INLINE, List.of());
        CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_HASHES, List.of());
        CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.REPORT_SAMPLE, List.of());
    }

    @Test
    void scriptSrcAttrAndStyleSrcAttributesDoesNotSupportOthersAndNone()
    {
        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of(CSPDirectiveSrcValue.UNSAFE_INLINE)));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of(CSPDirectiveSrcValue.UNSAFE_HASHES)));
    }

    @Test
    void scriptSrcAttrAndStyleSrcAttributesDoesNotSupportNoneAndOthers()
    {
        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.SCRIPT_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_INLINE, List.of(CSPDirectiveSrcValue.NONE)));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.STYLE_SRC_ATTR.checkValueForDirective(CSPDirectiveSrcValue.UNSAFE_HASHES, List.of(CSPDirectiveSrcValue.NONE)));
    }

    @Test
    void requireTrustedTypesForSupportsScriptOnly()
    {
        CSPDirective.REQUIRE_TRUSTED_TYPES_FOR.checkValueForDirective(new FixedCSPValue("'script'"), List.of());

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.REQUIRE_TRUSTED_TYPES_FOR.checkValueForDirective(new FixedCSPValue("script"), List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.REQUIRE_TRUSTED_TYPES_FOR.checkValueForDirective(new FixedCSPValue("'style'"), List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.REQUIRE_TRUSTED_TYPES_FOR.checkValueForDirective(CSPDirectiveSrcValue.SELF, List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.REQUIRE_TRUSTED_TYPES_FOR.checkValueForDirective(new FixedCSPValue("'script'"), List.of(new FixedCSPValue("'script'"))));
    }

    @Test
    void trustedTypesSupportsValuesFromSpec()
    {
        CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("wicket"), List.of());
        CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("my-Policy_1#=/@.%"), List.of(new FixedCSPValue("wicket")));
        CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("'allow-duplicates'"), List.of(new FixedCSPValue("wicket")));
        CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("*"), List.of(new FixedCSPValue("'allow-duplicates'")));
        CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("'none'"), List.of());
    }

    @Test
    void trustedTypesDoesNotSupportOtherValues()
    {
        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("'wicket'"), List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("wicket default"), List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("wicket;"), List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("'self'"), List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(CSPDirectiveSrcValue.SELF, List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(CSPDirectiveSrcValue.WILDCARD, List.of()));
    }

    @Test
    void trustedTypesSupportsNoneOfCSPDirectiveSrcValue()
    {
        CSPDirective.TRUSTED_TYPES.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of());

        CSPHeaderConfiguration configuration = new CSPHeaderConfiguration()
                .add(CSPDirective.TRUSTED_TYPES, CSPDirectiveSrcValue.NONE);

        assertEquals("trusted-types 'none'", configuration.renderHeaderValue(null, null));
    }

    @Test
    void trustedTypesDoesNotSupportNoneAndOthers()
    {
        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("'none'"), List.of(new FixedCSPValue("wicket"))));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("wicket"), List.of(new FixedCSPValue("'none'"))));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of(new FixedCSPValue("wicket"))));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(new FixedCSPValue("wicket"), List.of(CSPDirectiveSrcValue.NONE)));

        assertThrows(IllegalArgumentException.class, () ->
                CSPDirective.TRUSTED_TYPES.checkValueForDirective(CSPDirectiveSrcValue.NONE, List.of(CSPDirectiveSrcValue.NONE)));
    }

    @Test
    void requireTrustedTypesIsIdempotent()
    {
        CSPHeaderConfiguration configuration = new CSPHeaderConfiguration().requireTrustedTypes()
                .requireTrustedTypes();

        assertEquals("require-trusted-types-for 'script'; trusted-types wicket",
                configuration.renderHeaderValue(null, null));
    }

    @Test
    void trustedTypesAreRenderedInHeader()
    {
        CSPHeaderConfiguration configuration = new CSPHeaderConfiguration().requireTrustedTypes()
                .add(CSPDirective.TRUSTED_TYPES, "myPolicy", "'allow-duplicates'");

        assertEquals("require-trusted-types-for 'script'; trusted-types wicket myPolicy 'allow-duplicates'",
                configuration.renderHeaderValue(null, null));
    }

    @Test
    void presetsDoNotRequireTrustedTypes()
    {
        CSPHeaderConfiguration strict = new CSPHeaderConfiguration().strict();
        CSPHeaderConfiguration unsafeInline = new CSPHeaderConfiguration().unsafeInline();

        assertFalse(strict.getDirectives().containsKey(CSPDirective.REQUIRE_TRUSTED_TYPES_FOR));
        assertFalse(strict.getDirectives().containsKey(CSPDirective.TRUSTED_TYPES));
        assertFalse(unsafeInline.getDirectives().containsKey(CSPDirective.REQUIRE_TRUSTED_TYPES_FOR));
        assertFalse(unsafeInline.getDirectives().containsKey(CSPDirective.TRUSTED_TYPES));
    }
}
