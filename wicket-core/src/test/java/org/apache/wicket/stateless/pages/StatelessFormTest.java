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
package org.apache.wicket.stateless.pages;

import org.apache.wicket.Page;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * User: Anatoly Kupriyanov (kan.izh@gmail.com) Date: 12-Feb-2009 Time: 22:27:08 Bugfix for
 * http://issues.apache.org/jira/browse/WICKET-1897
 */
public class StatelessFormTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				mountPage("page1", Page1.class);
				mountPage("page2", Page2.class);
			}

			@Override
			public Class<? extends Page> getHomePage()
			{
				return Page1.class;
			}

		};
	}

	/**
	 * testBug()
	 */
	@Test
	public void bug()
	{
		{
			tester.getRequest().setUrl(Url.parse("page2"));
			tester.processRequest();
			tester.assertRenderedPage(Page2.class);
		}
		{
			tester.getRequest().setUrl(Url.parse("page1"));
			tester.processRequest();
			tester.assertRenderedPage(Page1.class);
		}
		{
			tester.getRequest().setUrl(Url.parse("page1?1-1.IFormSubmitListener-form"));
			tester.processRequest();
			tester.assertRenderedPage(Page1.class);
		}
	}
}
