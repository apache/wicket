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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import wicket.markup.html.link.Link;
import wicket.protocol.http.MockWebApplication;
import wicket.util.io.Streams;
import wicket.util.string.StringList;

import junit.framework.TestCase;


/**
 * Test for simple table behaviour.
 */
public class PagedTableNavigatorTest extends TestCase
{
    /**
     * Construct.
     */
    public PagedTableNavigatorTest()
    {
        super();
    }

    /**
     * Construct.
     * @param name name of test
     */
    public PagedTableNavigatorTest(String name)
    {
        super(name);
    }

    /**
     * Test simple table behaviour.
     * @throws Exception
     */
    public void testPagedTable() throws Exception
    {
        MockWebApplication application = new MockWebApplication(null);
        application.getPages().setHomePage(PagedTableNavigatorPage.class);
        application.setupRequestAndResponse();
        application.processRequestCycle();
        PagedTableNavigatorPage page = (PagedTableNavigatorPage)application.getLastRenderedPage();
        String document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_1.html"));

        Link link = (Link)page.get("navigator.first");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.next");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_2.html"));

        link = (Link)page.get("navigator.first");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_3.html"));

        link = (Link)page.get("navigator.first");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_4.html"));

        link = (Link)page.get("navigator.first");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.first");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_5.html"));

        link = (Link)page.get("navigator.first");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertFalse(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.navigation.2.pageLink");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_6.html"));

        link = (Link)page.get("navigator.first");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "PagedTableNavigatorExpectedResult_7.html"));

        link = (Link)page.get("navigator.first");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.prev");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.next");
        assertTrue(link.isEnabled());

        link = (Link)page.get("navigator.last");
        assertTrue(link.isEnabled());
    }

    /**
     * Validates page 1 of paged table.
     *
     * @param document The document
     * @param file 
     * @return The validation result
     * @throws IOException 
     */
    private boolean validatePage(final String document, final String file) throws IOException
    {
        String filename = this.getClass().getPackage().getName();
        filename = filename.replace('.', '/');
        filename += "/" + file;
        
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filename);
        if (in == null)
        {
            throw new IOException("File not found: " + filename);
        }
        
        String reference = Streams.readString(in);

        boolean equals = document.equals(reference);
        if (equals == false)
        {
            System.err.println("File name: " + file);
/*  */          
            System.err.println("===================");
            System.err.println(document);
            System.err.println("===================");

            System.err.println(reference);
            System.err.println("===================");
/* */            
	
	        String[] test1 = StringList.tokenize(document, "\n").toArray();
	        String[] test2 = StringList.tokenize(reference, "\n").toArray();
	        Diff diff = new Diff(test1, test2);
	        Diff.change script = diff.diff_2(false);
	        DiffPrint.Base p = new DiffPrint.UnifiedPrint( test1, test2 );
	        p.setOutput(new PrintWriter(System.err));
	        p.print_script(script);
        }
        
        return equals;
    }
}
