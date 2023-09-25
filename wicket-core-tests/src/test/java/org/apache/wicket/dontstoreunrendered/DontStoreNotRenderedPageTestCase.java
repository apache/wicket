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
package org.apache.wicket.dontstoreunrendered;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.wicket.Component;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.mock.MockPageManager;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

/**
 * https://issues.apache.org/jira/browse/WICKET-5415
 */
public abstract class DontStoreNotRenderedPageTestCase extends WicketTestCase
{
	@Override
	protected WicketTester newWicketTester(WebApplication app)
	{
		app.getComponentInstantiationListeners().add(new IComponentInstantiationListener()
		{
			@Override
			public void onInstantiation(Component component)
			{
				// WICKET-5546 behavior added before Page#init()
				component.add(new Behavior()
				{
				});
			}
		});

		return new WicketTester(app)
		{
			@Override
			protected IPageManagerProvider newTestPageManagerProvider()
			{
				return () -> {
					return new MockPageManager()
					{
						@Override
						public void touchPage(IManageablePage page)
						{
							assertFalse(page instanceof PageB, "PageB should not be touched!");
							super.touchPage(page);
						}
					};
				};
			}
		};
	}

	/**
	 * Start with PageA.
	 * Then click a link to go to PageB.
	 * PageB throws a RestartResponseException(PageC) in its constructor, so
	 * it shouldn't be neither initialized nor rendered.
	 * PageC is rendered.
	 *
	 * Verifies that PageB is not initialized, rendered and stored by PageManager
	 */
	@Test
	void dontStoreNotRenderedPage()
	{
		tester.startPage(PageA.class);
		tester.clickLink("goToB");

		tester.assertRenderedPage(PageC.class);

		assertFalse(PageB.PAGE_B_RENDERED.get());
		assertFalse(PageB.PAGE_B_INITIALIZED.get());
	}

}
