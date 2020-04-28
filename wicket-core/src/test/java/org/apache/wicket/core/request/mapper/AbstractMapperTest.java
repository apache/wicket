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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;

/**
 * @author Matej Knopp
 */
abstract class AbstractMapperTest
{
	/**
	 * Construct.
	 */
	AbstractMapperTest()
	{
	}

	TestMapperContext context = new TestMapperContext();

	Request getRequest(final Url url)
	{
		return new Request()
		{
			@Override
			public Url getUrl()
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
				return StandardCharsets.UTF_8;
			}

			@Override
			public Url getClientUrl()
			{
				return url;
			}

			@Override
			public Object getContainerRequest()
			{
				return null;
			}
		};
	}

	void checkPage(IRequestablePage page, int id)
	{
		assertEquals(id, page.getPageId());
	}

}
