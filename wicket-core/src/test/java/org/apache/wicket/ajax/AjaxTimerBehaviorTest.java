/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ajax;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.MockPageWithLinkAndComponent;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests that an AbstractAjaxTimerBehavior injects itself into the markup once and only once. Also
 * tests the callback URL to make sure the timer reinjects itself
 * 
 * @author Jim McLaughlin
 */
public class AjaxTimerBehaviorTest extends WicketTestCase
{
	private static final Logger log = LoggerFactory.getLogger(AjaxTimerBehaviorTest.class);

	/**
	 * Tests timer behavior in a component added to an AjaxRequestTarget
	 */
	@Test
	public void addToAjaxUpdate()
	{
		Duration dur = Duration.seconds(20);
		final MyAjaxSelfUpdatingTimerBehavior timer = new MyAjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();

		page.add(new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID).setOutputMarkupId(true));


		page.add(new AjaxLink<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				WebMarkupContainer wmc = new WebMarkupContainer(
					MockPageWithLinkAndComponent.COMPONENT_ID);
				wmc.setOutputMarkupId(true);
				wmc.add(timer);
				page.replace(wmc);
				target.add(wmc);
			}
		});

		tester.startPage(page);
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		validate(timer, false);

	}


	/**
	 * tests timer behavior in a WebPage.
	 */
	@Test
	public void addToWebPage()
	{
		Duration dur = Duration.seconds(20);
		final MyAjaxSelfUpdatingTimerBehavior timer = new MyAjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();
		Label label = new Label(MockPageWithLinkAndComponent.COMPONENT_ID, "Hello");
		page.add(label);
		page.add(new Link<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				// do nothing, link is just used to simulate a roundtrip
			}
		});
		label.setOutputMarkupId(true);
		label.add(timer);

		tester.startPage(page);

		validate(timer, true);

		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		validate(timer, true);

	}

	/**
	 * Validates the response, then makes sure the timer injects itself again when called.
	 * Tests {@link AbstractAjaxTimerBehavior#restart(AjaxRequestTarget)} method

	 * WICKET-1525, WICKET-2152
	 */
	@Test
	public void testRestartMethod()
	{
		final Integer labelInitialValue = Integer.valueOf(0);

		final Label label = new Label(MockPageWithLinkAndComponent.COMPONENT_ID,
			new Model<Integer>(labelInitialValue));

		// the duration doesn't matter because we manually trigger the behavior
		final AbstractAjaxTimerBehavior timerBehavior = new AbstractAjaxTimerBehavior(
			Duration.seconds(2))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				// increment the label's model object
				label.setDefaultModelObject(((Integer)label.getDefaultModelObject()) + 1);
				target.add(label);
			}
		};

		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();
		page.add(label);
		page.add(new AjaxLink<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				if (timerBehavior.isStopped())
				{
					timerBehavior.restart(target);
				}
				else
				{
					timerBehavior.stop(target);
				}
			}
		});

		label.setOutputMarkupId(true);
		label.add(timerBehavior);

		tester.startPage(page);

		final String labelPath = MockPageWithLinkAndComponent.COMPONENT_ID;

		// assert label == initial value (i.e. 0)
		tester.assertLabel(labelPath, String.valueOf(labelInitialValue));

		// increment to 1
		tester.executeBehavior(timerBehavior);

		// assert label == 1
		tester.assertLabel(labelPath, String.valueOf(labelInitialValue + 1));

		// stop the timer
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		// trigger it, but it is stopped
		tester.executeBehavior(timerBehavior);

		// assert label is still 1
		tester.assertLabel(labelPath, String.valueOf(labelInitialValue + 1));

		// restart the timer
		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		// increment to 2
		tester.executeBehavior(timerBehavior);

		// assert label is now 2
		tester.assertLabel(labelPath, String.valueOf(labelInitialValue + 2));
	}

	/**
	 * Validates the reponse, then makes sure the timer injects itself again when called.
	 * 
	 * @param timer
	 * @param inBodyOnLoad
	 */
	private void validate(MyAjaxSelfUpdatingTimerBehavior timer, boolean inBodyOnLoad)
	{
		String document = tester.getLastResponseAsString();

		String updateScript = timer.getUpdateScript();

		if (inBodyOnLoad)
		{
			String bodyOnLoadUpdateScript = "Wicket.Event.add(window, \"load\", function(event) { \n" +
				updateScript + ";\n;});";
			validateTimerScript(document, bodyOnLoadUpdateScript);
		}
		else
		{
			updateScript = updateScript.replaceAll("]", "]^");
			validateTimerScript(document, updateScript);
		}

		tester.executeBehavior(timer);

		if (inBodyOnLoad)
		{
			updateScript = timer.getUpdateScript();
			updateScript = updateScript.replaceAll("]", "]^");
		}

		// Validate the document
		document = tester.getLastResponseAsString();
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
		log.debug(document);
		String quotedRegex;
		quotedRegex = quote(updateScript);
		Pattern pat = Pattern.compile(quotedRegex, Pattern.DOTALL);
		Matcher mat = pat.matcher(document);

		int count = 0;
		while (mat.find())
		{
			++count;
		}
		// make sure there is only one match
		assertEquals("There should be 1 and only 1 script in the markup for this behavior," +
			"but " + count + " were found", 1, count);
	}

	// quick fix for JDK 5 method
	private static final String quote(String s)
	{
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1)
		{
			return "\\Q" + s + "\\E";
		}

		StringBuilder sb = new StringBuilder(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1)
		{
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		return sb.toString();
	}

	static class MyAjaxSelfUpdatingTimerBehavior extends AjaxSelfUpdatingTimerBehavior
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Duration duration;
		String updateScript;

		/**
		 * Construct.
		 * 
		 * @param updateInterval
		 */
		public MyAjaxSelfUpdatingTimerBehavior(Duration updateInterval)
		{
			super(updateInterval);
			duration = updateInterval;
		}

		@Override
		protected void onComponentRendered()
		{
			super.onComponentRendered();
			updateScript = getJsTimeoutCall(duration);
		}

		/**
		 * @return Update script
		 */
		public String getUpdateScript()
		{
			return updateScript;
		}


	}
}
