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
package org.apache.wicket.protocol.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.mock.MockRequestParameters;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;
import org.apache.wicket.request.mapper.IRequestMapperDelegate;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test WebApplication
 */
class WebApplicationTest extends WicketTestCase
{
	private static final String MOUNT_PATH_1 = "mount/path/1";
	private static final String MOUNT_PATH_2 = "mount/path/2";
	private static final String MOUNT_PATH_3 = "mount/path/3";
	private static final String MOUNT_PATH_4 = "mount/path/4";

	private static Request createMockRequest(String path)
	{
		final Url url = Url.parse(path);

		return new Request()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public Url getClientUrl()
			{
				return url;
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}

			@Override
			public Charset getCharset()
			{
				return null;
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};
	}

	private static int getCompoundRequestMapperSize(ICompoundRequestMapper compound)
	{
		int retv = 0;

		for (Iterator<IRequestMapper> it = compound.iterator(); it.hasNext();)
		{
			it.next();
			retv++;
		}

		return retv;
	}

	/**
	 * WICKET-6260
	 */
	@Test
	void testBodyNotReadBeforeApplicationSetsCharacterEncoding() throws Exception
	{
		WebApplication application = tester.getApplication();

		HttpServletRequest request = new MockHttpServletRequest(application, null, null)
		{
			@Override
			public Map<String, String[]> getParameterMap()
			{
				fail("body should not be read before character encoding is set");
				return null;
			}

			@Override
			public String getParameter(String name)
			{
				fail("body should not be read before character encoding is set");
				return null;
			}

			@Override
			public Enumeration<String> getParameterNames()
			{
				fail("body should not be read before character encoding is set");
				return null;
			}

			@Override
			public String[] getParameterValues(String name)
			{
				fail("body should not be read before character encoding is set");
				return null;
			}

			@Override
			public MockRequestParameters getPostParameters()
			{
				fail("body should not be read before character encoding is set");
				return null;
			}
		};

		// character encoding not set yet
		request.setCharacterEncoding(null);

		application.createWebRequest(request, "/");

		assertEquals("UTF-8", request.getCharacterEncoding());
	}

	/**
	 * Test basic unmounting from a compound mapper.
	 */
	@Test
	void testUnmountSimple()
	{
		CompoundRequestMapper compound = new CompoundRequestMapper();

		compound.add(tester.getApplication().getRootRequestMapper());

		compound.add(new MountMapper(MOUNT_PATH_1, new EmptyRequestHandler()));
		compound.add(new MountMapper(MOUNT_PATH_2, new EmptyRequestHandler()));
		compound.add(new MountMapper(MOUNT_PATH_3, new EmptyRequestHandler()));

		tester.getApplication().setRootRequestMapper(compound);

		tester.getApplication().unmount(MOUNT_PATH_1);

		assertEquals(3, getCompoundRequestMapperSize(compound), "Compound size should be 3");

		assertNull(tester.getApplication().getRootRequestMapper().mapRequest(
			createMockRequest(MOUNT_PATH_1)), "Mount path 1 should not be mounted");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_2)) instanceof EmptyRequestHandler,
			"Mount path 2 should match");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_3)) instanceof EmptyRequestHandler,
			"Mount path 3 should match");
	}

	/**
	 * See https://issues.apache.org/jira/browse/WICKET-5698
	 */
	@Test
	void testUnmountComplex()
	{
		CompoundRequestMapper nestedCompound = new CompoundRequestMapper();

		nestedCompound.add(tester.getApplication().getRootRequestMapper());

		nestedCompound.add(new MountMapper(MOUNT_PATH_1, new EmptyRequestHandler()));
		nestedCompound.add(new MountMapper(MOUNT_PATH_2, new EmptyRequestHandler()));

		CompoundRequestMapper rootCompound = new CompoundRequestMapper();

		rootCompound.add(new SimpleRequestMapperDelegate(nestedCompound));

		rootCompound.add(new MountMapper(MOUNT_PATH_3, new EmptyRequestHandler()));
		rootCompound.add(new MountMapper(MOUNT_PATH_4, new EmptyRequestHandler()));

		tester.getApplication().setRootRequestMapper(new SimpleRequestMapperDelegate(rootCompound));

		tester.getApplication().unmount(MOUNT_PATH_1);

		assertEquals(2, getCompoundRequestMapperSize(nestedCompound), "Compound size should be 2");
		assertNull(tester.getApplication().getRootRequestMapper().mapRequest(
			createMockRequest(MOUNT_PATH_1)), "Mount path 1 should not be mounted");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_2)) instanceof EmptyRequestHandler,
			"Mount path 2 should match");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_3)) instanceof EmptyRequestHandler,
			"Mount path 3 should match");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_4)) instanceof EmptyRequestHandler,
			"Mount path 4 should match");

		tester.getApplication().unmount(MOUNT_PATH_3);

		assertNull(tester.getApplication().getRootRequestMapper().mapRequest(
			createMockRequest(MOUNT_PATH_1)), "Mount path 1 should not be mounted");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_2)) instanceof EmptyRequestHandler,
			"Mount path 2 should match");

		assertNull(tester.getApplication().getRootRequestMapper().mapRequest(
			createMockRequest(MOUNT_PATH_3)), "Mount path 3 should not be mounted");

		assertTrue(
			tester.getApplication().getRootRequestMapper().mapRequest(
				createMockRequest(MOUNT_PATH_4)) instanceof EmptyRequestHandler,
			"Mount path 4 should match");
	}

	private static class MountMapper implements IRequestMapper
	{
		private final String path;
		private final IRequestHandler handler;

		MountMapper(String path, EmptyRequestHandler handler)
		{
			this.path = path;
			this.handler = handler;
		}

		@Override
		public IRequestHandler mapRequest(Request request)
		{
			if (request.getUrl().toString().equals(path))
			{
				return handler;
			}
			return null;
		}

		@Override
		public int getCompatibilityScore(Request request)
		{
			return 0;
		}

		@Override
		public Url mapHandler(IRequestHandler requestHandler)
		{
			return null;
		}
	}

	private static class SimpleRequestMapperDelegate implements IRequestMapperDelegate
	{
		private final IRequestMapper delegate;

		SimpleRequestMapperDelegate(IRequestMapper delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public IRequestMapper getDelegateMapper()
		{
			return delegate;
		}

		@Override
		public IRequestHandler mapRequest(Request request)
		{
			return delegate.mapRequest(request);
		}

		@Override
		public int getCompatibilityScore(Request request)
		{
			return delegate.getCompatibilityScore(request);
		}

		@Override
		public Url mapHandler(IRequestHandler requestHandler)
		{
			return delegate.mapHandler(requestHandler);
		}
	}
}
