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
package org.apache.wicket;

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.lang.Exceptions;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Ignore;

/**
 */
@Ignore
public class ExceptionMapperTest extends WicketTestCase
{
	/**
	 * Testing an custom exception mapper provider that return an wrapped exception mapper in order
	 * to catch special exceptions. It is important to enable other frameworks to plug in the
	 * application a custom response for their exceptions.
	 * 
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3256">WICKET-3256</a>
	 */
	public void exceptionMapper()
	{
		Application app = tester.getApplication();
		WrapperProvider wrapper = new WrapperProvider(app.getExceptionMapperProvider());
		// martin-g: WICKET-3806 IRequestCycleListener#onException() should be used by clients
// app.setExceptionMapperProvider(wrapper);
		tester.setExposeExceptions(false);
		tester.startPage(TestPage.class);
		tester.clickLink(MockPageWithLink.LINK_ID);
		tester.assertRenderedPage(TestExceptionPage.class);
	}

	/**
	 */
	public static class WrapperProvider implements IProvider<IExceptionMapper>
	{
		private IProvider<IExceptionMapper> wrapped;
		WrapperExceptionMapper wrapperExceptionMapper;

		/**
		 * @param wrapped
		 *            exception mapper provider
		 */
		public WrapperProvider(IProvider<IExceptionMapper> wrapped)
		{
			this.wrapped = wrapped;
		}

		@Override
		public IExceptionMapper get()
		{
			return wrapperExceptionMapper = new WrapperExceptionMapper(wrapped.get());
		}
	}

	/**
	 */
	public static class WrapperExceptionMapper implements IExceptionMapper
	{
		private IExceptionMapper wrapped;

		/**
		 * @param wrapped
		 *            IExceptionMapper
		 */
		public WrapperExceptionMapper(IExceptionMapper wrapped)
		{
			this.wrapped = wrapped;
		}

		@Override
		public IRequestHandler map(Exception e)
		{
			if (Exceptions.findCause(e, TestException.class) != null)
			{
				return new RenderPageRequestHandler(new PageProvider(TestExceptionPage.class));
			}
			return wrapped.map(e);
		}
	}


	/**
	 */
	public static class TestException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

	}

	/**
	 */
	public static class TestExceptionPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}

	/**
	 */
	public static class TestPage extends MockPageWithLink
	{
		private static final long serialVersionUID = 1L;

		/**
		 */
		public TestPage()
		{
			add(new Link<Void>(LINK_ID)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					throw new TestException();
				}
			});
		}
	}
}