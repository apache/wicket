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
package org.apache.wicket.request.resource;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link DynamicImageResource}
 */
public class DynamicImageResourceTest extends WicketTestCase
{

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3935
	 */
	@Test
	public void emptyImageDataIs404()
	{
		DynamicImageResource resource = new DynamicImageResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] getImageData(Attributes attributes)
			{
				return null;
			}
		};

		tester.startResource(resource);
		assertEquals(HttpServletResponse.SC_NOT_FOUND, tester.getLastResponse().getStatus());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3935
	 */
	@Test
	public void nonEmptyImageDataIs200()
	{
		final byte[] expected = new byte[] { 1, 2, 3 };

		DynamicImageResource resource = new DynamicImageResource()
		{
			@Override
			protected byte[] getImageData(Attributes attributes)
			{
				return expected;
			}
		};

		tester.startResource(resource);
		assertEquals(HttpServletResponse.SC_OK, tester.getLastResponse().getStatus());
		Assert.assertArrayEquals(expected, tester.getLastResponse().getBinaryContent());
	}
}
