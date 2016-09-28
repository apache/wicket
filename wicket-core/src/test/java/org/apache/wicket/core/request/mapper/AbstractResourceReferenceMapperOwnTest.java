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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.resource.ResourceUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for AbstractResourceReferenceMapper's own methods
 */
public class AbstractResourceReferenceMapperOwnTest extends Assert
{
	@Test
	public void testEscapeAttributesSeparator() throws Exception
	{
		AbstractResourceReferenceMapper mapper = new Mapper();
		CharSequence escaped = ResourceUtil.escapeAttributesSeparator("my-style~is~~cool");
		assertEquals("my~style~~is~~~~cool", escaped.toString());
	}

	@Test
	public void testUnescapeAttributesSeparator() throws Exception
	{
		AbstractResourceReferenceMapper mapper = new Mapper();
		CharSequence escaped = ResourceUtil.unescapeAttributesSeparator("my~style~~is~~~~cool");
		assertEquals("my-style~is~~cool", escaped.toString());
	}

	/**
	 * A non-abstract class used for the tests
	 */
	private static class Mapper extends AbstractResourceReferenceMapper
	{
		public IRequestHandler mapRequest(Request request)
		{
			return null;
		}

		public int getCompatibilityScore(Request request)
		{
			return 0;
		}

		public Url mapHandler(IRequestHandler requestHandler)
		{
			return null;
		}
	}
}
