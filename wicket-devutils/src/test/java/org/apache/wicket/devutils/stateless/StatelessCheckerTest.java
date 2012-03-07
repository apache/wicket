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
package org.apache.wicket.devutils.stateless;

import junit.framework.TestCase;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;

/**
 * @author Marat Radchenko
 */
public class StatelessCheckerTest extends TestCase
{
	/**
	 * 
	 */
	@StatelessComponent
	public static class StatelessPage extends DummyHomePage
	{
		private static final long serialVersionUID = 1L;
	}

	/**
	 * 
	 */
	@StatelessComponent
	private static class StatelessLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		public StatelessLabel(final String id)
		{
			super(id);
		}
	}

	private final StatelessChecker checker = new StatelessChecker();
	private WicketTester tester;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	public void setUp()
	{
		tester = new WicketTester();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	public void tearDown()
	{
		tester.destroy();
	}

	/**
	 * 
	 */
	public void testNonBookmarkablePage()
	{
		boolean hit = false;
		try
		{
			tester.getApplication().getComponentPostOnBeforeRenderListeners().add(checker);
			tester.startPage(StatelessPage.class);
		}
		catch (IllegalArgumentException ex)
		{
			hit = true;
		}
		assertTrue("Expected exception", hit);
	}

	/**
	 * 
	 */
	public void testPositive()
	{
		tester.getApplication().getComponentPostOnBeforeRenderListeners().add(checker);
		tester.startComponent(new StatelessLabel("foo"));
	}
}
