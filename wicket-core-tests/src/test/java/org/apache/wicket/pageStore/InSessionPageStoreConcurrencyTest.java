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
package org.apache.wicket.pageStore;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.page.PageManager;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InSessionPageStoreConcurrencyTest
{
	public static class PageStoringWicketTester extends WicketTester
	{
		final static InSessionPageStore session = new InSessionPageStore(10, new JavaSerializer("key"));

		public PageStoringWicketTester(final WebApplication application, final boolean init)
		{
			super(application, init);
		}

		@Override
		protected IPageManagerProvider newTestPageManagerProvider()
		{
			return () ->
					new PageManager(session);
		}
	}

	@Test
	public void testConcurrentModifications() throws Exception
	{
		final WebApplication application = new MockApplication();

		final MockHttpSession httpSession = new MockHttpSession(new MockServletContext(application, ""));

		//prepare servlet context
		final WicketTester wicketTester = new PageStoringWicketTester(application, true)
		{
			@Override
			public MockHttpSession getHttpSession()
			{
				return httpSession;
			}
		};
		wicketTester.executeUrl("/");


		ExecutorService executor = Executors.newFixedThreadPool(1000);

		Callable<Void> executeURLTask = () -> {
			final WicketTester parallelWicketTester;
			synchronized (application)
			{
				parallelWicketTester = new PageStoringWicketTester(application, false)
				{
					@Override
					public MockHttpSession getHttpSession()
					{
						return httpSession;
					}
				};
			}

			parallelWicketTester.executeUrl("/");
			return null;
		};

		Callable<Void> serializeSessionTask = () -> {
			final NullOutputStream nullOutputStream = NullOutputStream.NULL_OUTPUT_STREAM;
			try(final ObjectOutputStream objectOutputStream = new ObjectOutputStream(nullOutputStream))
			{
				objectOutputStream.writeObject(wicketTester.getSession());
			}
			catch(final IOException e)
			{
				throw new RuntimeException(e);
			}
			return null;
		};

		var tasks = Stream.concat(
				IntStream.rangeClosed(1, 500).mapToObj(i -> serializeSessionTask),
				IntStream.rangeClosed(1, 500).mapToObj(i -> executeURLTask)
		).collect(Collectors.toList());

		var futures = executor.invokeAll(tasks);

		futures.forEach(
				exceptionFuture -> {
					try
					{
						exceptionFuture.get();
					}
					catch (InterruptedException| ExecutionException e)
					{
						throw new RuntimeException(e);
					}
				});
	}

}
