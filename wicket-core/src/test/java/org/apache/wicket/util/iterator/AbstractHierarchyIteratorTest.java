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
package org.apache.wicket.util.iterator;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.junit.Test;

/**
 * 
 */
public class AbstractHierarchyIteratorTest extends WicketTestCase
{
	/** */
	@Test(expected = IllegalArgumentException.class)
	public void nullParent()
	{
		new ComponentHierarchyIterator(null);
	}

	/** */
	@Test
	public void emptyParent()
	{
		Page page = new MyPage();
		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void withComponent()
	{
		WebComponent comp = new WebComponent("id");
		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(comp);
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void multipleRemoves()
	{
	}

	/** */
	@Test
	public void multipleHasNext()
	{
	}

	/** */
	@Test
	public void multipleNext()
	{
	}

	/** */
	@Test
	public void parentWithSomeClients()
	{
		Page page = new MyPage();
		page.add(new WebComponent("1"));
		page.add(new WebMarkupContainer("2"));

		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);
		assertTrue(iter.hasNext());
		assertEquals("1", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("2", iter.next().getId());
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void simpleHierachy()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b21;
		b2.add(b21 = new WebMarkupContainer("b21"));

		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);
		assertTrue(iter.hasNext());
		assertEquals("a", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b1", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b2", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b21", iter.next().getId());
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void simpleHierachyDifferentOrder()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b12;
		b1.add(b12 = new WebMarkupContainer("b12"));
		WebMarkupContainer b121;
		b12.add(b121 = new WebMarkupContainer("b121"));

		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);
		assertTrue(iter.hasNext());
		assertEquals("a", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b1", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b12", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b121", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b2", iter.next().getId());
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void skip()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b12;
		b1.add(b12 = new WebMarkupContainer("b12"));
		WebMarkupContainer b121;
		b12.add(b121 = new WebMarkupContainer("b121"));

		// Filter leaf components only
		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);

		assertTrue(iter.hasNext());
		assertEquals("a", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b1", iter.next().getId());
		iter.skipRemainingSiblings();
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void skip2()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b12;
		b1.add(b12 = new WebMarkupContainer("b12"));
		WebMarkupContainer b121;
		b12.add(b121 = new WebMarkupContainer("b121"));
		WebComponent c;
		page.add(c = new WebComponent("c"));

		// Filter leaf components only
		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);

		assertTrue(iter.hasNext());
		assertEquals("a", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b", iter.next().getId());
		assertTrue(iter.hasNext());
		assertEquals("b1", iter.next().getId());
		iter.skipRemainingSiblings();
		assertTrue(iter.hasNext());
		assertEquals("c", iter.next().getId());
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}

	/** */
	@Test
	public void foreach()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b12;
		b1.add(b12 = new WebMarkupContainer("b12"));
		WebMarkupContainer b121;
		b12.add(b121 = new WebMarkupContainer("b121"));

		// Filter leaf components only
		int count = 0;
		String buf = "";
		for (Component component : new ComponentHierarchyIterator(page))
		{
			count += 1;
			buf += component.getId();
		}

		assertEquals(6, count);
		assertEquals("abb1b12b121b2", buf);
	}

	/** */
	@Test
	public void foreachDontGoDeeper()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b12;
		b1.add(b12 = new WebMarkupContainer("b12"));
		WebMarkupContainer b121;
		b12.add(b121 = new WebMarkupContainer("b121"));

		// Filter leaf components only
		int count = 0;
		String buf = "";
		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);
		while (iter.hasNext())
		{
			Component component = iter.next();
			count += 1;
			buf += component.getId();
			if ("b1".equals(component.getId()))
			{
				iter.dontGoDeeper();
			}
		}

		assertEquals(4, count);
		assertEquals("abb1b2", buf);
	}

	/** */
	@Test
	public void childFirst()
	{
		Page page = new MyPage();
		WebComponent a;
		page.add(a = new WebComponent("a"));
		WebMarkupContainer b;
		page.add(b = new WebMarkupContainer("b"));
		WebMarkupContainer b1;
		b.add(b1 = new WebMarkupContainer("b1"));
		WebMarkupContainer b2;
		b.add(b2 = new WebMarkupContainer("b2"));
		WebMarkupContainer b12;
		b1.add(b12 = new WebMarkupContainer("b12"));
		WebMarkupContainer b121;
		b12.add(b121 = new WebMarkupContainer("b121"));

		// Filter leaf components only
		int count = 0;
		StringBuilder buf = new StringBuilder();
		ComponentHierarchyIterator iter = new ComponentHierarchyIterator(page);
		iter.setChildFirst(true);
		while (iter.hasNext())
		{
			Component component = iter.next();
			count += 1;
			if (buf.length() > 0)
			{
				buf.append(Component.PATH_SEPARATOR);
			}
			buf.append(component.getId());
		}

		assertEquals(6, count);
		assertEquals("a:b121:b12:b1:b2:b", buf.toString());
	}

	/** */
	public static class MyPage extends WebPage
	{
		private static final long serialVersionUID = 1L;
	}
}
