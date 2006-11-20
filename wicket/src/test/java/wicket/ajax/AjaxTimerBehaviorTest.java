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
package wicket.ajax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.MockPageWithLinkAndComponent;
import wicket.MockPageWithOneComponent;
import wicket.Page;
import wicket.WicketTestCase;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.tester.ITestPageSource;
import wicket.util.time.Duration;

/**
 * Tests that an AbstractAjaxTimerBehavior injects itself into the markup once
 * and only once. Also tests the callback URL to make sure the timer reinjects
 * itself
 * 
 * @author Jim McLaughlin
 */
public class AjaxTimerBehaviorTest extends WicketTestCase
{
	private static final Log log = LogFactory.getLog(AjaxTimerBehaviorTest.class);

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public AjaxTimerBehaviorTest(String name)
	{
		super(name);
	}


	/**
	 * Tests timer behavior in a component added to an AjaxRequestTarget
	 */
	public void testAddToAjaxUpdate()
	{
		Duration dur = Duration.seconds(20);
		final MyAjaxSelfUpdatingTimerBehavior timer = new MyAjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();

		page.add(new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID)
				.setOutputMarkupId(true));


		page.add(new AjaxLink(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				WebMarkupContainer wmc = new WebMarkupContainer(
						MockPageWithLinkAndComponent.COMPONENT_ID);
				wmc.setOutputMarkupId(true);
				wmc.add(timer);
				page.replace(wmc);
				target.addComponent(wmc);
			}
		});

		application.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		application.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		validate(timer);

	}


	/**
	 * tests timer behavior in a WebPage.
	 */
	public void testAddToWebPage()
	{
		Duration dur = Duration.seconds(20);
		final MyAjaxSelfUpdatingTimerBehavior timer = new MyAjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithOneComponent page = new MockPageWithOneComponent();
		Label label = new Label(MockPageWithOneComponent.COMPONENT_ID, "Hello");
		page.add(label);
		label.setOutputMarkupId(true);
		label.add(timer);

		application.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		validate(timer);

	}

	/**
	 * Validates the reponse, then makes sure the timer injects itself again
	 * when called.
	 * 
	 * @param timer
	 */
	private void validate(MyAjaxSelfUpdatingTimerBehavior timer)
	{
		String document = application.getServletResponse().getDocument();

		String updateScript = timer.getUpdateScript();

		validateTimerScript(document, updateScript);


		application.setupRequestAndResponse();
		WebRequestCycle cycle = application.createRequestCycle();
		String url = timer.getCallbackUrl().toString();
		application.getServletRequest().setRequestToRedirectString(url);

		application.processRequestCycle(cycle);

		// Validate the document
		document = application.getServletResponse().getDocument();
		validateTimerScript(document, updateScript);
	}

	/**
	 * Checks that the timer javascript is in the document once and only once
	 * 
	 * @param document
	 *            the response from the Application
	 * @param updateScript
	 *            the timer script
	 */
	private void validateTimerScript(String document, String updateScript)
	{
		String quotedRegex = Pattern.quote(updateScript);
		Pattern pat = Pattern.compile(quotedRegex, Pattern.DOTALL);
		Matcher mat = pat.matcher(document);

		int count = 0;
		while (mat.find())
		{
			++count;
		}
		// make sure there is only one match
		assertEquals("There should be 1 and only 1 script in the markup for this behavior,"
				+ "but " + count + " were found", 1, count);
	}

	static class MyAjaxSelfUpdatingTimerBehavior extends AjaxSelfUpdatingTimerBehavior
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Duration duration;
		String updateScript;

		/**
		 * Construct.
		 * @param updateInterval
		 */
		public MyAjaxSelfUpdatingTimerBehavior(Duration updateInterval)
		{
			super(updateInterval);
			this.duration = updateInterval;
		}

		protected void onComponentRendered()
		{
			super.onComponentRendered();
			updateScript = getJsTimeoutCall(duration);
		}

		public String getUpdateScript()
		{
			return updateScript;
		}


	}
}
