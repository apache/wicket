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

import junit.framework.TestCase;
import wicket.protocol.http.MockHttpApplication;

/**
 * Simple application that demonstrates the mock http application
 * code (and checks that it is working)
 *
 * @author Chris Turner
 */
public class ComponentCreateTagTest extends TestCase {

    private MockHttpApplication application;

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
        application = new MockHttpApplication(null);
        application.getPages().setHomePage(ComponentCreateTag.class);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage_2() throws Exception {
        application = new MockHttpApplication(null);
        application.getPages().setHomePage(ComponentCreateTag_2.class);
        
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
    }
}
