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
package org.apache.wicket.markupdriventree.ivaynberg;

import org.apache.wicket.MarkupDrivenTreeInitializionListener;
import org.apache.wicket.Page;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

class TesterRule implements MethodRule
{
	private WebApplication application;
	private WicketTester tester;

	public TesterRule()
	{
	}

	public WicketTester getTester()
	{
		return tester;
	}

	public void startPage(Page page)
	{
		tester.startPage(page);
	}

	public <T extends Page> void startPage(Class<T> page)
	{
		tester.startPage(page);
	}


	@Override
	public Statement apply(final Statement base, FrameworkMethod method, Object target)
	{
		return new Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				WebApplication application = getApplication();
				if (application == null)
				{
					application = new MockApplication()
					{
						@Override
						protected void init()
						{
							super.init();
							getPageSettings().setMarkupDrivenComponentTreeEnabled(true);
							getComponentInitializationListeners().add(new MarkupDrivenTreeInitializionListener());
						}
					};
				}
				tester = new WicketTester(application);
				tester.setExposeExceptions(true);
				try
				{
					base.evaluate();
				}
				finally
				{
					tester.destroy();
					tester = null;
				}
			}
		};
	}

	public WebApplication getApplication()
	{
		return application;
	}

	public TesterRule setApplication(WebApplication application)
	{
		this.application = application;
		return this;
	}

	public Page getLastRenderedPage()
	{
		return tester.getLastRenderedPage();
	}

}
