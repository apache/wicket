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
package org.apache.wicket.queueing.transparentresolvers;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.Test;

public class DequeueingTransparentWebMarkupContainerTest extends WicketTestCase
{
	/**
	 * Test case for:
	 *  - https://issues.apache.org/jira/browse/WICKET-5572
	 *  - https://issues.apache.org/jira/browse/WICKET-5722
	 */
	@Test
	public void startSubPageWithTWMCinTheParentPage() 
	{
		tester.startPage(SubPage.class);
		tester.assertRenderedPage(SubPage.class);
		tester.assertComponent("html", HtmlTag.class);
	}
	
	/**
	 * https://issues.apache.org/jira/browse/WICKET-5724
	 * 
	 * Transparent component inside page body must allow 
	 * queued children components.
	 */
	@Test
	public void queuedComponentsInsideTransparentContainer()
	{
		tester.startPage(TransparentContainerQueuePage.class);
		tester.assertRenderedPage(TransparentContainerQueuePage.class);
		
		Page lastRenderedPage = tester.getLastRenderedPage();
		
		//test if page contains the queued label
		boolean containsQueuedLabel = lastRenderedPage.visitChildren(new IVisitor<Component, Boolean>()
		{
			@Override
			public void component(Component component, IVisit<Boolean> visit)
			{
				if(component instanceof Label)
				{
					visit.stop(true);
				}
			}
		});
		
		assertTrue(containsQueuedLabel);
	}
}
