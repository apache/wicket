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
package wicket.protocol.http;

import junit.framework.TestCase;

/**
 * Simple application that demonstrates the mock http application
 * code (and checks that it is working)
 *
 * @author Chris Turner
 */
public class WicketTagPanelTest extends TestCase {

    private MockHttpApplication application;

    /**
     * Create the test.
     *
     * @param name The test name
     */
    public WicketTagPanelTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        application = new MockHttpApplication(null);
        application.getSettings().setHomePage(WicketPanelPage.class);
    }

    /**
     * @throws Exception
     */
    public void testRenderHomePage() throws Exception {
        // Do the processing
        application.setupRequestAndResponse();
        application.processRequestCycle();

        // Validate the document
        String document = application.getServletResponse().getDocument();
        System.out.println(document);
    }
}
