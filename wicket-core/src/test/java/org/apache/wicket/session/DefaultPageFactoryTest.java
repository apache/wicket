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
package org.apache.wicket.session;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.flow.ResetResponseException;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Test;


/**
 * Default page facotry tests
 * 
 * @author ivaynberg
 */
public class DefaultPageFactoryTest extends WicketTestCase
{
	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public AbortAndRespondPage1()
		{
			throw new ResetResponseException(new EmptyRequestHandler())
			{
				private static final long serialVersionUID = 1L;
			};
		}
	}

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage2 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param params
		 */
		public AbortAndRespondPage2(PageParameters params)
		{
			throw new ResetResponseException(new EmptyRequestHandler())
			{
				private static final long serialVersionUID = 1L;
			};
		}
	}

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage3 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public AbortAndRespondPage3()
		{
			throw new ResetResponseException(new EmptyRequestHandler())
			{
				private static final long serialVersionUID = 1L;
			};
		}

		/**
		 * Construct.
		 * 
		 * @param params
		 */
		public AbortAndRespondPage3(PageParameters params)
		{
			throw new ResetResponseException(new EmptyRequestHandler())
			{
				private static final long serialVersionUID = 1L;
			};
		}

	}

	/**
	 */
	public static class PageThrowingCheckedException extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**	 */
		public static final Exception EXCEPTION = new Exception("a checked exception");

		/**
		 * Construct.
		 * 
		 * @throws Exception
		 */
		public PageThrowingCheckedException() throws Exception
		{
			throw EXCEPTION;
		}
	}


	final private IPageFactory pageFactory = new DefaultPageFactory();

	/**
	 * Verifies page factory bubbles AbortAndRespondException
	 */
	@Test
	public void abortAndRespondContract()
	{
		try
		{
			pageFactory.newPage(AbortAndRespondPage1.class);
			fail();
		}
		catch (ResetResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage2.class);
			fail();
		}
		catch (ResetResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage2.class, new PageParameters());
			fail();
		}
		catch (ResetResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage3.class);
			fail();
		}
		catch (ResetResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage3.class, new PageParameters());
			fail();
		}
		catch (ResetResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(PageThrowingCheckedException.class);
			fail();
		}
		catch (WicketRuntimeException e)
		{
			assertNotNull(e.getCause());
			assertNotNull(e.getCause().getCause());
			assertEquals(PageThrowingCheckedException.EXCEPTION, e.getCause().getCause());
		}
		catch (Exception e)
		{
			fail();
		}
	}
}
