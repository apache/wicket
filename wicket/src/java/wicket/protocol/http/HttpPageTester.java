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

import wicket.Page;
import wicket.RequestCycle;
import wicket.Session;
import wicket.response.ConsoleResponse;
import wicket.response.NullResponse;
import wicket.util.profile.IObjectProfileNode;


/**
 * Tests application pages by rendering them to the console. Also has profiling
 * functionality that can give an idea of how big the pages are.
 * @author Jonathan Locke
 */
public final class HttpPageTester
{ // TODO finalize javadoc
    private final RequestCycle cycle;

    private boolean consoleOutput = true;

    private boolean showObjectSize = true;

    private boolean dumpObjects = false;

    /**
     * Constructor.
     * @param application The application class to instantiate (must extend
     *            HttpApplication)
     */
    public HttpPageTester(final HttpApplication application)
    {
        this(new HttpSession(application, null)
        {
			/** Serial Version ID */
			private static final long serialVersionUID = -5729585004546567932L;
        });
    }

    /**
     * Constructor.
     * @param session Http session object for application
     */
    public HttpPageTester(final HttpSession session)
    {
        this(new HttpRequestCycle((HttpApplication) session.getApplication(), session,
                HttpRequest.NULL, NullResponse.getInstance()));
    }

    /**
     * Constructor.
     * @param cycle Http request cycle to do page testing with
     */
    public HttpPageTester(final HttpRequestCycle cycle)
    {
        Session.set(cycle.getSession());
        this.cycle = cycle;
    }

    /**
     * Test the given page
     * @param page The page to test
     */
    public void test(final Page page)
    {
        if (showObjectSize || dumpObjects)
        {
            final IObjectProfileNode profile = wicket.util.profile.ObjectProfiler
                    .profile(page);

            if (showObjectSize)
            {
                System.out.println("Page "
                        + page.getClass() + " object size = " + profile.size() + " bytes");
            }

            if (dumpObjects)
            {
                System.out.println("Page " + page.getClass() + " objects: " + profile.dump());
            }
        }

        // Render page using request cycle
        page.render(getRequestCycle());
    }

    /**
     * Returns true if console output is desired.
     * @return Returns the consoleOutput.
     */
    public boolean getConsoleOutput()
    {
        return consoleOutput;
    }

    /**
     * Determines if console output is desired.
     * @param consoleOutput The consoleOutput to set.
     */
    public void setConsoleOutput(final boolean consoleOutput)
    {
        this.consoleOutput = consoleOutput;
    }

    /**
     * @return Returns the showObjectSize.
     */
    public boolean getShowObjectSize()
    {
        return showObjectSize;
    }

    /**
     * @param showObjectSize The showObjectSize to set.
     */
    public void setShowObjectSize(final boolean showObjectSize)
    {
        this.showObjectSize = showObjectSize;
    }

    /**
     * @return Returns the dumpObjects.
     */
    public boolean getDumpObjects()
    {
        return dumpObjects;
    }

    /**
     * @param dumpObjects The dumpObjects to set.
     */
    public void setDumpObjects(final boolean dumpObjects)
    {
        this.dumpObjects = dumpObjects;
    }

    /**
     * @return RequestCycle for this page tester
     */
    private RequestCycle getRequestCycle()
    {
        if (consoleOutput)
        {
            cycle.setResponse(ConsoleResponse.getInstance());
        }

        return cycle;
    }
}


