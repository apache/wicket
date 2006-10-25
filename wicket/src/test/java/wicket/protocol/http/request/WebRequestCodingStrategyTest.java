/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http.request;

import wicket.RequestCycle;
import wicket.WicketTestCase;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.resource.DummyPage;

/**
 * Test of WebRequestCodingStrategy.
 * 
 * @author frankbille
 */
public class WebRequestCodingStrategyTest extends WicketTestCase
{
	private boolean executed;

	/**
	 * Construct.
	 */
	public WebRequestCodingStrategyTest()
	{
		super("Test of WebRequestCodingStrategy");
	}

	/**
	 * Test that the encoding generates correct encoded urls even for class
	 * names containing non ASCII characters, like ä, æ, ø, å etc.
	 * 
	 * See description in
	 * {@link WebRequestCodingStrategy#encode(RequestCycle, IBookmarkablePageRequestTarget)}
	 * for further information.
	 */
	public void testEncode__bookmarkablePage_nonAsciiClassNames()
	{
		application.setupRequestAndResponse();
		RequestCycle requestCycle = application.createRequestCycle();
		application.setHomePage(DummyPage.class);

		executed = false;
		WebRequestCodingStrategy codingStrategy = new WebRequestCodingStrategy()
		{
			protected CharSequence encode(RequestCycle requestCycle,
					IBookmarkablePageRequestTarget requestTarget)
			{
				executed = true;
				return super.encode(requestCycle, requestTarget);
			}
		};

		CharSequence url = codingStrategy.encode(requestCycle, new BookmarkablePageRequestTarget(
				NønÅsciiPäge.class));

		assertTrue(executed);

		assertNotNull(url);

		assertTrue(url.toString().endsWith(
				"wicket.protocol.http.request.N%C3%B8n%C3%85sciiP%C3%A4ge"));
	}

}
