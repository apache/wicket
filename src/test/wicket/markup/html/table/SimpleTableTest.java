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
package wicket.markup.html.table;

import wicket.protocol.http.MockWebApplication;
import wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import wicket.protocol.http.documentvalidation.Tag;
import wicket.protocol.http.documentvalidation.TextContent;
import junit.framework.TestCase;


/**
 * Test for simple table behaviour.
 */
public class SimpleTableTest extends TestCase
{

    /**
     * Construct.
     * 
     */
    public SimpleTableTest()
    {
        super();
    }

    /**
     * Construct.
     * @param arg0
     */
    public SimpleTableTest(String arg0)
    {
        super(arg0);
    }

    /**
     * Test simple table behaviour.
     * @throws Exception
     */
    public void testSimpleTable() throws Exception
    {
        MockWebApplication application = new MockWebApplication(null);
        application.getPages().setHomePage(SimpleTablePage.class);
        application.setupRequestAndResponse();
        application.processRequestCycle();
        SimpleTablePage page = (SimpleTablePage)application.getLastRenderedPage();
        String document = application.getServletResponse().getDocument();
        assertTrue(validateDocument(document));
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
        title.addExpectedChild(new TextContent("Simple Table Page"));
        Tag body = new Tag("body");
        html.addExpectedChild(body);
        Tag ul = new Tag("ul");
        ul.addExpectedChild(new Tag("li").addExpectedChild(new Tag("span").addExpectedChild(new TextContent("one"))));
        ul.addExpectedChild(new Tag("li").addExpectedChild(new Tag("span").addExpectedChild(new TextContent("two"))));
        ul.addExpectedChild(new Tag("li").addExpectedChild(new Tag("span").addExpectedChild(new TextContent("three"))));
        body.addExpectedChild(ul);
        validator.addRootElement(html);

        return validator.isDocumentValid(document);
    }
}
