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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.IterableUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

public class JavaScriptImportMapHeaderItemTest extends WicketTestCase
{
    @Test
    void renderTokensAreCorrectIfNoIdSpecified() {
        JavaScriptHeaderItem item = new JavaScriptImportMapHeaderItem(null, null, null);

        List<?> renderTokens = IterableUtils.toList(item.getRenderTokens());

        assertEquals(List.of("{}"), renderTokens);
    }

    @Test
    void renderTokensAreCorrectIfIdSpecified() {
        JavaScriptHeaderItem item = new JavaScriptImportMapHeaderItem(null, null, null)
                .setId("theId");

        List<?> renderTokens = IterableUtils.toList(item.getRenderTokens());

        assertEquals(List.of("theId", "{}"), renderTokens);
    }

    @Test
    void doesNotAddPropertiesIfItemNotSpecified() {
        JavaScriptImportMapHeaderItem item = new JavaScriptImportMapHeaderItem(null, null, null);
        MockWebResponse response = new MockWebResponse();

        item.render(response);

        assertEquals("""
                <script type="importmap">{}</script>
                """, response.getTextResponse().toString());

    }

    @Test
    void outputsBaseModuleSpecifierMapIfSpecified() {
        Map<String, JavaScriptResourceReference> resourceReferencesByModuleSpecifier = new HashMap<>();
        resourceReferencesByModuleSpecifier.put("themodule", new JavaScriptResourceReference(JavaScriptImportMapHeaderItemTest.class, "thescript.js"));
        JavaScriptImportMapHeaderItem item = new JavaScriptImportMapHeaderItem(resourceReferencesByModuleSpecifier, null, null);
        MockWebResponse response = new MockWebResponse();
        tester.startComponentInPage(new WebMarkupContainer("someId"));

        item.render(response);

        assertEquals("""
                <script type="importmap">{"imports":{"themodule":"./wicket/resource/org.apache.wicket.markup.head.JavaScriptImportMapHeaderItemTest/thescript.js"}}</script>
                """, response.getTextResponse().toString());

    }

    @Test
    void outputsScopedModuleSpecifierMapsIfSpecified() {
        Map<String, JavaScriptResourceReference> resourceReferencesByModuleSpecifier = new HashMap<>();
        resourceReferencesByModuleSpecifier.put("themodule", new JavaScriptResourceReference(JavaScriptImportMapHeaderItemTest.class, "thescript.js"));
        Map<String, Map<String, JavaScriptResourceReference>> resourceReferencesByModuleSpecifierByScopeUrl = new HashMap<>();
        resourceReferencesByModuleSpecifierByScopeUrl.put("thescope", resourceReferencesByModuleSpecifier);
        JavaScriptImportMapHeaderItem item = new JavaScriptImportMapHeaderItem(null, resourceReferencesByModuleSpecifierByScopeUrl, null);
        MockWebResponse response = new MockWebResponse();
        tester.startComponentInPage(new WebMarkupContainer("someId"));

        item.render(response);

        assertEquals("""
                <script type="importmap">{"scopes":{"thescope":{"themodule":"./wicket/resource/org.apache.wicket.markup.head.JavaScriptImportMapHeaderItemTest/thescript.js"}}}</script>
                """, response.getTextResponse().toString());

    }

    @Test
    void outputsHashesIfSpecified() {
        Map<JavaScriptResourceReference, Collection<String>> integrityHashesByResourceReference = new HashMap<>();
        integrityHashesByResourceReference.put(
                new JavaScriptResourceReference(JavaScriptImportMapHeaderItemTest.class,
                        "thescript.js"), List.of("first-hash", "second-hash"));
        JavaScriptImportMapHeaderItem item = new JavaScriptImportMapHeaderItem(null, null, integrityHashesByResourceReference);
        MockWebResponse response = new MockWebResponse();
        tester.startComponentInPage(new WebMarkupContainer("someId"));

        item.render(response);

        assertEquals("""
                <script type="importmap">{"integrity":{"./wicket/resource/org.apache.wicket.markup.head.JavaScriptImportMapHeaderItemTest/thescript.js":"first-hash second-hash"}}</script>
                """, response.getTextResponse().toString());

    }

    @Test
    void outputsIdIfSpecified() {
        JavaScriptHeaderItem item = new JavaScriptImportMapHeaderItem(null, null, null)
                .setId("theId");
        MockWebResponse response = new MockWebResponse();
        tester.startComponentInPage(new WebMarkupContainer("someId"));

        item.render(response);

        assertEquals("""
                <script type="importmap" id="theId">{}</script>
                """, response.getTextResponse().toString());

    }

    @Test
    void outputsNonceIfSpecified() {
        AbstractCspHeaderItem item = new JavaScriptImportMapHeaderItem(null, null, null)
                .setNonce("theNonce");
        MockWebResponse response = new MockWebResponse();

        item.render(response);

        assertEquals("""
                <script type="importmap" nonce="theNonce">{}</script>
                """, response.getTextResponse().toString());

    }
}
