/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.voicetribe.wicket;

import com.voicetribe.wicket.protocol.http.MockHttpApplication;
import com.voicetribe.wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import com.voicetribe.wicket.protocol.http.documentvalidation.Tag;
import com.voicetribe.wicket.protocol.http.documentvalidation.TextContent;
import com.voicetribe.wicket.markup.html.basic.Label;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This set of tests builds a sample application for testing the
 * dynamic modicication of attributes in both component tags and
 * also in raw markup. The tests contained here represent testing
 * on one specific area of functionality of the Component class.
 * It is expected that separate test cases will be added to test
 * other facets of Components.
 *
 * @author Chris Turner
 */
public class AttributeModifierComponentTest extends TestCase {

    private MockHttpApplication application;

    /**
     * Create a test case instance.
     *
     * @param name The name of the test being run
     */
    public AttributeModifierComponentTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        application = new MockHttpApplication(null);
        application.getSettings().setHomePage(AttributeModifierComponentPage.class);
    }

    public void testComponentTagAttributeModification() throws Exception {
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
        Assert.assertTrue(validateDocument(document));
    }

    /**
     * Helper method to validate the returned XML document.
     *
     * @param document The document
     * @return The validation result
     */
    private boolean validateDocument(String document) {
        HtmlDocumentValidator validator = new HtmlDocumentValidator();
        Tag html = new Tag("html");
        Tag head = new Tag("head");
        html.addExpectedChild(head);
        Tag title = new Tag("title");
        head.addExpectedChild(title);
        title.addExpectedChild(new TextContent("Attribute Modifier Test Page"));
        Tag body = new Tag("body");
        html.addExpectedChild(body);

        Tag label1 = new Tag("span");
        label1.addExpectedAttribute("class", "label");
        label1.addExpectedChild(new TextContent("Label 1"));
        body.addExpectedChild(label1);

        Tag label2 = new Tag("span");
        label2.addExpectedAttribute("class", "overrideLabel");
        label2.addExpectedChild(new TextContent("Label 2"));
        body.addExpectedChild(label2);

        Tag label3 = new Tag("span");
        label3.addExpectedAttribute("class", "insertLabel");
        label3.addExpectedChild(new TextContent("Label 3"));
        body.addExpectedChild(label3);

        validator.addRootElement(html);
        return validator.isDocumentValid(document);
    }

}
