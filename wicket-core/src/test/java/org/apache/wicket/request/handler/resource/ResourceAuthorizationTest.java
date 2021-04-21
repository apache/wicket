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
package org.apache.wicket.request.handler.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests authorization of IResources
 */
class ResourceAuthorizationTest extends WicketTestCase
{
	private static class RejectingAuthorizationStrategy extends IAuthorizationStrategy.AllowAllAuthorizationStrategy
	{
		@Override
		public boolean isResourceAuthorized(IResource resource, PageParameters pageParameters)
		{
			return false;
		}
	}

	private static class TestResource extends AbstractResource
	{
		@Override
		protected ResourceResponse newResourceResponse(Attributes attributes)
		{
			return null;
		}

		@Override
		public String toString()
		{
			return "TestResource";
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5012
	 */
	@Test
	void rejectWith403()
	{
		tester.getApplication().getSecuritySettings().setAuthorizationStrategy(new RejectingAuthorizationStrategy());

		tester.startResource(new TestResource());

		assertEquals(HttpServletResponse.SC_FORBIDDEN, tester.getLastResponse().getStatus());
		assertEquals("The request to resource 'TestResource' with parameters '' cannot be authorized.",
				tester.getLastResponse().getErrorMessage());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5012
	 */
	@Test
	void rejectWithException()
	{
		tester.getApplication().getSecuritySettings().setAuthorizationStrategy(new RejectingAuthorizationStrategy());
		tester.getApplication().getSecuritySettings().setUnauthorizedResourceRequestListener((resource, parameters) -> {
			throw new RuntimeException("Not authorized to request: " + resource);
		});

		TestResource resource = new TestResource();

		Exception e = assertThrows(RuntimeException.class, () -> {
			tester.startResource(resource);
		});

		assertEquals("Not authorized to request: " + resource, e.getMessage());
	}
}
