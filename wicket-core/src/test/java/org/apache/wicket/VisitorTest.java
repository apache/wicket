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

import org.junit.Assert;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.visit.ClassVisitFilter;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.util.visit.Visits;
import org.junit.Test;

/**
 * <code>
 * A
 * +-B
 * +-C
 * | +-D
 * | +-E
 * |   +-F
 * +-G
 *   +-H
 * </code>
 * 
 * @author igor.vaynberg
 */
public class VisitorTest extends WicketTestCase
{
	/**
	 * testVisit()
	 */
	@Test
	public void visit()
	{
		final StringBuilder path = new StringBuilder();

		TestContainer container = new TestContainer();

		Visits.visit(container, new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				path.append(component.getId());
			}
		});

		Assert.assertEquals("ABCDEFGH", path.toString());
	}


	/**
	 * testContinueTraversal()
	 */
	@Test
	public void continueTraversal()
	{
		final StringBuilder path = new StringBuilder();

		TestContainer container = new TestContainer();

		container.visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				path.append(component.getId());
			}
		});

		Assert.assertEquals("BCDEFGH", path.toString());
	}

	/**
	 * testContinuePostOrder()
	 */
	@Test
	public void continuePostOrder()
	{
		final StringBuilder path = new StringBuilder();

		TestContainer container = new TestContainer();
		FormComponent.visitComponentsPostOrder(container, new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				path.append(component.getId());
			}
		});

		Assert.assertEquals("BDFECHGA", path.toString());
	}

	/**
	 * testStop()
	 */
	@Test
	public void stop()
	{
		final StringBuilder path = new StringBuilder();

		TestContainer container = new TestContainer();
		Object result = container.visitChildren(new IVisitor<Component, String>()
		{
			@Override
			public void component(Component component, IVisit<String> visit)
			{
				path.append(component.getId());
				if ("D".equals(component.getId()))
				{
					visit.stop("RESULT");
				}
			}
		});
		Assert.assertEquals("BCD", path.toString());
		Assert.assertEquals("RESULT", result);
	}

	/**
	 * testDoNotGoDeeper1()
	 */
	@Test
	public void doNotGoDeeper1()
	{
		final StringBuilder path = new StringBuilder();

		TestContainer container = new TestContainer();
		container.visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				path.append(component.getId());
				if ("C".equals(component.getId()))
				{
					visit.dontGoDeeper();
				}
			}
		});
		Assert.assertEquals("BCGH", path.toString());
	}

	/**
	 * testDoNotGoDeeper2()
	 */
	@Test
	public void doNotGoDeeper2()
	{
		final StringBuilder path = new StringBuilder();

		TestContainer container = new TestContainer();
		container.visitChildren(new IVisitor<Component, Void>()
		{
			@Override
			public void component(Component component, IVisit<Void> visit)
			{
				path.append(component.getId());
				if ("E".equals(component.getId()))
				{
					visit.dontGoDeeper();
				}
			}
		});
		Assert.assertEquals("BCDEGH", path.toString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3805
	 * 
	 * Visit parents with arbitrary type
	 */
	@Test
	public void testVisitParents()
	{
		TestContainer testContainer = new TestContainer();
		IVisitor<MarkupContainer, MarkerInterface> visitor = new IVisitor<MarkupContainer, MarkerInterface>()
		{
			@Override
			public void component(MarkupContainer object, IVisit<MarkerInterface> visit)
			{
				visit.stop((MarkerInterface)object);
			}
		};
		MarkerInterface markedParent = testContainer.get("G:H").visitParents(MarkupContainer.class,
			visitor, new ClassVisitFilter(MarkerInterface.class));
		assertEquals("G", markedParent.getId());
	}

	private static interface MarkerInterface
	{
		public String getId();
	}

	private static class MarkedWebMarkupContainer extends WebMarkupContainer
		implements
			MarkerInterface
	{
		private static final long serialVersionUID = 1L;

		public MarkedWebMarkupContainer(String id)
		{
			super(id);
		}
	}

	private static class TestContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		public TestContainer()
		{
			super("A");
			WebMarkupContainer b = new WebMarkupContainer("B");
			WebMarkupContainer c = new WebMarkupContainer("C");
			WebMarkupContainer d = new WebMarkupContainer("D");
			WebMarkupContainer e = new WebMarkupContainer("E");
			WebMarkupContainer f = new WebMarkupContainer("F");
			WebMarkupContainer g = new MarkedWebMarkupContainer("G");
			WebMarkupContainer h = new WebMarkupContainer("H");
			add(b);
			add(c);
			c.add(d);
			c.add(e);
			e.add(f);
			add(g);
			g.add(h);
		}

	}
}
