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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.BehaviorsUtil;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
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
	 * Searches the {@link MarkupContainer}, looking for and triggering {@link AjaxLazyLoadPanel}s
	 * to fetch their contents. Very useful for testing pages / panels that use
	 * {@link AjaxLazyLoadPanel}s.
	 * 
	 * @param wt
	 *            the {@link WicketTester} to execute the behaviour (
	 *            {@link WicketTester#executeBehavior} ).
	 * @param container
	 *            contains the {@link AjaxLazyLoadPanel} to trigger
	 */
	public static void executeAjaxLazyLoadPanel(final WicketTester wt, MarkupContainer container)
	{
		container.visitChildren(AjaxLazyLoadPanel.class, new IVisitor<AjaxLazyLoadPanel, Void>()
		{
			public void component(AjaxLazyLoadPanel component, final IVisit<Void> visit)
			{
				// get the AbstractAjaxBehaviour which is responsible for
				// getting the contents of the lazy panel
				List<IBehavior> behaviors = BehaviorsUtil.getBehaviors(component,
					AbstractAjaxBehavior.class);
				if (behaviors.size() == 0)
				{
					logger.warn("AjaxLazyLoadPanel child found, but no attached AbstractAjaxBehaviors found. A curious situation...");
				}
				for (IBehavior b : behaviors)
				{
					if (b instanceof AbstractAjaxBehavior &&
						!(b instanceof AjaxSelfUpdatingTimerBehavior))
					{
						// tell wicket tester to execute it :)
						logger.debug("Triggering lazy panel: " + component.getClassRelativePath());
						AbstractAjaxBehavior abstractAjaxBehaviour = (AbstractAjaxBehavior)b;
						wt.executeBehavior(abstractAjaxBehaviour);
					}
				}
				// continue looking for other AjazLazyLoadPanel
			}
		});
	}


}
