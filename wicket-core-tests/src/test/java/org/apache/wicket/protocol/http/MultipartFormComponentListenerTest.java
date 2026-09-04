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
package org.apache.wicket.protocol.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-6914
 * https://issues.apache.org/jira/browse/WICKET-6921
 */
class MultipartFormComponentListenerTest extends WicketTestCase
{
    @Test
    void updateFormEnctype()
    {
        tester.startPage(MultipartFormComponentListenerPage.class);
        tester.assertRenderedPage(MultipartFormComponentListenerPage.class);

        TagTester formTagTester = tester.getTagByWicketId("form");
        String formMarkupId = formTagTester.getAttribute("id");

        assertEquals(Form.ENCTYPE_MULTIPART_FORM_DATA, formTagTester.getAttribute("enctype"));

        tester.getRequest().setAttribute("form:dropDown", 1);
        tester.executeAjaxEvent("form:dropDown", "change");
        String ajaxResponse = tester.getLastResponseAsString();
        assertTrue(ajaxResponse.contains("Wicket.$('"+formMarkupId+"').enctype='" + MultipartFormComponentListener.ENCTYPE_URL_ENCODED + "'})();"));

        tester.getRequest().setAttribute("form:dropDown", 2);
        tester.executeAjaxEvent("form:dropDown", "change");
        ajaxResponse = tester.getLastResponseAsString();
        assertTrue(ajaxResponse.contains("Wicket.$('"+formMarkupId+"').enctype='" + Form.ENCTYPE_MULTIPART_FORM_DATA + "'})();"));

        tester.clickLink("toggleVisibility");
        ajaxResponse = tester.getLastResponseAsString();
        assertFalse(ajaxResponse.contains("Wicket.$('"+formMarkupId+"').enctype="), "enctype should not be pushed on hidden elements");
    }
}
