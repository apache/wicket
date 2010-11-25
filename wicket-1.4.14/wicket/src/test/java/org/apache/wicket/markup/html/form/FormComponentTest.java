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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

/**
 * 
 */
public class FormComponentTest extends TestCase
{
	private WicketTester wicketTester;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		wicketTester = new WicketTester();
	}

	public void testArrayType()
	{
		final FormComponent<?> fc = new TextField<String>("foo");
		fc.setType(String[].class);
		Assert.assertSame(String[].class, fc.getType());
	}

	public void testMultiDimentionalArrayType()
	{
		final FormComponent<?> fc = new TextField<String>("foo");
		fc.setType(String[][][].class);
		Assert.assertSame(String[][][].class, fc.getType());
	}

	public void testPrimitiveArrayType()
	{
		final FormComponent<?> fc = new TextField<String>("foo");
		fc.setType(boolean[].class);
		Assert.assertSame(boolean[].class, fc.getType());
	}

	@Override
	protected void tearDown() throws Exception
	{
		wicketTester.destroy();
		wicketTester = null;
		super.tearDown();
	}
}
