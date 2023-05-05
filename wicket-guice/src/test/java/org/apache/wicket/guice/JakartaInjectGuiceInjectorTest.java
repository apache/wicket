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
package org.apache.wicket.guice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.inject.Inject;

import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.junit.jupiter.api.Test;

import com.google.inject.ConfigurationException;
import com.google.inject.spi.Message;

/**
 */
public class JakartaInjectGuiceInjectorTest extends AbstractInjectorTest
{
	@Override
	protected JakartaInjectTestComponent newTestComponent(String id)
	{
		return new JakartaInjectTestComponent(id);
	}

	@Override
	protected TestNoComponentInterface newTestNoComponent()
	{
		return new JakartaInjectTestNoComponent();
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5686
	 *
	 * Wicket-Guice creates a lazy proxy that fails only when trying to use it
	 *
	 * @see org.apache.wicket.guice.GuiceFieldValueFactory#GuiceFieldValueFactory(boolean)
	 */
	@Test
	public void required()
	{
		try
		{
			JakartaInjectTestComponent component = new MyJakartaInjectWithNonExistingTestComponent();
			// Throws exception because component.getNonExisting() cannot be injected
			fail("Fields annotated with @jakarta.inject.Inject are required!");
		}
		catch (ConfigurationException cx)
		{
			Message message = cx.getErrorMessages().iterator().next();
			assertEquals(
				"No implementation for org.apache.wicket.ajax.attributes.IAjaxCallListener was bound.",
				message.getMessage());
		}
	}

	private static class MyJakartaInjectWithNonExistingTestComponent extends JakartaInjectTestComponent
	{
		@Inject
		private IAjaxCallListener nonExisting;

		public MyJakartaInjectWithNonExistingTestComponent()
		{
			super("id");
		}


		public IAjaxCallListener getNonExisting()
		{
			return nonExisting;
		}
	}
}
