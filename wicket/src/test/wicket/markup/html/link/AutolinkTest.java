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
import wicket.protocol.http.MockHttpApplication;
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
    private MockHttpApplication application;

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
        application = new MockHttpApplication(null);
        application.getPages().setHomePage(AutolinkPage.class);
        application.getSettings().setAutomaticLinking(true);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
    	Assert.assertTrue(validateDocument(document));
    	
    	/*
        TODO
        <img href="Page1.gif"> ...   if autolink == true then resolve Page relativ
        <img href="/Page1.gif"> ...   if autolink == true then resolve servlet context absolut
        <link href="*.css"> ...      if autolink == true then replace in <head> as well
        <a href="subdir/Home.html">  if autolink == true then resolve Page relativ
        <a href="/rootDir/Home.html"> if autolink == true then resolve servlet absolut
        */

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
		anchor1.addExpectedAttribute("href", ".*MockHttpApplication.*");
		anchor1.addExpectedChild(new TextContent("Home"));
		body.addExpectedChild(anchor1);
		
		Tag link1 = new Tag("wicket:link");
		body.addExpectedChild(link1);
		link1.addExpectedChild(new TextContent(".*"));
		
		Tag anchor2 = new Tag("a");
		anchor2.addExpectedAttribute("href", ".*MockHttpApplication.*");
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
		anchor4.addExpectedAttribute("href", ".*MockHttpApplication.*");
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
		anchor6.addExpectedAttribute("href", ".*MockHttpApplication.*name=test&id=123");
		anchor6.addExpectedChild(new TextContent("Home"));
		link5.addExpectedChild(anchor6);
		link5.addExpectedChild(new TextContent(".*"));
	
		validator.addRootElement(html);
		return validator.isDocumentValid(document);
	}
}
