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
package org.apache.wicket.markup.resolver.inparent;

import org.apache.wicket.markup.resolver.inparent.base.BasePage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test validating that an auto linked resource from the parent's page is properly resolved in the
 * child page
 */
public class AutoLinkResolveInParentsMarkupTest extends WicketTestCase
{

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{

			@Override
			public Class<HomePage> getHomePage()
			{
				return HomePage.class;
			}

			@Override
			public void init()
			{
				super.init();

				getMarkupSettings().setAutomaticLinking(true);

				mountPage("/home", HomePage.class);
				mountPage("/base", BasePage.class);
			}
		};
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4141
	 * 
	 * @throws Exception
	 */
	@Test
	public void resolveResourceFromMarkupInParent() throws Exception
	{
		tester.startPage(HomePage.class);
		tester.assertContains("wicket/resource/org.apache.wicket.markup.resolver.inparent.base.BasePage/theme/logo.png");
	}

}
