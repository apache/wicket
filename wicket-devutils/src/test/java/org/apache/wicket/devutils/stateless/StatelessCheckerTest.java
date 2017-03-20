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
import org.apache.wicket.MarkupContainer;
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
	@StatelessComponent
	public static class StatelessPage extends DummyHomePage
	{
		private static final long serialVersionUID = 1L;
	}

	@StatelessComponent
	private static class StatelessLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		public StatelessLabel(final String id)
		{
			super(id);
		}
	}

	@StatelessComponent
	private static class StatefulMarkupContainer extends MarkupContainer
	{
		private static final long serialVersionUID = 1L;

		public StatefulMarkupContainer(String id) {
			super(id);
		}

		@Override
		public boolean getStatelessHint()
		{
			return false;
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

	private static class StatelessCheckerQuietly extends StatelessChecker
	{
		private StatelessCheckFailureException ex;

		protected void fail(StatelessCheckFailureException e)
		{
			this.ex = e;
		}

		public StatelessCheckFailureException getFailureException()
		{
			return ex;
		}
	}

	private StatelessChecker checker;

	private StatelessCheckerQuietly checkerQuietly;

	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester();
		checker = new StatelessChecker();
		checkerQuietly = new StatelessCheckerQuietly();
	}

	@After
	public void tearDown()
	{
		tester.destroy();
	}

	@Test
	public void testNonBookmarkablePage()
	{
		try
		{
			startNonBookmarkablePage(checker);
			fail("Expected tester.startPage() to fail with StatelessCheckFailureException");
		}
		catch (StatelessCheckFailureException ex)
		{
			assertNonBookmarkablePage(ex);
		}
	}

	@Test
	public void testNonBookmarkablePageQuietly()
	{
		startNonBookmarkablePage(checkerQuietly);
		StatelessCheckFailureException ex = checkerQuietly.getFailureException();
		assertNonBookmarkablePage(ex);
	}

	private void startNonBookmarkablePage(StatelessChecker checker)
	{
		tester.getApplication().getComponentPostOnBeforeRenderListeners().add(checker);
		tester.startPage(StatelessPage.class);
	}

	private void assertNonBookmarkablePage(StatelessCheckFailureException ex) {
		assertEquals("'[Page class = org.apache.wicket.devutils.stateless.StatelessCheckerTest$StatelessPage, id = 0, render count = 1]' claims to be stateless but isn't. Offending component: [TestLink [Component id = testPage]]", ex.getMessage());
		assertEquals(StatelessPage.class, ex.getComponent().getClass());
	}

	@Test
	public void testStatefulBehaviors()
	{
		try
		{
			startComponentInPage(checker, new StatelessLabel("foo").add(new StatefulBehavior()));
			fail("Expected tester.startComponentInPage() to fail with StatelessCheckFailureException");
		}
		catch (StatelessCheckFailureException ex)
		{
			assertStatefulBehaviors(ex);
		}
	}

	@Test
	public void testStatefulBehaviorsQuietly()
	{
		startComponentInPage(checkerQuietly, new StatelessLabel("foo").add(new StatefulBehavior()));
		StatelessCheckFailureException ex = checkerQuietly.getFailureException();
		assertStatefulBehaviors(ex);
	}

	private void startComponentInPage(StatelessChecker checker, Component foo) {
		tester.getApplication().getComponentPostOnBeforeRenderListeners().add(checker);
		tester.startComponentInPage(foo);
	}

	private void assertStatefulBehaviors(StatelessCheckFailureException ex) {
		assertEquals("'[Component id = foo]' claims to be stateless but isn't. Stateful behaviors: org.apache.wicket.devutils.stateless.StatelessCheckerTest$StatefulBehavior", ex.getMessage());
		assertEquals(StatelessLabel.class, ex.getComponent().getClass());
	}

	@Test
	public void testPositive()
	{
		startComponentInPage(checker, new StatelessLabel("foo"));
	}

	@Test
	public void testStatefulMarkupContainer()
	{
		try
		{
			startComponentInPage(checker, new StatefulMarkupContainer("foo"));
			fail("Expected tester.startComponentInPage() to fail with StatelessCheckFailureException");
		}
		catch (StatelessCheckFailureException ex)
		{
			assertStatefulMarkupContainer(ex);
		}
	}

	@Test
	public void testStatefulMarkupContainerQuietly()
	{
		startComponentInPage(checkerQuietly, new StatefulMarkupContainer("foo"));
		StatelessCheckFailureException ex = checkerQuietly.getFailureException();
		assertStatefulMarkupContainer(ex);

	}

	private void assertStatefulMarkupContainer(StatelessCheckFailureException ex)
	{
		assertEquals("'[StatefulMarkupContainer [Component id = foo]]' claims to be stateless but isn't. Possible reason: no stateless hint", ex.getMessage());
		assertEquals(StatefulMarkupContainer.class, ex.getComponent().getClass());
	}
}
