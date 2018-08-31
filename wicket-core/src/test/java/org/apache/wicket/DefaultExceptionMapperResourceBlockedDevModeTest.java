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
package org.apache.wicket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.handler.ErrorCodeRequestHandler;
import org.apache.wicket.request.resource.PackageResource;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link org.apache.wicket.DefaultExceptionMapper}
 */
class DefaultExceptionMapperResourceBlockedDevModeTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				getExceptionSettings().setUnexpectedExceptionDisplay(
					ExceptionSettings.SHOW_NO_EXCEPTION_PAGE);
			}

			@Override
			public RuntimeConfigurationType getConfigurationType()
			{
				return RuntimeConfigurationType.DEVELOPMENT;
			}
		};
	}

	/**
	 * In development mode return http status 500 when a resource is blocked
	 * so the developer can see the problem early (the exception will be displayed
	 * on the screen)
	 */
	@Test
	void packageResourceBlockedExceptionDevMode()
	{
		DefaultExceptionMapper mapper = new DefaultExceptionMapper();
		PackageResource.PackageResourceBlockedException x =
				new PackageResource.PackageResourceBlockedException("test");
		IRequestHandler handler = mapper.map(x);
		assertThat(handler).isInstanceOf(ErrorCodeRequestHandler.class);
		ErrorCodeRequestHandler errorCodeRequestHandler = (ErrorCodeRequestHandler) handler;
		assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorCodeRequestHandler.getErrorCode());
	}
}
