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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.flow.ResetResponseException;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Default page factory tests
 * 
 * @author ivaynberg
 */
class DefaultPageFactoryTest extends WicketTestCase
{
	final private IPageFactory pageFactory = new DefaultPageFactory();

	@Test
	void throwExceptionInConstructor()
	{
		Exception e = assertThrows(WicketRuntimeException.class, () -> {
			pageFactory.newPage(ThrowExceptionInConstructorPage.class);
		});

		assertEquals(
			"Can't instantiate page using constructor 'public org.apache.wicket.session.DefaultPageFactoryTest$ThrowExceptionInConstructorPage()'. An exception has been thrown during construction!",
			e.getMessage());
	}

	@Test
	void privateConstructor()
	{
		Exception e = assertThrows(WicketRuntimeException.class, () -> {
			pageFactory.newPage(PrivateDefaultConstructorPage.class);
		});

		assertEquals(
			"Can't instantiate page using constructor 'private org.apache.wicket.session.DefaultPageFactoryTest$PrivateDefaultConstructorPage()'. This constructor is private!",
			e.getMessage());
	}

	@Test
	void privateConstructorWithParameters()
	{

		PageParameters parameters = new PageParameters();
		parameters.add("key", "value");


		Exception e = assertThrows(WicketRuntimeException.class, () -> {
			pageFactory.newPage(PrivateConstructorWithParametersPage.class, parameters);
		});

		assertEquals(
			"Can't instantiate page using constructor 'private org.apache.wicket.session.DefaultPageFactoryTest$PrivateConstructorWithParametersPage(org.apache.wicket.request.mapper.parameter.PageParameters)' and argument 'key=[value]'. This constructor is private!",
			e.getMessage());
	}

	@Test
	void nonDefaultConstructor()
	{
		Exception e = assertThrows(WicketRuntimeException.class, () -> {
			pageFactory.newPage(NonDefaultConstructorPage.class);
		});

		assertEquals(
			"Unable to create page from class org.apache.wicket.session.DefaultPageFactoryTest$NonDefaultConstructorPage. Class does not have a visible default constructor.",
			e.getMessage());
	}

	/**
	 * Verifies page factory bubbles ResetResponseException
	 */
	@Test
	void abortAndRespondContract()
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

	/**
	 * @author ivaynberg
	 */
	public static class AbortAndRespondPage1 extends WebPage
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 */
		AbortAndRespondPage1()
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
		AbortAndRespondPage2(PageParameters params)
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
		AbortAndRespondPage3()
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
		AbortAndRespondPage3(PageParameters params)
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
		/**	 */
		static final Exception EXCEPTION = new Exception("a checked exception");
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 *
		 * @throws Exception
		 */
		PageThrowingCheckedException() throws Exception
		{
			throw EXCEPTION;
		}
	}

    public static class PrivateDefaultConstructorPage extends WebPage
	{
		private PrivateDefaultConstructorPage()
		{
		}
	}

    public static class PrivateConstructorWithParametersPage extends WebPage
	{
        private PrivateConstructorWithParametersPage(PageParameters parameters)
		{
			super(parameters);
		}
	}

    public static class NonDefaultConstructorPage extends WebPage
	{
        public NonDefaultConstructorPage(String aa)
		{
			super();
		}
	}

    public static class ThrowExceptionInConstructorPage extends WebPage
	{
        public ThrowExceptionInConstructorPage()
		{

			throw new RuntimeException("exception!");
		}
	}
}
