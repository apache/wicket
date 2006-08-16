/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision$
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
package wicket.markup.html.internal;

import wicket.WicketTestCase;
import wicket.markup.MarkupParser;
import wicket.markup.MarkupParserFactory;
import wicket.markup.MarkupResourceStream;
import wicket.markup.parser.filter.EnclosureHandler;

/**
 * 
 * @author Juergen Donnerstag
 */
public class EnclosureTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public EnclosureTest(final String name)
	{
		super(name);
	}

	/**
	 * 
	 * @see wicket.WicketTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.application.getMarkupSettings().setMarkupParserFactory(new MarkupParserFactory()
		{
			@Override
			public MarkupParser newMarkupParser(MarkupResourceStream resource)
			{
				MarkupParser parser = super.newMarkupParser(resource);
				// register the additional EnclosureHandler
				parser.registerMarkupFilter(new EnclosureHandler(application));
				return parser;
			}
		});
	}
	
	/**
	 * @throws Exception
	 */
	public void testRenderHomePage() throws Exception
	{
		executeTest(EnclosurePage_1.class, "EnclosurePageExpectedResult_1.html");
	}
}
