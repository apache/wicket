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
package wicket.markup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import junit.framework.TestCase;
import wicket.markup.html.list.Diff;
import wicket.markup.html.list.DiffPrint;
import wicket.protocol.http.MockWebApplication;
import wicket.util.io.Streams;
import wicket.util.string.StringList;

/**
 * Simple application that demonstrates the mock http application
 * code (and checks that it is working)
 *
 * @author Chris Turner
 */
public class ComponentCreateTagTest extends TestCase {

    private MockWebApplication application;

    /**
     * Create the test.
     *
     * @param name The test name
     */
    public ComponentCreateTagTest(String name) {
        super(name);
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage() throws Exception {
        application = new MockWebApplication(null);
        application.getPages().setHomePage(ComponentCreateTag.class);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
        
        validatePage(document, "ComponentCreateTagExpectedResult.html");
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage_2() throws Exception {
        application = new MockWebApplication(null);
        application.getPages().setHomePage(ComponentCreateTag_2.class);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
        
        validatePage(document, "ComponentCreateTagExpectedResult_2.html");
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage_3() throws Exception {
        application = new MockWebApplication(null);
        application.getPages().setHomePage(ComponentCreateTag_3.class);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
        
        validatePage(document, "ComponentCreateTagExpectedResult_3.html");
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage_4() throws Exception {
        application = new MockWebApplication(null);
        application.getPages().setHomePage(ComponentCreateTag_4.class);
        application.getSettings().setStripWicketTags(true);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
        
        validatePage(document, "ComponentCreateTagExpectedResult_4.html");
    }

	/**
	 * Validates page 1 of paged table.
	 * @param document The document
	 * @param file the file
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
		    // Change the condition to true, if you want to make the new output
		    // the reference output for future tests. That is, it is regarded as 
		    // correct. It'll replace the current reference files. Thus change
		    // it only for one test-run.
		    if (false)
		    {
		        in.close();
		        in = null;

		        final URL url = this.getClass().getClassLoader().getResource(filename);
		        filename = url.getFile();
		        filename = filename.replaceAll("/build/test-classes/", "/src/test/");
		        PrintWriter out = new PrintWriter(new FileOutputStream(filename));
		        out.print(document);
		        out.close();
		        return true;
		    }
		    
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
			DiffPrint.Base p = new DiffPrint.UnifiedPrint(test1, test2);
			p.setOutput(new PrintWriter(System.err));
			p.print_script(script);
		}

		return equals;
	}
}
