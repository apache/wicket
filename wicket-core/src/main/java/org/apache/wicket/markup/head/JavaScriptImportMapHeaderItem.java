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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.github.openjson.JSONObject;
import com.github.openjson.JSONStringer;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.LazyInitializer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.AttributeMap;

/**
 * {@link HeaderItem} for
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Reference/Elements/script/type/importmap">import
 * maps</a>.
 */
public class JavaScriptImportMapHeaderItem extends JavaScriptHeaderItem
{
    private final Map<String, JavaScriptResourceReference> resourceReferencesByModuleSpecifier;
    private final Map<String, Map<String, JavaScriptResourceReference>> resourceReferencesByModuleSpecifierByScopeUrl;
    private final Map<JavaScriptResourceReference, Collection<String>> integrityHashesByResourceReference;
    private final LazyInitializer<String> lazyJson = new LazyInitializer<>()
    {
        @Override
        protected String createInstance()
        {
            JSONObject importMap = new JSONObject();

            if (resourceReferencesByModuleSpecifier != null) {
                importMap.put("imports", createImportsObject(resourceReferencesByModuleSpecifier));
            }

            if (resourceReferencesByModuleSpecifierByScopeUrl != null) {
                JSONObject scopes = new JSONObject();
                resourceReferencesByModuleSpecifierByScopeUrl
                        .forEach((scopeUrl, resourceReferencesByModuleSpecifier) ->
                                scopes.put(scopeUrl, createImportsObject(resourceReferencesByModuleSpecifier)));
                importMap.put("scopes", scopes);
            }

            if (integrityHashesByResourceReference != null) {
                JSONObject integrity = new JSONObject();
                integrityHashesByResourceReference.forEach((resourceReference, hashes) ->
                        integrity.put(RequestCycle.get().urlFor(resourceReference, null).toString(), String.join(" ", hashes)));
                importMap.put("integrity", integrity);
            }

            return importMap.toString(new JSONStringer());
        }

        private static JSONObject createImportsObject(
                Map<String, JavaScriptResourceReference> resourceReferencesByModuleSpecifier
        )
        {
            JSONObject imports = new JSONObject();
            resourceReferencesByModuleSpecifier
                    .forEach((moduleSpecifier, resourceReference) ->
                            imports.put(moduleSpecifier, RequestCycle.get().urlFor(resourceReference, null)));
            return imports;
        }
    };

    /**
     * Create a header item for an import map. All fields are optional.
     *
     * @param resourceReferencesByModuleSpecifier the base module specifier map.
     * @param resourceReferencesByModuleSpecifierByScopeUrl module specifier maps for scripts with specific URLs that
     *                                                     import modules.
     * @param integrityHashesByResourceReference lists of hashes of the resources.
     */
    public JavaScriptImportMapHeaderItem(
            final Map<String, JavaScriptResourceReference> resourceReferencesByModuleSpecifier,
            final Map<String, Map<String, JavaScriptResourceReference>> resourceReferencesByModuleSpecifierByScopeUrl,
            final Map<JavaScriptResourceReference, Collection<String>> integrityHashesByResourceReference
    )
    {
        this.resourceReferencesByModuleSpecifier = resourceReferencesByModuleSpecifier;
        this.resourceReferencesByModuleSpecifierByScopeUrl = resourceReferencesByModuleSpecifierByScopeUrl;
        this.integrityHashesByResourceReference = integrityHashesByResourceReference;
    }

    @Override
    public Iterable<?> getRenderTokens()
    {
        String json = lazyJson.get();
        if (Strings.isEmpty(getId()))
            return Collections.singletonList(json);
        return Arrays.asList(getId(), json);
    }

    @Override
    public void render(Response response)
    {
        AttributeMap attributes = new AttributeMap();
        attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "importmap");
        attributes.putAttribute(JavaScriptUtils.ATTR_ID, getId());
        attributes.putAttribute(JavaScriptUtils.ATTR_CSP_NONCE, getNonce());
        JavaScriptUtils.writeInlineScript(response, lazyJson.get(), attributes);
    }
}
