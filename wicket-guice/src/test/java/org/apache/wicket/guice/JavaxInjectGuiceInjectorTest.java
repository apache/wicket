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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.junit.Test;

import com.google.inject.ConfigurationException;
import com.google.inject.spi.Message;

import javax.inject.Inject;

/**
 */
public class JavaxInjectGuiceInjectorTest extends AbstractInjectorTest
{
	@Override
	protected JavaxInjectTestComponent newTestComponent(String id)
	{
		return new JavaxInjectTestComponent(id);
	}

	@Override
	protected TestNoComponentInterface newTestNoComponent()
	{
		return new JavaxInjectTestNoComponent();
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
			JavaxInjectTestComponent component = new MyJavaxInjectWithNonExistingTestComponent();
			// Throws exception because component.getNonExisting() cannot be injected
			fail("Fields annotated with @javax.inject.Inject are required!");
		}
		catch (ConfigurationException cx)
		{
			Message message = cx.getErrorMessages().iterator().next();
			assertThat(message.getMessage(), is(equalTo("No implementation for org.apache.wicket.ajax.attributes.IAjaxCallListener was bound.")));
		}
	}

	private static class MyJavaxInjectWithNonExistingTestComponent extends JavaxInjectTestComponent {
		@Inject
		private IAjaxCallListener nonExisting;

		public MyJavaxInjectWithNonExistingTestComponent() {
			super("id");
		}


		public IAjaxCallListener getNonExisting() {
			return nonExisting;
		}
	}
}
