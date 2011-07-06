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

import java.util.Iterator;

import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;


/**
 * 
 * @author Juergen Donnerstag
 */
public class MarkupContainerTest extends WicketTestCase
{
	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public MarkupContainerTest(String name)
	{
		super(name);
	}

	/**
	 * Make sure components are iterated in the order they were added. Required e.g. for Repeaters
	 */
	public void testIteratorOrder()
	{
		MarkupContainer container = new WebMarkupContainer("component");
		for (int i = 0; i < 10; i++)
		{
			container.add(new WebComponent(Integer.toString(i)));
		}
		int i = 0;
		Iterator iter = container.iterator();
		while (iter.hasNext())
		{
			Component component = (Component)iter.next();
			assertEquals(Integer.toString(i++), component.getId());
		}
	}

	public void testMarkupId() throws Exception
	{
		executeTest(MarkupIdTestPage.class, "MarkupIdTestPageExpectedResult.html");
	}

	public void testGet()
	{
		WebMarkupContainer a = new WebMarkupContainer("a");
		WebMarkupContainer b = new WebMarkupContainer("b");
		WebMarkupContainer c = new WebMarkupContainer("c");
		WebMarkupContainer d = new WebMarkupContainer("d");
		WebMarkupContainer e = new WebMarkupContainer("e");
		WebMarkupContainer f = new WebMarkupContainer("f");

		// ....A
		// ...B....C
		// .......D..E
		// ...........F

		a.add(b);
		a.add(c);
		c.add(d);
		c.add(e);
		e.add(f);

		// basic gets

		assertTrue(a.get(null) == a);
		assertTrue(a.get("") == a);
		assertTrue(a.get("b") == b);
		assertTrue(a.get("c") == c);
		assertTrue(a.get("c:d") == d);
		assertTrue(a.get("c:e:f") == f);

		assertTrue(b.get("..") == a);
		assertTrue(e.get("..:..") == a);
		assertTrue(d.get("..:..:c:e:f") == f);

		// invalid gets

		assertNull(a.get(".."));
		assertNull(a.get("..:a"));
		assertNull(b.get("..|.."));
		assertNull(a.get("q"));
	}
}
