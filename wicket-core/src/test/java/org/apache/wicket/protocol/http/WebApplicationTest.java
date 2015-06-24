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

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.request.mapper.CompoundRequestMapper;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;
import org.apache.wicket.request.mapper.IRequestMapperDelegate;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test WebApplication
 */
public class WebApplicationTest extends WicketTestCase
{
	private static final String MOUNT_PATH_1 = "mount/path/1";
	private static final String MOUNT_PATH_2 = "mount/path/2";
	private static final String MOUNT_PATH_3 = "mount/path/3";
	private static final String MOUNT_PATH_4 = "mount/path/4";

	/**
	 * Test basic unmounting from a compound mapper.
	 */
	@Test
	public void testUnmountSimple()
	{
		CompoundRequestMapper compound = new CompoundRequestMapper();

		compound.add(tester.getApplication().getRootRequestMapper());

		compound.add(new MountMapper(MOUNT_PATH_1, new EmptyRequestHandler()));
		compound.add(new MountMapper(MOUNT_PATH_2, new EmptyRequestHandler()));
		compound.add(new MountMapper(MOUNT_PATH_3, new EmptyRequestHandler()));

		tester.getApplication().setRootRequestMapper(compound);

		tester.getApplication().unmount(MOUNT_PATH_1);

		assertEquals("Compound size should be 3", 3, getCompoundRequestMapperSize(compound));

		assertNull("Mount path 1 should not be mounted",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_1)));

		assertTrue("Mount path 2 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_2)) instanceof EmptyRequestHandler);

		assertTrue("Mount path 3 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_3)) instanceof EmptyRequestHandler);
	}

	/**
	 * See https://issues.apache.org/jira/browse/WICKET-5698
	 */
	@Test
	public void testUnmountComplex()
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

		assertEquals("Compound size should be 2", 2, getCompoundRequestMapperSize(nestedCompound));

		assertNull("Mount path 1 should not be mounted",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_1)));

		assertTrue("Mount path 2 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_2)) instanceof EmptyRequestHandler);

		assertTrue("Mount path 3 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_3)) instanceof EmptyRequestHandler);

		assertTrue("Mount path 4 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_4)) instanceof EmptyRequestHandler);

		tester.getApplication().unmount(MOUNT_PATH_3);

		assertNull("Mount path 1 should not be mounted",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_1)));

		assertTrue("Mount path 2 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_2)) instanceof EmptyRequestHandler);

		assertNull("Mount path 3 should not be mounted",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_3)));

		assertTrue("Mount path 4 should match",
			tester.getApplication().getRootRequestMapper().mapRequest(createMockRequest(MOUNT_PATH_4)) instanceof EmptyRequestHandler);
	}

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

	private static class MountMapper implements IRequestMapper
	{
		private final String path;
		private final IRequestHandler handler;

		public MountMapper(String path, EmptyRequestHandler handler)
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

		public SimpleRequestMapperDelegate(IRequestMapper delegate)
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
