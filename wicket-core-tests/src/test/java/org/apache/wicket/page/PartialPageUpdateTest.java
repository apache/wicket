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
package org.apache.wicket.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

class PartialPageUpdateTest extends WicketTestCase
{
    @Test
    void returnsNoReplacementMethodIfNotSpecified()
    {
        var page = new PageForPartialUpdate();
        tester.startPage(page);
        var update = new TestPartialPageUpdate(page);

        update.add(page.alternativeReplacement, "theMarkupId");

        assertNull(update.getReplacementMethod("theMarkupId"));
    }

    @Test
    void returnsNoReplacementMethodIfNull()
    {
        var page = new PageForPartialUpdate();
        tester.startPage(page);
        var update = new TestPartialPageUpdate(page);

        update.add(null, page.alternativeReplacement, "theMarkupId");

        assertNull(update.getReplacementMethod("theMarkupId"));
    }

    @Test
    void returnsReplacementMethodIfSpecified()
    {
        var page = new PageForPartialUpdate();
        tester.startPage(page);
        var update = new TestPartialPageUpdate(page);

        update.add("theReplacementMethod", page.alternativeReplacement, "theMarkupId");

        assertEquals("theReplacementMethod", update.getReplacementMethod("theMarkupId"));
    }

    private static class TestPartialPageUpdate extends PartialPageUpdate
    {
        public TestPartialPageUpdate(PageForPartialUpdate page)
        {
            super(page);
        }

        @Override
        protected void writeFooter(Response response, String encoding)
        {
        }

        @Override
        protected void writeHeader(Response response, String encoding)
        {
        }

        @Override
        protected void writeComponent(Response response, String markupId, CharSequence contents)
        {
        }

        @Override
        protected void writePriorityEvaluation(Response response, CharSequence contents)
        {
        }

        @Override
        protected void writeHeaderContribution(Response response, CharSequence contents)
        {
        }

        @Override
        protected void writeEvaluation(Response response, CharSequence contents)
        {
        }

        @Override
        public void setContentType(WebResponse response, String encoding)
        {
        }
    }
}
