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

import org.apache.wicket.Component;
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
	public void addedInAjaxSetsTimout()
	{
		Duration dur = Duration.seconds(20);
		final AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();

		page.add(new WebComponent(MockPageWithLinkAndComponent.COMPONENT_ID)
				.setOutputMarkupId(true));


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

		// first render sets timeout
		assertMatches("setTimeout", 1);

		tester.executeBehavior(timer);

		assertMatches("setTimeout", 1);
	}


	/**
	 * tests timer behavior in a WebPage.
	 */
	@Test
	public void pageRenderSetsTimeout()
	{
		Duration dur = Duration.seconds(20);
		final AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(dur);
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

		assertMatches("setTimeout", 1);

		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		assertMatches("setTimeout", 1);

		tester.executeBehavior(timer);

		assertMatches("setTimeout", 1);
	}

	/**
	 * tests timer behavior in a WebPage.
	 */
	@Test
	public void ajaxUpdateDoesNotSetTimeout()
	{
		Duration dur = Duration.seconds(20);
		final AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();
		final Label label = new Label(MockPageWithLinkAndComponent.COMPONENT_ID, "Hello");
		page.add(label);
		page.add(new AjaxLink<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				target.add(label);
			}
		});
		label.setOutputMarkupId(true);
		label.add(timer);

		tester.startPage(page);

		assertMatches("setTimeout", 1);

		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		// ajax update does not set timeout
		assertMatches("setTimeout", 0);

		tester.executeBehavior(timer);

		assertMatches("setTimeout", 1);
	}


	/**
	 */
	@Test
	public void setVisibleSetsTimeout()
	{
		Duration dur = Duration.seconds(20);
		final AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(dur);
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();
		final Label label = new Label(MockPageWithLinkAndComponent.COMPONENT_ID, "Hello");
		page.add(label);
		page.add(new AjaxLink<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
			}
		});
		label.setOutputMarkupId(true);
		label.setVisible(false);
		label.add(timer);

		tester.startPage(page);

		assertMatches("setTimeout", 0);

		tester.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		assertMatches("setTimeout", 0);

		label.setVisible(true);

		tester.startPage(page);

		// no visible, so timeout is set
		assertMatches("setTimeout", 1);
	}

	/**
	 */
	@Test
	public void setDisabledClearsTimeout()
	{
		final AbstractAjaxTimerBehavior timer = new AbstractAjaxTimerBehavior(Duration.seconds(20))
		{
			private boolean enabled = true;

			@Override
			protected void onTimer(AjaxRequestTarget target)
			{
				enabled = false;
			}

			@Override
			public boolean isEnabled(Component component)
			{
				return enabled;
			}
		};
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();
		final Label label = new Label(MockPageWithLinkAndComponent.COMPONENT_ID, "Hello");
		page.add(label);
		page.add(new Link<Void>(MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
			}
		});
		label.setOutputMarkupId(true);
		label.add(timer);

		tester.startPage(page);

		assertMatches("setTimeout", 1);

		tester.executeBehavior(timer);

		assertMatches("clearTimeout", 1);
		assertMatches("setTimeout", 0);
	}

	/**
	 * Validates the response, then makes sure the timer injects itself again
	 * when called. Tests
	 * {@link AbstractAjaxTimerBehavior#restart(AjaxRequestTarget)} method
	 * 
	 * WICKET-1525, WICKET-2152
	 */
	@Test
	public void restartResultsInAddTimeout()
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

		assertMatches("setTimeout", 1);
		// label is updated automatically (this will no longer be the case in
		// Wicket 7.x)
		assertMatches("wicket:id=\"component\"", 1);

		// increment to 2
		tester.executeBehavior(timerBehavior);

		// assert label is now 2
		tester.assertLabel(labelPath, String.valueOf(labelInitialValue + 2));
	}

	/**
	 * Validates the reponse, then makes sure the timer injects itself again
	 * when called.
	 * 
	 * @param timer
	 * @param wasAjax
	 */
	private void assertMatches(String string, int count)
	{
		String document = tester.getLastResponseAsString();

		log.debug(document);

		int found = 0;
		int lastIndex = 0;
		while (true)
		{
			lastIndex = document.indexOf(string, lastIndex);

			if (lastIndex == -1)
			{
				break;
			}

			found++;
			lastIndex += string.length();
		}

		assertEquals(count, found);
	}
}