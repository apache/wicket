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
package wicket.markup.html.link;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.protocol.http.MockWebApplication;
import wicket.protocol.http.documentvalidation.HtmlDocumentValidator;
import wicket.protocol.http.documentvalidation.Tag;
import wicket.protocol.http.documentvalidation.TextContent;


/**
 * Test autolinks (href="...")
 *
 * @author Juergen Donnerstag
 */
public class AutolinkTest extends TestCase 
{
	private static Log log = LogFactory.getLog(AutolinkTest.class);

    private MockWebApplication application;

    /**
     * Create the test.
     *
     * @param name The test name
     */
    public AutolinkTest(String name) {
        super(name);
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage() throws Exception {
        application = new MockWebApplication(null);
        application.getPages().setHomePage(AutolinkPage.class);
        application.getSettings().setAutomaticLinking(true);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        log.info(document);
    	Assert.assertTrue(validateDocument(document));
    }

	/**
	 * Helper method to validate the returned XML document.
	 * @param document The document
	 * @return The validation result
	 */
	private boolean validateDocument(String document)
	{
		HtmlDocumentValidator validator = new HtmlDocumentValidator();
		Tag html = new Tag("html");
		Tag head = new Tag("head");
		html.addExpectedChild(head);
		Tag title = new Tag("title");
		head.addExpectedChild(title);
		title.addExpectedChild(new TextContent("Mock Page"));
		Tag body = new Tag("body");
		html.addExpectedChild(body);
	
		Tag anchor1 = new Tag("a");
		anchor1.addExpectedAttribute("href", ".*MockWebApplication.*");
		anchor1.addExpectedChild(new TextContent("Home"));
		body.addExpectedChild(anchor1);
		
		Tag link1 = new Tag("wicket:link");
		body.addExpectedChild(link1);
		link1.addExpectedChild(new TextContent(".*"));
		
		Tag anchor2 = new Tag("a");
		anchor2.addExpectedAttribute("href", ".*MockWebApplication.*");
		anchor2.addExpectedChild(new TextContent("Home"));
		link1.addExpectedChild(anchor2);
		
		Tag link2 = new Tag("wicket:link");
		body.addExpectedChild(link2);
		link2.addExpectedChild(new TextContent(".*"));
		
		Tag anchor3 = new Tag("a");
		anchor3.addExpectedAttribute("href", "Page1.html");
		anchor3.addExpectedChild(new TextContent("Home"));
		link2.addExpectedChild(anchor3);
		
		Tag link3 = new Tag("wicket:link");
		body.addExpectedChild(link3);
		link3.addExpectedChild(new TextContent(".*"));
		
		Tag anchor4 = new Tag("a");
		anchor4.addExpectedAttribute("href", ".*MockWebApplication.*");
		anchor4.addExpectedChild(new TextContent("Home"));
		link3.addExpectedChild(anchor4);
		
		Tag link4 = new Tag("wicket:link");
		body.addExpectedChild(link4);
		link4.addExpectedChild(new TextContent(".*"));
		
		Tag anchor5 = new Tag("a");
		anchor5.addExpectedAttribute("href", "Page1.html");
		anchor5.addExpectedChild(new TextContent("Home"));
		link4.addExpectedChild(anchor5);

		link4.addExpectedChild(new TextContent(".*"));
		
		Tag link5 = new Tag("wicket:link");
		link4.addExpectedChild(link5);
		link5.addExpectedChild(new TextContent(".*"));
		
		Tag anchor6 = new Tag("a");
		anchor6.addExpectedAttribute("href", ".*MockWebApplication.*name=test&amp;id=123");
		anchor6.addExpectedChild(new TextContent("Home"));
		link5.addExpectedChild(anchor6);
		link5.addExpectedChild(new TextContent(".*"));

		Tag link6 = new Tag("wicket:link");
		body.addExpectedChild(link6);
		Tag anchor7 = new Tag("a");
		anchor7.addExpectedAttribute("href", "Page1.html");
		anchor7.addExpectedChild(new TextContent("Home"));
		body.addExpectedChild(anchor7);

		Tag link7 = new Tag("wicket:link");
		body.addExpectedChild(link7);
		Tag anchor8 = new Tag("a");
		anchor8.addExpectedAttribute("href", ".*MockWebApplication.*wicket.markup.html.link.subdir.Page1");
		anchor8.addExpectedChild(new TextContent("Home"));
		body.addExpectedChild(anchor8);

		Tag link8 = new Tag("link");
		link8.addExpectedAttribute("href", "test.css");
		body.addExpectedChild(link8);

		Tag anchor9 = new Tag("a");
		anchor9.addExpectedAttribute("href", "/root/test.html");
		anchor9.addExpectedChild(new TextContent("Home"));
		body.addExpectedChild(anchor9);

		Tag anchor11 = new Tag("a");
		anchor11.addExpectedAttribute("href", ".*MockWebApplication.*wicket.markup.html.link.Page1");
		anchor11.addExpectedChild(new TextContent("Home"));
		body.addExpectedChild(anchor11);
		body.addExpectedChild(new TextContent(".*"));

		Tag anchor10 = new Tag("a");
		anchor10.addExpectedAttribute("href", "http://www.google.com");
		anchor10.addExpectedChild(new TextContent("Google"));
		body.addExpectedChild(anchor10);
		
		//body.addExpectedChild(new TextContent(".*"));
		validator.addRootElement(html);
		return validator.isDocumentValid(document);
	}
}
