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
package com.voicetribe.wicket.markup.html.table;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.voicetribe.util.io.Streams;
import com.voicetribe.util.string.StringList;
import com.voicetribe.wicket.markup.html.link.Link;
import com.voicetribe.wicket.protocol.http.MockHttpApplication;

/**
 * Test for simple table behaviour.
 */
public class SortableTableHeadersTest extends TestCase
{
    /**
     * Construct.
     */
    public SortableTableHeadersTest()
    {
        super();
    }

    /**
     * Construct.
     * @param name name of test
     */
    public SortableTableHeadersTest(String name)
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
        application.getSettings().setHomePage(SortableTableHeadersPage.class);
        application.setupRequestAndResponse();
        application.processRequestCycle();
        SortableTableHeadersPage page = (SortableTableHeadersPage)application.getLastRenderedPage();
        String document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "SortableTableHeadersExpectedResult_1.html"));

        Link link = (Link)page.get("header.id.actionLink");
        assertTrue(link.isEnabled());

        link = (Link)page.get("header.name.actionLink");
        assertTrue(link.isEnabled());

        link = (Link)page.get("header.email.actionLink");
        assertNull(link);

        link = (Link)page.get("header.name.actionLink");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();

        // Check that redirect was set as expected and invoke it
        Assert.assertTrue("Response should be a redirect", application.getServletResponse().isRedirect());
        String redirect = application.getServletResponse().getRedirectLocation();
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToRedirectString(redirect);
        application.processRequestCycle();
        
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "SortableTableHeadersExpectedResult_2.html"));

        // reverse sorting
        link = (Link)page.get("header.name.actionLink");
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToComponent(link);
        application.processRequestCycle();

        // Check that redirect was set as expected and invoke it
        Assert.assertTrue("Response should be a redirect", application.getServletResponse().isRedirect());
        redirect = application.getServletResponse().getRedirectLocation();
        application.setupRequestAndResponse();
        application.getServletRequest().setRequestToRedirectString(redirect);
        application.processRequestCycle();
        
        document = application.getServletResponse().getDocument();
        assertTrue(validatePage(document, "SortableTableHeadersExpectedResult_3.html"));
    }

    /**
     * Validates page 1 of paged table.
     *
     * @param document The document
     * @return The validation result
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
