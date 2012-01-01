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
package org.apache.wicket.hierarchy.complete;

import static org.apache.wicket.hierarchy.complete.WicketMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.internal.Enclosure;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class HierarchyCompletionTest
{
	@Rule
	public TesterRule tester = new TesterRule();


	/** {@code [a,b,c] -> [a[b[c]]] } */
	@Test
	public void dequeue1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(b, c, a);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b, c)));
	}

	/** {@code [a[b,c]] -> [a[b[c]]] } */
	@Test
	public void dequeue2()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(a);
		a.queue(b, c);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b, c)));
	}

	/** {@code [a[b[c]] -> [a[b[c]]] } */
	@Test
	public void dequeue3()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(a);
		a.queue(b);
		b.queue(c);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b, c)));
	}

	/** {@code [a[b],c] -> [a[b[c]]] } */
	@Test
	public void dequeue4()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(a, c);
		a.queue(b);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b, c)));
	}

	/** {@code [a(b)],c] -> [a[b[c]]] } */
	@Test
	public void dequeue5()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();
		p.queue(a, c);
		a.add(b);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b, c)));
	}

	/** {@code [a,b,c] -> [a[b,c]] } */
	@Test
	public void dequeue6()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'></p><p wicket:id='c'></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(a, b, c);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b)));
		assertThat(p, hasPath(new Path(a, c)));
	}

	/** {@code [a,c[b]] ->| [a[b[c]]] } */
	@Test
	public void dequeueError1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(b, c);
		c.queue(a);

		try
		{
			tester.startPage(p);
			Assert.fail();
		}
		catch (WicketRuntimeException e)
		{
			// expected
		}
	}

	/** {@code [a,q[r,s]] - > [a[q[r[s]]]] } */
	@Test
	public void dequeueWithPanel1()
	{
		MarkupContainer a = new A(), r = new R(), s = new S();

		TestPanel q = new TestPanel("q");
		q.setPanelMarkup("<wicket:panel><p wicket:id='r'><p wicket:id='s'></p></p></wicket:panel>");
		q.queue(r, s);

		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='q'></p></p>");

		p.queue(a, q);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, q, r, s)));
	}

	/** panel has leading markup */
	@Test
	public void dequeueWithPanel2()
	{
		MarkupContainer r = new R();

		TestPanel q = new TestPanel("q");
		q.setPanelMarkup("<html><body><wicket:panel><p wicket:id='r'></p></wicket:panel></body></html>");
		q.queue(r);

		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='q'></p>");
		p.queue(q);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(q, r)));
	}

	/** panel with a static header section */
	@Test
	public void dequeueWithPanel3()
	{
		MarkupContainer r = new R();

		TestPanel q = new TestPanel("q");
		q.setPanelMarkup("<html><head><wicket:head><meta/></wicket:head></head>"
			+ "<body><wicket:panel><p wicket:id='r'></p></wicket:panel></body></html>");
		q.queue(r);

		TestPage p = new TestPage();
		p.setPageMarkup("<html><head></head><body><p wicket:id='q'></p></body></html>");
		p.queue(q);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(q, r)));
	}

	/** repeater */
	@Test
	public void dequeueWithRepeater1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='lv'><p wicket:id='b'><p wicket:id='c'></p></p></p></p>");

		MarkupContainer a = new A();
		LV l = new LV(3)
		{
			@Override
			protected void populateItem(ListItem<Integer> item)
			{
				item.queue(new B(), new C());
			}
		};

		p.queue(a, l);

		tester.startPage(p);

		assertThat(l.size(), is(3));
		for (Component item : l)
		{
			assertThat(p, hasPath(new Path(a, l, item, new B(), new C())));
		}
	}

	/** repeater with a panel inside */
	@Test
	public void dequeueWithRepeater2()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='lv'><p wicket:id='b'><p wicket:id='q'></p></p></p></p>");

		MarkupContainer a = new A();
		LV l = new LV(3)
		{
			@Override
			protected void populateItem(ListItem<Integer> item)
			{
				TestPanel q = new TestPanel("q");
				q.setPanelMarkup("<wicket:panel><p wicket:id='r'><p wicket:id='s'></p></p></wicket:panel>");
				q.queue(new R(), new S());

				item.queue(q, new B());
			}
		};

		p.queue(a, l);

		tester.startPage(p);

		assertThat(l.size(), is(3));
		for (Component item : l)
		{
			assertThat(p, hasPath(new Path(a, l, item, new B()).add("q").add(new R(), new S())));
		}
	}

	/** dequeue, then rerender the page instance after a callback is executed */
	@Test
	public void dequeueWithCallback()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><a wicket:id='l'><p wicket:id='b'></p></a></p>");
		MarkupContainer a = new A(), b = new B();
		L l = new L();
		p.queue(a, b, l);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, l, b)));
		assertThat(l.isClicked(), is(false));

		tester.getTester().clickLink(l);

		assertThat(l.isClicked(), is(true));
	}


	/** queuing two components with the same id */
	@Test
	public void queueIdCollission()
	{
		try
		{
			new A().queue(new B(), new B());
			Assert.fail("Should not be able to queue two components with the same id under the same parent");
		}
		catch (WicketRuntimeException e)
		{
			// expected
		}
	}

	@Test
	public void resolveHeader1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<html><head><wicket:head></wicket:head></head><body></body></html>");

		tester.startPage(p);
	}

	/** resolve header, then rerender the page instance after a callback is executed */
	@Test
	public void resolveHeaderWithCallback()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<html><head><wicket:head><wicket:link><a href='Foo.html'>foo</a></wicket:link></wicket:head></head>"
			+ "<body><p wicket:id='a'><a wicket:id='l'><p wicket:id='b'></p></a></p></body></html>");
		MarkupContainer a = new A(), b = new B();
		L l = new L();
		p.queue(a, b, l);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, l, b)));
		assertThat(l.isClicked(), is(false));

		tester.getTester().clickLink(l);

		assertThat(l.isClicked(), is(true));
	}

	@Test
	public void resolveHeaderWithRepeatedRenderOfSameInstance()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<html><head><wicket:head><wicket:link><a href='Foo.html'>foo</a></wicket:link></wicket:head></head><body></body></html>");

		tester.startPage(p);
		tester.startPage(p);
	}

	@Test
	public void resolveHeaderWithRepeatedRenderOfNewInstances()
	{
		class MyPage extends TestPage
		{
			public MyPage()
			{
				setPageMarkup("<html><head><wicket:head><wicket:link><a href='Foo.html'>foo</a></wicket:link></wicket:head></head><body></body></html>");
			}
		}
		tester.startPage(new MyPage());
		tester.startPage(new MyPage());
	}

	@Test
	public void enclosure1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<form wicket:id='f'><wicket:enclosure child='t'><input wicket:id='t' type='text'/><span wicket:id='l'></span></wicket:enclosure></form>");
		Form<?> form = new Form<Void>("f");
		Component t = new TextField<String>("t", new Model<String>());
		Component l = new Label("l", "label")
		{
			@Override
			protected void onInitialize()
			{
				super.onInitialize();
				assertThat("in oninitialize the component should be in the correct hierarchy",//
					getParent(), is(instanceOf(Enclosure.class)));
			}
		};

		p.queue(form);
		form.queue(t, l);

		t.setVisible(false); // this should make 'l' invisible
		tester.startPage(p);

		assertThat("dequeued into a resolved enclosure", //
			t.getParent(), is(instanceOf(Enclosure.class)));

		assertThat("should be made invisible by enclosure", //
			l.isVisibleInHierarchy(), is(false));
	}


	private static class A extends WebMarkupContainer
	{
		public A()
		{
			super("a");
		}
	}

	private static class B extends WebMarkupContainer
	{
		public B()
		{
			super("b");
		}
	}

	private static class C extends WebMarkupContainer
	{
		public C()
		{
			super("c");
		}
	}

	private static class R extends WebMarkupContainer
	{
		public R()
		{
			super("r");
		}
	}

	private static class S extends WebMarkupContainer
	{
		public S()
		{
			super("s");
		}
	}

	private static abstract class LV extends ListView<Integer>
	{
		public LV(int size)
		{
			super("lv");
			ArrayList<Integer> values = new ArrayList<Integer>();
			for (int i = 0; i < size; i++)
				values.add(i);
			setModel(new Model<ArrayList<Integer>>(values));
		}
	}

	private static class L extends Link<Void>
	{
		private boolean clicked = false;

		public L()
		{
			super("l");
		}

		@Override
		public void onClick()
		{
			clicked = true;
		}

		public boolean isClicked()
		{
			return clicked;
		}
	}


	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private String markup;

		public TestPage()
		{
		}

		public TestPage(String markup)
		{
			this.markup = markup;
		}

		protected String getPageMarkup()
		{
			return markup;
		}

		public void setPageMarkup(String markup)
		{
			this.markup = markup;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(getPageMarkup());
		}

	}

	private static class TestPanel extends Panel implements IMarkupResourceStreamProvider
	{

		private String markup;

		public TestPanel(String id)
		{
			super(id);
		}

		public TestPanel(String id, String markup)
		{
			super(id);
			this.markup = markup;
		}

		protected void setPanelMarkup(String markup)
		{
			this.markup = markup;
		}

		protected String getPanelMarkup()
		{
			return markup;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(getPanelMarkup());
		}

	}
}
