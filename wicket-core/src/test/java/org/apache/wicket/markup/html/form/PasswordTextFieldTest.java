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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Test for {@link PasswordTextField}.
 *
 * @author svenmeier
 */
public class PasswordTextFieldTest extends WicketTestCase
{

	@Test
	public void nullifyPassword()
	{
		TestModel model = new TestModel();

		PasswordTextField field = new PasswordTextField("password", model);

		field.detach();

		assertNull(model.password);
		assertTrue(model.detached);
	}

	@Test
	public void nullifyPasswordOnNullModel()
	{
		PasswordTextField field = new PasswordTextField("password");
		field.setVisible(false);

		// does nothing on null model
		field.detach();
	}


	@Test
	public void nullifyNoReset()
	{
		TestModel model = new TestModel();

		PasswordTextField field = new PasswordTextField("password", model);
		field.setResetPassword(false);

		field.detach();

		assertEquals("test", model.password);
		assertTrue(model.detached);
	}

	private class TestModel implements IModel<String>
	{
		public boolean detached;

		public String password = "test";

		@Override
		public String getObject()
		{
			detached = false;

			return password;
		}

		@Override
		public void setObject(String password)
		{
			this.password = password;

			detached = false;
		}

		@Override
		public void detach()
		{
			detached = true;
		}
	}
}
