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
package org.apache.wicket.behavior;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Test for https://issues.apache.org/jira/browse/WICKET-6192
 */
public class AjaxCallbackUrlTest extends WicketTestCase {

    @Test
    public void withoutPathParameters() {
        String callbackUrl = getCallbackUrl("/a/b/c");
        assertThat(callbackUrl, is(equalTo("./a/b/c?0-1.IBehaviorListener.0-link")));
    }

    @Test
    public void withPathParameters() {
        String callbackUrl = getCallbackUrl("/a/${b}/${c}");
        assertThat(callbackUrl, is(equalTo("./a/BBB/CCC?0-1.IBehaviorListener.0-link")));
    }

    private String getCallbackUrl(final String pageMountPath) {

        WebApplication application = tester.getApplication();
        application.mountPage(pageMountPath, TestPage.class);
        application.getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);

        PageParameters pageParameters = new PageParameters();
        pageParameters.set("b", "BBB", INamedParameters.Type.PATH);
        pageParameters.set("c", "CCC", INamedParameters.Type.PATH);

        TestPage page = tester.startPage(TestPage.class, pageParameters);

        return page.getAjaxCallbackUrl().toString();
    }

    public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
    {

        private final AbstractAjaxBehavior ajaxBehavior;

        public TestPage()
        {
            WebMarkupContainer link = new WebMarkupContainer("link");
            add(link);

            ajaxBehavior = new AbstractAjaxBehavior() {
                @Override
                public void onRequest() {

                }
            };
            link.add(ajaxBehavior);
        }

        private String getAjaxCallbackUrl()
        {
            return ajaxBehavior.getCallbackUrl().toString();
        }

        @Override
        public IResourceStream getMarkupResourceStream(final MarkupContainer container, final Class<?> containerClass) {
            return new StringResourceStream("<html><body><a wicket:id='link'>Link</a></body></html>");
        }
    }
}
