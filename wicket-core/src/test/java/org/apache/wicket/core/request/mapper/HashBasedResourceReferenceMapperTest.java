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
package org.apache.wicket.core.request.mapper;

import java.util.function.Supplier;
import org.apache.wicket.Application;
import org.apache.wicket.DefaultMapperContext;
import org.apache.wicket.Page;
import org.apache.wicket.SystemMapper;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.caching.IResourceCachingStrategy;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

class HashBasedResourceReferenceMapperTest {

    public static class OtherPage extends DummyHomePage
    {
        private static final JavaScriptResourceReference JAVA_SCRIPT_RESOURCE_REFERENCE = new JavaScriptResourceReference(OtherPage.class, "a.js");
        private static final CssResourceReference CSS_RESOURCE_REFERENCE = new CssResourceReference(OtherPage.class, "b.css");

    }

    public static class HomePage extends DummyHomePage
    {
        private static final JavaScriptResourceReference JAVA_SCRIPT_RESOURCE_REFERENCE = new JavaScriptResourceReference(HomePage.class, "a.js");
        private static final CssResourceReference CSS_RESOURCE_REFERENCE = new CssResourceReference(HomePage.class, "b.css");

        @Override
        public void renderHead(IHeaderResponse response) {
            response.render(JavaScriptHeaderItem.forReference(JAVA_SCRIPT_RESOURCE_REFERENCE));
            response.render(JavaScriptHeaderItem.forReference(OtherPage.JAVA_SCRIPT_RESOURCE_REFERENCE));
            response.render(CssHeaderItem.forReference(CSS_RESOURCE_REFERENCE));
            response.render(CssHeaderItem.forReference(OtherPage.CSS_RESOURCE_REFERENCE));
        }
    }

    @Test
    void withJavaHash() {
        final WebApplication dummyApplication = new DummyApplication() {
            @Override
            public Class<? extends Page> getHomePage() {
                return HomePage.class;
            }

            @Override
            protected IMapperContext newMapperContext() {
                return new DefaultMapperContext(this) {
                    @Override
                    public String getNamespace() {
                        // we want to hide wicket from URL.
                        return "appres";
                    }
                };
            }


            @Override
            protected void init() {
                super.init();
                setRootRequestMapper(new SystemMapper(this) {
                    @Override
                    protected IRequestMapper newResourceReferenceMapper(PageParametersEncoder pageParametersEncoder, ParentFolderPlaceholderProvider parentFolderPlaceholderProvider, Supplier<IResourceCachingStrategy> resourceCachingStrategy, Application application) {
                        return HashBasedResourceReferenceMapper.withJavaHash(pageParametersEncoder, parentFolderPlaceholderProvider, resourceCachingStrategy, false);
                    }
                });
            }
        };

        final WicketTester tester = new WicketTester(dummyApplication);
        long hash = HomePage.class.getName().hashCode();
        long hash1 = OtherPage.class.getName().hashCode();
        tester.startPage(HomePage.class);
        tester.assertRenderedPage(HomePage.class);
        // we check resource urls are hash encoded
        tester.assertContains("./appres/resource/" + hash + "/a.js");
        tester.assertContains("./appres/resource/" + hash1 + "/a.js");
        tester.assertContains("./appres/resource/" + hash + "/b.css");
        tester.assertContains("./appres/resource/" + hash1 + "/b.css");
        tester.destroy();
    }
}