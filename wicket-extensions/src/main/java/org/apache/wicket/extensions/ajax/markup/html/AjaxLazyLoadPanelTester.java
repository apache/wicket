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
package org.apache.wicket.extensions.ajax.markup.html;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel.AjaxLazyLoadTimer;
import org.apache.wicket.util.tester.BaseWicketTester;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to help test {@link AjaxLazyLoadPanel}
 * 
 * @author Antony Stubbs
 */
public class AjaxLazyLoadPanelTester
{
	private static final Logger logger = LoggerFactory.getLogger(AjaxLazyLoadPanelTester.class);

	/**
	 * Triggers loading of all {@link AjaxLazyLoadPanel}'s content in the last rendered page.
	 * 
	 * @param wt
	 *            the tester
	 */
	public static void executeAjaxLazyLoadPanel(final BaseWicketTester wt)
	{
		executeAjaxLazyLoadPanel(wt, wt.getLastRenderedPage());
	}

	/**
	 * Triggers loading of all {@link AjaxLazyLoadPanel}'s content in a page.
	 * 
	 * @param wt
	 *            the tester
	 * @param page
	 *            contains the {@link AjaxLazyLoadPanel}s to trigger
	 */
	public static void executeAjaxLazyLoadPanel(final BaseWicketTester wt, final Page page)
	{
		// get the AbstractAjaxBehaviour which is responsible for
		// getting the contents of the lazy panel
		List<AjaxLazyLoadTimer> behaviors = page.getBehaviors(AjaxLazyLoadTimer.class);
		if (behaviors.size() == 0)
		{
			logger.warn("No timer behavior for AjaxLazyLoadPanel found. A curious situation...");
			return;
		}
		else if (behaviors.size() > 1)
		{
			logger.warn(
				"Multiple timer behavior for AjaxLazyLoadPanel found. A curious situation...");
		}

		wt.executeBehavior(behaviors.get(0));
	}

	/**
	 * Triggers loading of a single {@link AjaxLazyLoadPanel}.
	 * 
	 * @param wt
	 *            the tester
	 * @param panel
	 *            the panel
	 * @return	update duration or {@value null} of already loadedO
	 */
	public static Duration loadAjaxLazyLoadPanel(final BaseWicketTester wt, final AjaxLazyLoadPanel<?> panel)
	{
		if (panel.isLoaded())
		{
			return null;
		}
		else
		{
			return panel.getUpdateInterval();
		}
	}
}
