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

import org.apache.wicket.AbstractRestartResponseException;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.WicketTestCase;


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
	public static class AbortAndRespondPage1 extends Page
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public AbortAndRespondPage1()
		{
			throw new AbstractRestartResponseException()
			{

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			};
		}
	}

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage2 extends Page
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param params
		 */
		public AbortAndRespondPage2(PageParameters params)
		{
			throw new AbstractRestartResponseException()
			{

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			};
		}

	}

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage3 extends Page
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		public AbortAndRespondPage3()
		{
			throw new AbstractRestartResponseException()
			{

				/**
				 * 
				 */
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
			throw new AbstractRestartResponseException()
			{

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

			};
		}

	}

	public static class PageThrowingCheckedException extends Page
	{
		private static final long serialVersionUID = 1L;

		public static final Exception EXCEPTION = new Exception("a checked exception");

		public PageThrowingCheckedException() throws Exception
		{
			throw EXCEPTION;
		}
	}


	final private IPageFactory pageFactory = new DefaultPageFactory();

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public DefaultPageFactoryTest(String name)
	{
		super(name);
	}

	/**
	 * Verifies page factory bubbles AbortAndRespondException
	 */
	public void testAbortAndRespondContract()
	{
		try
		{
			tester.setupRequestAndResponse();
			tester.createRequestCycle();
			pageFactory.newPage(AbortAndRespondPage1.class);
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage2.class);
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage2.class, new PageParameters());
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage3.class);
			fail();
		}
		catch (AbstractRestartResponseException e)
		{
			// noop
		}

		try
		{
			pageFactory.newPage(AbortAndRespondPage3.class, new PageParameters());
			fail();
		}
		catch (AbstractRestartResponseException e)
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
