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

import wicket.markup.html.link.Link;
import wicket.protocol.http.MockHttpApplication;
import wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import wicket.protocol.http.documentvalidation.Tag;
import wicket.protocol.http.documentvalidation.TextContent;
import junit.framework.TestCase;


/**
 * Test for simple table behaviour.
 */
public class IncrementalTableNavigationTest extends TestCase
{

    /**
     * Construct.
     */
    public IncrementalTableNavigationTest()
    {
        super();
    }

    /**
     * Construct.
     * @param name name of test
     */
    public IncrementalTableNavigationTest(String name)
    {
        super(name);
    }

    /**
     * Test simple table behaviour.
     * @throws Exception
     */
    public void testPagedTable() throws Exception
    {
        MockHttpApplication application = new MockHttpApplication(null);
        application.getPages().setHomePage(IncrementalTableNavigationPage.class);
        application.setupRequestAndResponse();
        application.processRequestCycle();
        IncrementalTableNavigationPage page = (IncrementalTableNavigationPage)application.getLastRenderedPage();
        String document = application.getServletResponse().getDocument();
        assertTrue(validatePage1(document));

        Link link = (Link)page.get("nextNext");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage2(document));

        link = (Link)page.get("prev");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage3(document));
    }

    /**
     * Validates page 1 of paged table.
     *
     * @param document The document
     * @return The validation result
     */
    private boolean validatePage1(String document)
    {
        //System.err.println(document);
        
        HtmlDocumentValidator validator = new HtmlDocumentValidator();
        Tag html = new Tag("html");
        Tag head = new Tag("head");
        html.addExpectedChild(head);
        Tag title = new Tag("title");
        head.addExpectedChild(title);
        title.addExpectedChild(new TextContent("Paged Table Page"));
        Tag body = new Tag("body");
        html.addExpectedChild(body);

        Tag ulTable = new Tag("ul");
        ulTable.addExpectedChild(new Tag("li")
                .addExpectedChild(new Tag("span")
                .addExpectedChild(new TextContent("one"))));
        ulTable.addExpectedChild(new Tag("li")
                .addExpectedChild(new Tag("span")
                .addExpectedChild(new TextContent("two"))));
        // note that we DO NOT expect the third element as this is not on the current page
        body.addExpectedChild(ulTable);

        body.addExpectedChild(new Tag("span")
                .addExpectedChild(new Tag("i")
                    .addExpectedChild(new TextContent("Prev"))));
        
        body.addExpectedChild(new Tag("a")
        	.addExpectedChild(new TextContent("NextNext")));

        validator.addRootElement(html);

        return validator.isDocumentValid(document);
    }

    /**
     * Validate page 2 of the paged table.
     *
     * @param document The document
     * @return The validation result
     */
    private boolean validatePage2(String document)
    {
        //System.err.println(document);
        
        HtmlDocumentValidator validator = new HtmlDocumentValidator();
        Tag html = new Tag("html");
        Tag head = new Tag("head");
        html.addExpectedChild(head);
        Tag title = new Tag("title");
        head.addExpectedChild(title);
        title.addExpectedChild(new TextContent("Paged Table Page"));
        Tag body = new Tag("body");
        html.addExpectedChild(body);

        Tag ulTable = new Tag("ul");
        ulTable.addExpectedChild(new Tag("li")
                .addExpectedChild(new Tag("span")
                .addExpectedChild(new TextContent("five"))));
        ulTable.addExpectedChild(new Tag("li")
                .addExpectedChild(new Tag("span")
                .addExpectedChild(new TextContent("six"))));
        // note that we DO NOT expect the third element as this is not on the current page
        body.addExpectedChild(ulTable);

        body.addExpectedChild(new Tag("a")
                .addExpectedChild(new TextContent("Prev")));
        
        body.addExpectedChild(new Tag("a")
        	.addExpectedChild(new TextContent("NextNext")));

        validator.addRootElement(html);

        return validator.isDocumentValid(document);
    }

    /**
     * Validate page 3 of the paged table.
     *
     * @param document The document
     * @return The validation result
     */
    private boolean validatePage3(String document)
    {
        //System.err.println(document);
        
        HtmlDocumentValidator validator = new HtmlDocumentValidator();
        Tag html = new Tag("html");
        Tag head = new Tag("head");
        html.addExpectedChild(head);
        Tag title = new Tag("title");
        head.addExpectedChild(title);
        title.addExpectedChild(new TextContent("Paged Table Page"));
        Tag body = new Tag("body");
        html.addExpectedChild(body);

        Tag ulTable = new Tag("ul");
        ulTable.addExpectedChild(new Tag("li")
                .addExpectedChild(new Tag("span")
                .addExpectedChild(new TextContent("three"))));
        ulTable.addExpectedChild(new Tag("li")
                .addExpectedChild(new Tag("span")
                .addExpectedChild(new TextContent("four"))));
        // note that we DO NOT expect the third element as this is not on the current page
        body.addExpectedChild(ulTable);

        body.addExpectedChild(new Tag("a")
                .addExpectedChild(new TextContent("Prev")));
        
        body.addExpectedChild(new Tag("a")
        	.addExpectedChild(new TextContent("NextNext")));

        validator.addRootElement(html);

        return validator.isDocumentValid(document);
    }
}
