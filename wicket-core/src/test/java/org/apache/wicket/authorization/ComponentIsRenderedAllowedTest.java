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
package org.apache.wicket.authorization;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Checks whether or not authorization strategy blocks rendering of components
 * 
 * @author igor
 */
public class ComponentIsRenderedAllowedTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new SecuredApplication();
	}

	/** */
	@Test
	public void onBeforeRenderNotCalledOnVetoedComponents()
	{
		TestPage page = new TestPage();
		tester.startPage(page);
		assertFalse(page.normal.onBeforeRenderCalled);
	}

	/** */
	@Test
	public void vetoedComponentNotRendered()
	{
		TestPage page = new TestPage();
		tester.startPage(page);
		assertFalse(page.normal.onAfterRenderCalled);
	}

	/** */
	public class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private final NormalContainer normal;

		/** */
		public TestPage()
		{
			ForbiddenContainer forbidden = new ForbiddenContainer("forbidden");
			normal = new NormalContainer("normal");
			add(forbidden);
			forbidden.add(normal);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id='forbidden'><div wicket:id='normal'></div></div></body></html>");
		}

	}

	private static class NormalContainer extends WebMarkupContainer
	{

		private boolean onBeforeRenderCalled = false;
		private boolean onAfterRenderCalled = false;

		public NormalContainer(String id)
		{
			super(id);
		}

		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			onBeforeRenderCalled = true;
		}

		@Override
		protected void onAfterRender()
		{
			super.onAfterRender();
			onAfterRenderCalled = true;
		}

	}

	private static class ForbiddenContainer extends WebMarkupContainer implements Forbidden
	{
		public ForbiddenContainer(String id)
		{
			super(id);
		}
	}

	private static class SecuredApplication extends MockApplication
	{
		@Override
		protected void init()
		{
			super.init();
			getSecuritySettings().setAuthorizationStrategy(new Authorizer());
		}
	}

	private static interface Forbidden
	{

	}

	private static class Authorizer extends IAuthorizationStrategy.AllowAllAuthorizationStrategy
	{
		@Override
		public boolean isActionAuthorized(Component component, Action action)
		{
			return !(component instanceof Forbidden);
		}
	}

}
