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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc", "serial" })
class MarkupContainerTest extends WicketTestCase
{
	private static final int NUMBER_OF_CHILDREN_FOR_A_MAP = MarkupContainer.MAPIFY_THRESHOLD + 1;

	/**
	 * Make sure components are iterated in the order they were added. Required e.g. for Repeaters
	 */
	@Test
	void iteratorOrder()
	{
		MarkupContainer container = new WebMarkupContainer("component");
		for (int i = 0; i < 10; i++)
		{
			container.add(new WebComponent(Integer.toString(i)));
		}
		int i = 0;
		for (Component component : container)
		{
			assertEquals(Integer.toString(i++), component.getId());
		}
	}

	@Test
	void markupId() throws Exception
	{
		executeTest(MarkupIdTestPage.class, "MarkupIdTestPageExpectedResult.html");
	}

	@Test
	void get()
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

		// parent path gets

		assertTrue(b.get("..") == a);
		assertTrue(e.get("..:..") == a);
		assertTrue(d.get("..:..:c:e:f") == f);
		assertTrue(e.get("..:d:..:e:f") == f);
		assertTrue(e.get("..:d:..:..") == a);

		// invalid gets

		assertNull(a.get(".."));
		assertNull(a.get("..:a"));
		assertNull(b.get("..|.."));
		assertNull(a.get("q"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4006
	 */
	@Test
	void addMyself()
	{
		WebMarkupContainer me = new WebMarkupContainer("a");
		assertThrows(IllegalArgumentException.class, () -> {
			me.add(me);
		});
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5911
	 */
	@Test
	void rerenderAfterRenderFailure()
	{
		FirstRenderFailsPage page = new FirstRenderFailsPage();
		try
		{
			tester.startPage(page);
		}
		catch (WicketRuntimeException expected)
		{
		}

		tester.startPage(page);

		// rendering flags where properly reset, so second rendering works properly
		assertEquals(2, page.beforeRenderCalls);
	}

	@Test
	void hierarchyChangeDuringRender()
	{
		HierarchyChangePage page = new HierarchyChangePage();
		try
		{
			tester.startPage(page);
			fail();
		}
		catch (WicketRuntimeException expected)
		{
			assertEquals(
				"Cannot modify component hierarchy after render phase has started (page version cant change then anymore)",
				expected.getMessage());
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4012
	 */
	@Test
	void afterRenderJustOnce()
	{
		AfterRenderJustOncePage page = new AfterRenderJustOncePage();
		tester.startPage(page);

		assertEquals(1, page.afterRenderCalls);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4016
	 */
	@Test
	void callToStringFromConstructor()
	{
		ToStringComponent page = new ToStringComponent();
	}

	@Test
	void noChildShouldNotIterate()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Iterator<Component> iterator = wmc.iterator();
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildAddingChildAfterIteratorAcquiredShouldIterateAndReturnNewChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Iterator<Component> iterator = wmc.iterator();

		Label label1 = new Label("label1", "Label1");
		wmc.add(label1);

		assertEquals(1, wmc.size());

		assertEquals(true, iterator.hasNext());
		assertEquals(label1, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildAddingNChildrenAfterIteratorAcquiredShouldIterateAndReturnNewChildren()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Iterator<Component> iterator = wmc.iterator();

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		assertEquals(NUMBER_OF_CHILDREN_FOR_A_MAP, wmc.size());

		Label label1 = new Label("label1", "Label1");
		wmc.add(label1);

		assertEquals(true, iterator.hasNext());

		takeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP);

		assertEquals(label1, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildAddingNChildrenAfterIteratorAcquiredShouldIterateAndReturnNewChildren2()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		assertEquals(NUMBER_OF_CHILDREN_FOR_A_MAP, wmc.size());

		Iterator<Component> iterator = wmc.iterator();

		takeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP);

		Label label1 = new Label("label1", "Label1");
		wmc.add(label1);

		assertEquals(true, iterator.hasNext());
		assertEquals(label1, iterator.next());
		assertEquals(false, iterator.hasNext());
	}


	/*
	 * Iterator tests
	 *
	 * The tests below are specific for testing addition and removal of children while maintaining
	 * the correct order of iterators without throwing ConcurrentModificationException.
	 */

	@Test
	void noChildAddingAndRemoveChildAfterIteratorAcquiredShouldNotIterate()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");

		Iterator<Component> iterator = wmc.iterator();

		wmc.add(label1);
		wmc.remove(label1);

		assertEquals(0, wmc.size());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void addingNewChildAfterIterationHasStartedShouldIterateNewChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		// add one child
		addNChildren(wmc, 1);

		Iterator<Component> iterator = wmc.iterator();

		// iterate
		takeNChildren(iterator, 1);

		// there are no more children to iterate
		assertEquals(false, iterator.hasNext());

		// add the new child
		Label newChild = new Label("label1", "Label1");
		wmc.add(newChild);

		assertEquals(2, wmc.size());

		// ensure that the newChild is up next (as it was added)
		assertEquals(newChild, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void replacingTheFirstChildAfterIteratingDoesntIterateTheNewChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");
		Component label2 = new Label("label2", "Label2");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);
		wmc.add(label1);
		wmc.add(label2);

		Iterator<Component> iterator = wmc.iterator();

		takeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP);

		iterator.next();

		// replace the first child **after** we already passed the child with the iterator
		Label newChild = new Label("label1", "newChild");
		wmc.replace(newChild);

		// the next child is still label 2
		assertSame(label2, iterator.next());

		// and the new child is not iterated (was replaced before the current position of the
		// iterator).
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void removingComponentsDuringIterationDoesntFail()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Component label1 = new Label("label1", "Label1");
		Component label2 = new Label("label2", "Label2");
		Component label3 = new Label("label3", "Label3");
		Component label4 = new Label("label4", "Label4");
		Component label5 = new Label("label5", "Label5");

		wmc.add(label1);
		wmc.add(label2);
		wmc.add(label3);
		wmc.add(label4);
		wmc.add(label5);

		// start iterating the 5 children
		Iterator<Component> iterator = wmc.iterator();

		assertSame(label1, iterator.next());
		assertSame(label2, iterator.next());
		assertSame(label3, iterator.next());

		// remove the current, previous and next children
		wmc.remove(label3);
		wmc.remove(label2);
		wmc.remove(label4);

		// ensure that the next iterated child is the 5th label
		assertSame(label5, iterator.next());

		// and that there are no more children to iterate
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void childrenBecomesListWhenMoreThanOneChild() throws Exception
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, 5);

		Field childrenField = MarkupContainer.class.getDeclaredField("children");
		childrenField.setAccessible(true);
		Object field = childrenField.get(wmc);
		assertThat(field).isInstanceOf(List.class);
	}

	@Test
	void childrenListBecomesMapWhenThresholdPassed() throws Exception
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP - 1);
		assertChildrenType(wmc, List.class);

		addNChildren(wmc, 1);
		assertChildrenType(wmc, LinkedMap.class);
	}

	@Test
	void childrenBecomesLinkedMapWhenThresholdPassed() throws Exception
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP + 1);

		assertChildrenType(wmc, LinkedMap.class);
	}

	@Test
	void linkedMapChildrenBecomesChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);
		wmc.add(new EmptyPanel("panel"));

		assertChildrenType(wmc, LinkedMap.class);

		Iterator<Component> iterator = wmc.iterator();
		removeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP);

		assertChildrenType(wmc, EmptyPanel.class);
	}

	@Test
	void listChildrenBecomesChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP - 2);
		wmc.add(new EmptyPanel("panel"));

		assertChildrenType(wmc, List.class);

		Iterator<Component> iterator = wmc.iterator();
		removeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP - 2);

		assertChildrenType(wmc, EmptyPanel.class);
	}

	@Test
	void geenIdee3() throws Exception
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP + 1);

		Iterator<Component> iterator = wmc.iterator();

		removeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP);

		assertEquals(true, iterator.hasNext());
		assertEquals(1, wmc.size());

		iterator.next();

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildAddIterateAndRemoveChildShouldIterateChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");

		Iterator<Component> iterator = wmc.iterator();

		wmc.add(label1);
		assertEquals(label1, iterator.next());

		wmc.remove(label1);
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildAddIterateAndRemoveAndAddSameChildShouldIterateChildTwice()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");

		Iterator<Component> iterator = wmc.iterator();

		wmc.add(label1);
		assertEquals(label1, iterator.next());

		assertEquals(false, iterator.hasNext());

		wmc.remove(label1);

		assertEquals(false, iterator.hasNext());

		wmc.add(label1);
		assertEquals(label1, iterator.next());
	}

	@Test
	void noChildAddIterateAndRemoveAndAddDifferentChildShouldIterateNewChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");
		Label label2 = new Label("label1", "Label2");

		Iterator<Component> iterator = wmc.iterator();

		wmc.add(label1);
		assertEquals(label1, iterator.next());

		assertEquals(false, iterator.hasNext());

		wmc.remove(label1);

		assertEquals(false, iterator.hasNext());

		wmc.add(label2);
		assertEquals(label2, iterator.next());
	}

	@Test
	void noChildAddingAndReplaceChildAfterIteratorAcquiredShouldIterateAndReturnNewReplacementChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");
		Label label2 = new Label("label1", "Label2");

		Iterator<Component> iterator = wmc.iterator();

		wmc.add(label1);
		wmc.replace(label2);

		assertEquals(true, iterator.hasNext());
		assertEquals(label2, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void singleChildIterateOneChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		wmc.add(label1 = new Label("label1", "Label1"));

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(true, iterator.hasNext());
		assertEquals(label1, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void singleChildShouldAllowReplacingChildAfterIterationHasStarted()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Component label1 = new Label("label1", "Label1");
		Component label2 = new Label("label1", "Label2");

		wmc.add(label1);

		Iterator<Component> iterator = wmc.iterator();

		wmc.replace(label2);

		assertEquals(true, iterator.hasNext());
		assertSame(label2, iterator.next());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void singleChildShouldAllowReplacingVisitedChildButNotRevisitReplacementChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");
		Label label2 = new Label("label1", "Label2");
		wmc.add(label1);

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(true, iterator.hasNext());
		assertEquals(label1, iterator.next());

		wmc.replace(label2);

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void multipleChildIteratorRetainsOrderOfAddition()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));
		wmc.add(label3 = new Label("label3", "Label3"));

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());
		assertEquals(label2, iterator.next());
		assertEquals(label3, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowAddingComponentAfterIterationStarted()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());
		assertEquals(label2, iterator.next());

		wmc.add(label3 = new Label("label3", "Label3"));
		assertEquals(label3, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowRemovingComponentAfterIterationStarted0()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));
		wmc.add(label3 = new Label("label3", "Label3"));

		Iterator<Component> iterator = wmc.iterator();

		wmc.remove(label1);

		assertEquals(label2, iterator.next());
		assertEquals(label3, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowRemovingComponentAfterIterationStarted1()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1 = new Label("label1", "Label1");
		Label label2 = new Label("label2", "Label2");
		Label label3 = new Label("label3", "Label3");
		wmc.add(label1);
		wmc.add(label2);
		wmc.add(label3);

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());
		wmc.remove(label1);
		assertEquals(label2, iterator.next());
		assertEquals(label3, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowRemovingComponentAfterIterationStarted2()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));
		wmc.add(label3 = new Label("label3", "Label3"));

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());
		assertEquals(label2, iterator.next());
		wmc.remove(label1);
		assertEquals(label3, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowRemovingComponentAfterIterationStarted3()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));
		wmc.add(label3 = new Label("label3", "Label3"));

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());
		assertEquals(label2, iterator.next());
		assertEquals(label3, iterator.next());
		wmc.remove(label1);

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowReplacingComponentAfterIterationStarted0()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));

		Iterator<Component> iterator = wmc.iterator();

		wmc.replace(label3 = new Label("label1", "Label3"));

		assertEquals(label3, iterator.next());
		assertEquals(label2, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowReplacingComponentAfterIterationStarted1()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));

		Iterator<Component> iterator = wmc.iterator();

		wmc.replace(label3 = new Label("label1", "Label3"));

		assertEquals(label3, iterator.next());
		assertEquals(label2, iterator.next());

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowReplacingComponentAfterIterationStarted()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());
		assertEquals(label2, iterator.next());

		wmc.replace(label3 = new Label("label1", "Label3"));

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void iteratorShouldAllowReplacingComponentAfterIterationStarted24()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		Label label1;
		Label label2;
		Label label3;
		wmc.add(label1 = new Label("label1", "Label1"));
		wmc.add(label2 = new Label("label2", "Label2"));

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		Iterator<Component> iterator = wmc.iterator();

		assertEquals(label1, iterator.next());

		wmc.replace(label3 = new Label("label2", "Label3"));

		assertEquals(label3, iterator.next());

		takeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP);

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildLeftBehindRemoveEach()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		Iterator<Component> iterator = wmc.iterator();
		while (iterator.hasNext())
		{
			iterator.next();
			iterator.remove();
		}
		assertEquals(0, wmc.size());
	}

	@Test
	void noChildLeftBehindRemoveAll()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		Iterator<Component> iterator = wmc.iterator();

		wmc.removeAll();

		assertEquals(0, wmc.size());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void noChildLeftBehindRemoveAll2()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		Iterator<Component> iterator = wmc.iterator();

		iterator.next();

		wmc.removeAll();

		assertEquals(0, wmc.size());
		assertEquals(false, iterator.hasNext());
	}

	@Test
	void ensureSerializationDeserializationWorks()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Iterator<Component> iterator = wmc.iterator();

		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);
		assertEquals(NUMBER_OF_CHILDREN_FOR_A_MAP, wmc.size());

		assertNotNull(WicketObjects.cloneObject(wmc));

		removeNChildren(iterator, 1);
		assertEquals(NUMBER_OF_CHILDREN_FOR_A_MAP - 1, wmc.size());
		assertNotNull(WicketObjects.cloneObject(wmc));

		removeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP - 2);
		assertNotNull(WicketObjects.cloneObject(wmc));

		assertEquals(1, wmc.size());
		removeNChildren(iterator, 1);
		assertEquals(0, wmc.size());
		assertNotNull(WicketObjects.cloneObject(wmc));
	}

	@Test
	void detachDuringIterationWorks()
	{
		int halfOfChildren = NUMBER_OF_CHILDREN_FOR_A_MAP / 2;
		int numberOfRemainingChildren = halfOfChildren + NUMBER_OF_CHILDREN_FOR_A_MAP % 2;

		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Iterator<Component> iterator = wmc.iterator();
		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP);

		takeNChildren(iterator, halfOfChildren);

		wmc.detach();

		takeNChildren(iterator, numberOfRemainingChildren);

		assertEquals(false, iterator.hasNext());
	}

	@Test
	void detachDuringIterationWithRemovalsSucceeds()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");

		Iterator<Component> iterator = wmc.iterator();

		addNChildren(wmc, 2);
		removeNChildren(iterator, 1);
		wmc.detach();
		takeNChildren(iterator, 1);

		assertEquals(false, iterator.hasNext());
		assertEquals(1, wmc.size());
	}

	/**
	 * Tests whether two iterators being used simultaneously keep correct score of where they are.
	 */
	@Test
	void twoIteratorsWorkInTandem()
	{
		int n = NUMBER_OF_CHILDREN_FOR_A_MAP * 2;

		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, n);

		Iterator<Component> iterator1 = wmc.iterator();
		Iterator<Component> iterator2 = wmc.iterator();

		Random r = new Random();

		for (int i = 0; i < n; i++)
		{
			if (r.nextBoolean())
			{
				iterator1.next();
				iterator1.remove();
			}
			else
			{
				iterator2.next();
				iterator2.remove();
			}
		}

		// after 2*N removals there should not be any child left
		assertEquals(false, iterator1.hasNext());
		assertEquals(false, iterator2.hasNext());
	}

	/**
	 * Tests removing a child when an iterator is active, followed by a detach still has the correct
	 * state for the iterator.
	 */
	@Test
	void detachWithOneIteratorOneChild()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, 1);

		Iterator<Component> iterator1 = wmc.iterator();

		iterator1.next();
		iterator1.remove();

		wmc.detach();

		assertEquals(false, iterator1.hasNext());
	}

	/**
	 * Tests removing and adding a component when an iterator is active, followed by a detach still
	 * has the correct state for the iterator.
	 */
	@Test
	void detachWithOneIteratorOneChildRemovedAndAdded()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, 1);

		Iterator<Component> iterator1 = wmc.iterator();

		iterator1.next();
		iterator1.remove();

		addNChildren(wmc, 1);

		assertEquals(true, iterator1.hasNext());

		wmc.detach();

		assertEquals(true, iterator1.hasNext());
		assertNotNull(iterator1.next());
	}

	/**
	 * Tests the case when one child is removed from a list the iterator still works after a detach.
	 */
	@Test
	void detachWithOneIteratorTwoChildren()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, 2);

		Iterator<Component> iterator1 = wmc.iterator();

		iterator1.next();
		iterator1.remove();

		assertEquals(true, iterator1.hasNext());

		wmc.detach();

		assertEquals(true, iterator1.hasNext());
		assertNotNull(iterator1.next());
	}

	/**
	 * Tests whether when the children is a list, removal and iteration still work after a detach.
	 */
	@Test
	void detachWithOneIteratorWithListForChildren()
	{
		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, NUMBER_OF_CHILDREN_FOR_A_MAP - 2);

		assertChildrenType(wmc, List.class);

		Iterator<Component> iterator = wmc.iterator();

		takeNChildren(iterator, 1);

		removeNChildren(iterator, 1);

		wmc.detach();

		takeNChildren(iterator, NUMBER_OF_CHILDREN_FOR_A_MAP - 4);
		assertEquals(false, iterator.hasNext());
	}

	/**
	 * Tests whether when the children is a map, removal and iteration still work after a detach.
	 */
	@Test
	void detachWithOneIteratorsWithMapForChildren()
	{
		int n = NUMBER_OF_CHILDREN_FOR_A_MAP * 2;

		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, n);

		Iterator<Component> iterator1 = wmc.iterator();

		Random r = new Random();

		for (int i = 0; i < NUMBER_OF_CHILDREN_FOR_A_MAP; i++)
		{
			iterator1.next();
			iterator1.remove();
		}
		wmc.detach();
		for (int i = 0; i < NUMBER_OF_CHILDREN_FOR_A_MAP; i++)
		{
			iterator1.next();
			iterator1.remove();
		}
		assertEquals(false, iterator1.hasNext());
	}

	@Test
	void detachWithTwoIteratorsAndRemovals()
	{
		int n = NUMBER_OF_CHILDREN_FOR_A_MAP * 2;

		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, n);

		Iterator<Component> iterator1 = wmc.iterator();
		Iterator<Component> iterator2 = wmc.iterator();

		Random r = new Random();

		for (int i = 0; i < NUMBER_OF_CHILDREN_FOR_A_MAP; i++)
		{
			if (r.nextBoolean())
			{
				iterator1.next();
				iterator1.remove();
			}
			else
			{
				iterator2.next();
				iterator2.remove();
			}
		}
		wmc.detach();
		iterator1.next();
		iterator1.remove();

		iterator2.next();
	}

	@Test
	void detachWithTwoIteratorsAndRemovals2()
	{
		int n = NUMBER_OF_CHILDREN_FOR_A_MAP * 2;

		WebMarkupContainer wmc = new WebMarkupContainer("id");
		addNChildren(wmc, n);

		Iterator<Component> iterator1 = wmc.iterator();
		Iterator<Component> iterator2 = wmc.iterator();

		Random r = new Random();

		for (int i = 0; i < NUMBER_OF_CHILDREN_FOR_A_MAP; i++)
		{
			Iterator<Component> iterator = r.nextBoolean() ? iterator1 : iterator2;
			if (iterator.hasNext())
			{
				iterator.next();
				iterator.remove();
			}
		}
		wmc.detach();
		iterator1.next();
		iterator2.next();
		iterator1.remove();
		while (iterator1.hasNext() || iterator2.hasNext())
		{
			Iterator<Component> iterator = r.nextBoolean() ? iterator1 : iterator2;
			if (iterator.hasNext())
			{
				iterator.next();
				iterator.remove();
			}
		}
		assertEquals(false, iterator1.hasNext());
		assertEquals(false, iterator2.hasNext());
	}

	/**
	 * Asserts that the children property of the {@code wmc} is of a particular {@code type}.
	 *
	 * @param wmc
	 *            the web markup container whose children property is to be checked
	 * @param type
	 *            the expected type
	 */
	private void assertChildrenType(WebMarkupContainer wmc, Class<?> type)
	{
		try
		{
			Field childrenField = MarkupContainer.class.getDeclaredField("children");
			childrenField.setAccessible(true);
			Object field = childrenField.get(wmc);
			assertThat(field).isInstanceOf(type);
		}
		catch (Exception e)
		{
			throw new AssertionError("Unable to read children", e);
		}
	}

	/**
	 * Adds {@code numberOfChildrenToAdd} anonymous children to the {@code parent}.
	 *
	 * @param parent
	 *            the parent to add the children to
	 * @param numberOfChildrenToAdd
	 *            the number of children
	 */
	private void addNChildren(WebMarkupContainer parent, int numberOfChildrenToAdd)
	{
		assertThat(numberOfChildrenToAdd).isGreaterThanOrEqualTo(0);
		int start = parent.size();
		for (int i = 0; i < numberOfChildrenToAdd; i++)
		{
			int index = start + i;
			parent.add(new Label("padding" + index, "padding" + index));
		}
	}

	/**
	 * Removes {@code numberOfChildrenToRemove} anonymous children from the parent using the
	 * {@code iterator}.
	 *
	 * @param iterator
	 *            the iterator to remove the children with
	 * @param numberOfChildrenToRemove
	 *            the number of children
	 */
	private void removeNChildren(Iterator<Component> iterator, int numberOfChildrenToRemove)
	{
		for (int i = 0; i < numberOfChildrenToRemove; i++)
		{
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * Progresses the {@code iterator} with {@code numberOfChildrenToTake} anonymous children.
	 *
	 * @param iterator
	 *            the iterator to progress
	 * @param numberOfChildrenToTake
	 *            the number of children
	 */
	private void takeNChildren(Iterator<Component> iterator, int numberOfChildrenToTake)
	{
		for (int i = 0; i < numberOfChildrenToTake; i++)
			iterator.next();
	}

	@Test
	void stream()
	{
		LoginPage loginPage = new LoginPage();
		Optional<Component> first = loginPage.stream()
			.filter(c -> c.getId().equals("form"))
			.findFirst();
		assertEquals(false, first.isPresent());

		loginPage.add(new Form<>("form"));
		Optional<Component> second = loginPage.stream()
			.filter(c -> c.getId().equals("form"))
			.findFirst();
		assertEquals(true, second.isPresent());

		loginPage.add(new WebMarkupContainer("wmc"));

		Optional<Form> form = loginPage.stream()
			.filter(Form.class::isInstance)
			.map(Form.class::cast)
			.findFirst();
		assertEquals(true, form.isPresent());

		Optional<WebMarkupContainer> wmc = loginPage.stream()
			.filter(WebMarkupContainer.class::isInstance)
			.map(WebMarkupContainer.class::cast)
			.findFirst();
		assertEquals(true, wmc.isPresent());
	}

	@Test
	void streamChildren()
	{
		LoginPage loginPage = new LoginPage();
		Optional<Component> first = loginPage.stream()
			.filter(c -> c.getId().equals("form"))
			.findFirst();
		assertEquals(false, first.isPresent());

		Form<Object> form = new Form<>("form");
		loginPage.add(form);

		form.add(new TextField<>("field"));

		assertTrue(loginPage.streamChildren()
			.filter(c -> c.getId().equals("form"))
			.findFirst()
			.isPresent());

		assertTrue(loginPage.streamChildren()
			.filter(c -> c.getId().equals("field"))
			.findFirst()
			.isPresent());

		assertTrue(loginPage.streamChildren()
			.filter(TextField.class::isInstance)
			.filter(c -> c.getId().equals("field"))
			.findFirst()
			.isPresent());
	}

	// https://issues.apache.org/jira/browse/WICKET-6754
	@Test
	void streamChildrenNestedContainer() {
		WebMarkupContainer wmc = new WebMarkupContainer("parent");
		WebMarkupContainer wmc1 = new WebMarkupContainer("wmc1");
		wmc.add(wmc1);
		WebMarkupContainer wmc1_2= new WebMarkupContainer("wmc1_2");
		wmc1.add(wmc1_2);
		Label lbl2 = new Label("lbl2");
		wmc.add(lbl2);
		List l = wmc.streamChildren().map(Component::getId).collect(Collectors.toList());
		assertEquals("[wmc1, wmc1_2, lbl2]", l.toString());
	}

	private static class HierarchyChangePage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{

		@Override
		protected void onRender()
		{
			// change hierarchy during render
			add(new Label("child"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}

	private static class ToStringComponent extends WebMarkupContainer
	{
		private ToStringComponent()
		{
			super("id");
			toString(true);
		}
	}

	private static class AfterRenderJustOncePage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private int afterRenderCalls = 0;

		private AfterRenderJustOncePage()
		{

			WebMarkupContainer a1 = new WebMarkupContainer("a1");
			add(a1);

			WebMarkupContainer a2 = new WebMarkupContainer("a2");
			a1.add(a2);

			WebMarkupContainer a3 = new WebMarkupContainer("a3")
			{

				@Override
				protected void onAfterRender()
				{
					super.onAfterRender();
					afterRenderCalls++;
				}

			};
			a2.add(a3);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id='a1'><div wicket:id='a2'><div wicket:id='a3'></div></div></div></body></html>");
		}
	}

	private static class FirstRenderFailsPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private boolean firstRender = true;

		private int beforeRenderCalls = 0;

		private FirstRenderFailsPage()
		{
			WebMarkupContainer a1 = new WebMarkupContainer("a1")
			{
				@Override
				protected void onBeforeRender()
				{
					super.onBeforeRender();

					beforeRenderCalls++;

					if (firstRender)
					{
						firstRender = false;
						throw new WicketRuntimeException();
					}
				}
			};
			add(a1);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><div wicket:id='a1'></div></body></html>");
		}
	}
}
