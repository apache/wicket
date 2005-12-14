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
package wicket.markup.parser;

import wicket.WicketTestCase;
import wicket.markup.parser.filter.PrependContextPathHandler;
import wicket.protocol.http.MockWebApplication;

/**
 * Quite some tests are already with MarkupParser. 
 * 
 * @author Juergen Donnerstag
 */
public class PrependContextPathHandlerTest extends WicketTestCase
{
	/**
	 * Construct.
	 * @param name
	 */
    public PrependContextPathHandlerTest(String name)
	{
		super(name);
	}

    /**
     * @see junit.framework.TestCase#setUp()
     */
	protected void setUp() throws Exception
	{
		application = new MockWebApplication(null)
		{
			public IMarkupFilter[] getAdditionalMarkupHandler()
			{
				return new IMarkupFilter[] { new PrependContextPathHandler() };
			}
		};
	}

	/**
     * 
     * @throws Exception
     */
    public final void testBasics() throws Exception
    {
	    executeTest(Page_1.class, "PageExpectedResult_1.html");
    }
}
