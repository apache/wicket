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
package org.apache.wicket.util.file;

import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.wicket.util.resource.IResourceStream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * @since 1.5.5
 */
public class WebApplicationPathTest extends Assert
{
	@Test
	public void doNotServeResourcesFromWebInfEvenIfRootIsAdded() throws Exception
	{
		URL webUrl = new URL("file://dummyFile");

		ServletContext context = Mockito.mock(ServletContext.class);
		Class<String> scope = String.class;
		Mockito.when(context.getResource(Matchers.any(scope))).thenReturn(webUrl);

		WebApplicationPath path = new WebApplicationPath(context);
		path.addToWebPath("/");
		IResourceStream resourceStream = path.find(scope, "WEB-INF/web.xml");
		assertNull(resourceStream);

		IResourceStream resourceStreamWithLeadingSlash = path.find(scope, "/WEB-INF/web.xml");
		assertNull(resourceStreamWithLeadingSlash);

		IResourceStream otherResourceStream = path.find(scope, "any/other/resource");
		assertNotNull(otherResourceStream);
		IResourceStream otherResourceStreamWithLeadingSlash = path.find(scope,
			"/any/other/resource");
		assertNotNull(otherResourceStreamWithLeadingSlash);

	}
}
