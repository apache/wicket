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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketTestCase;


/**
 * Test autolinks (href="...")
 *
 * @author Juergen Donnerstag
 */
public class AutolinkTest extends WicketTestCase 
{
	private static Log log = LogFactory.getLog(AutolinkTest.class);

    /**
     * Create the test.
     *
     * @param name The test name
     */
    public AutolinkTest(String name) 
    {
        super(name);
    }
    
	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_1() throws Exception
	{
        application.getSettings().setAutomaticLinking(true);
	    executeTest(AutolinkPage_1.class, "AutolinkPageExpectedResult_1.html");
	}
    
	/**
	 * @throws Exception
	 */
	public void testRenderHomePage_2() throws Exception
	{
        application.getSettings().setAutomaticLinking(true);
        application.getSettings().setStripWicketTags(true);
	    executeTest(AutolinkPage_2.class, "AutolinkPageExpectedResult_2.html");
	}
}
