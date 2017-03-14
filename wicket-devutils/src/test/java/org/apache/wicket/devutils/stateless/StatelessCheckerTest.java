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

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marat Radchenko
 */
public class StatelessCheckerTest extends Assert
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

	private static class StatefulBehavior extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean getStatelessHint(Component component)
		{
			return false;
		}
	}

	private final StatelessChecker checker = new StatelessChecker();
	private final StatelessChecker checkerQuietly = new StatelessChecker() {
		protected void fail(Component component, String reason)
		{
			// Do Nothing...
		}
		protected void failBehaviors(Component component, String reason) {
			// Do Nothing...
		}

	};

	private WicketTester tester;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setUp()
	{
		tester = new WicketTester();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown()
	{
		tester.destroy();
	}

	@Test
	public void testNonBookmarkablePage()
	{
		boolean hit = isHitBookmarkablePage(checker);
		assertTrue("Expected exception", hit);
	}

	@Test
	public void testNonBookmarkablePageQuietly()
	{
		boolean hit = isHitBookmarkablePage(checkerQuietly);
		assertFalse("Expected exception", hit);
	}

	@Test
	public void testStatefulBehaviors()
	{
		boolean hit = isHitBehaviors(checker);
		assertTrue("Expected exception", hit);
	}
	@Test
	public void testStatefulBehaviorsQuietly()
	{
		boolean hit = isHitBehaviors(checkerQuietly);
		assertFalse("Expected exception", hit);
	}

	@Test
	public void testPositive()
	{
		tester.getApplication().getComponentPostOnBeforeRenderListeners().add(checker);
		tester.startComponentInPage(new StatelessLabel("foo"));
	}

	private boolean isHitBehaviors(StatelessChecker checker) {
		boolean hit = false;
		try
		{
			tester.getApplication().getComponentPostOnBeforeRenderListeners().add(checker);
			tester.startComponentInPage(new StatelessLabel("foo").add(new StatefulBehavior()));
		}
		catch (WicketRuntimeException ex)
		{
			if(ex.getCause() instanceof IllegalStateException) {
				hit = true;
			}
		}
		return hit;
	}

	private boolean isHitBookmarkablePage(StatelessChecker checker) {
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
		return hit;
	}

}
