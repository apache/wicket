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
package org.apache.wicket.queueing;

import static org.apache.wicket.queueing.WicketMatchers.hasPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.internal.Enclosure;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.queueing.nestedborders.InnerBorder;
import org.apache.wicket.queueing.nestedborders.OuterBorder;
import org.apache.wicket.queueing.nestedpanels.InnerPanel;
import org.apache.wicket.queueing.nestedpanels.OuterPanel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

public class ComponentQueueingTest extends WicketTestCase
{
	/** {@code [a,b,c] -> [a[b[c]]] } */
	@Test
	public void dequeue1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='b'><p wicket:id='c'></p></p></p>");
		MarkupContainer a = new A(), b = new B(), c = new C();

		p.queue(b, c, a);
		assertThat(p, hasPath(a, b, c));
		tester.startPage(p);
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

		assertThat(p, hasPath(a, b, c));
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

		assertThat(p, hasPath(a, b, c));
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

		assertThat(p, hasPath(a, b, c));
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

		assertThat(p, hasPath(a, b, c));
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

	/**
	 * {a[b{e}[d,f{g}]],c} -> [a[b[c,d[e],f[g]]]]
	 */
	@Test
	public void dequeue7()
	{
		TestPage p = new TestPage();

		// @formatter:off
		p.setPageMarkup(
			"  <p wicket:id='a'>"
			+ "  <p wicket:id='b'>"
			+ "    <p wicket:id='c'></p>"
			+ "    <p wicket:id='d'>"
			+ "      <p wicket:id='e'></p>"
			+ "    </p>"
			+ "    <p wicket:id='f'>"
			+ "      <p wicket:id='g'></p>"
			+ "    </p>"
			+ "  </p>"
			+ "</p>");
		// @formatter:on

		MarkupContainer a = new A(), b = new B(), c = new C(), d = new D(), e = new E(), f = new F(), g = new G();

		a.add(b);
		b.queue(e);
		p.queue(a, c);
		b.add(d);
		f.queue(g);
		b.add(f);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, b, c)));
		assertThat(p, hasPath(new Path(a, b, d, e)));
		assertThat(p, hasPath(new Path(a, b, f, g)));
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
	public void panel1()
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
	public void panel2()
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
	public void panel3()
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
		tester.assertContains("<meta/>"); // contributed by <wicket:head>
	}

	/**
	 * test with inner panels
	 */
	@Test
	public void nestedPanels()
	{
		MarkupContainer r = new R(), s = new S();

		Panel innerPanel = new InnerPanel("inner");
		innerPanel.queue(s);

		Panel outerPanel = new OuterPanel("outer");

		outerPanel.queue(r, innerPanel);

		TestPage p = new TestPage();
		p.setPageMarkup("<html><head></head><body><p wicket:id='outer'></p></body></html>");
		p.queue(outerPanel);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(outerPanel, r)));
		assertThat(p, hasPath(new Path(outerPanel, innerPanel, s)));
		tester.assertContains("<meta/>"); // contributed by <wicket:head> in outer
		tester.assertContains("<meta2/>"); // contributed by <wicket:head> in inner
	}

	@Test
	public void repeater1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='lv'><p wicket:id='b'><p wicket:id='c'></p></p></p>");

		LV l = new LV(3)
		{
			@Override
			protected void populateItem(ListItem<Integer> item)
			{
				item.queue(new B(), new C());
			}
		};

		p.queue(l);

		tester.startPage(p);

		assertThat(l.size(), is(3));
		for (Component item : l)
		{
			assertThat(p, hasPath(new Path(l, item, new B(), new C())));
		}
	}

	/** repeater */
	@Test
	public void repeater2()
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
	public void repeater3()
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
	public void callback()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><a wicket:id='l'><p wicket:id='b'></p></a></p>");
		MarkupContainer a = new A(), b = new B();
		L l = new L();
		p.queue(a, b, l);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, l, b)));
		assertThat(l.isClicked(), is(false));

		tester.clickLink(l);

		assertThat(l.isClicked(), is(true));
	}


	/** queuing two components with the same id */
	@Test
	public void queueIdCollission()
	{
		try
		{
			new A().queue(new B(), new B());
			Assert
				.fail("Should not be able to queue two components with the same id under the same parent");
		}
		catch (WicketRuntimeException e)
		{
			// expected
		}
	}


	@Test
	public void autos1()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<wicket:enclosure child='a'><div wicket:id='a'></div><div wicket:id='b'></div></wicket:enclosure>");
		A a = new A();
		B b = new B();
		p.queue(a, b);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof Enclosure);
		assertTrue(b.getParent() instanceof Enclosure);

		// A is visible, enclosure renders

		assertEquals(
			"<wicket:enclosure child=\"a\"><div wicket:id=\"a\"></div><div wicket:id=\"b\"></div></wicket:enclosure>",
			tester.getLastResponseAsString());

		// A is not visible, enclosure does not render

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("", tester.getLastResponseAsString());
	}

	@Test
	public void autos2()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<wicket:enclosure child='a'><div wicket:id='a'></div><div wicket:id='b'></div></wicket:enclosure>");
		A a = new A();
		B b = new B();
		p.add(a, b);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof TestPage);
		assertTrue(b.getParent() instanceof TestPage);

		// A is visible, enclosure renders

		assertEquals(
			"<wicket:enclosure child=\"a\"><div wicket:id=\"a\"></div><div wicket:id=\"b\"></div></wicket:enclosure>",
			tester.getLastResponseAsString());

		// A is not visible, enclosure does not render

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("", tester.getLastResponseAsString());
	}

	@Test
	public void autos3()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<wicket:enclosure child='a'><div wicket:id='a'></div><div wicket:id='b'></div></wicket:enclosure>");
		A a = new A();
		B b = new B();
		p.queue(b);
		p.add(a);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof TestPage);
		assertTrue(b.getParent() instanceof Enclosure);

		// A is visible, enclosure renders

		assertEquals(
			"<wicket:enclosure child=\"a\"><div wicket:id=\"a\"></div><div wicket:id=\"b\"></div></wicket:enclosure>",
			tester.getLastResponseAsString());

		// A is not visible, enclosure does not render

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("", tester.getLastResponseAsString());
	}

	@Test
	public void autos4()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<wicket:enclosure child='a'><div wicket:id='a'></div><div wicket:id='b'></div></wicket:enclosure>");
		A a = new A();
		B b = new B();
		p.add(b);
		p.queue(a);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof Enclosure);
		assertTrue(b.getParent() instanceof TestPage);

		// A is visible, enclosure renders

		assertEquals(
			"<wicket:enclosure child=\"a\"><div wicket:id=\"a\"></div><div wicket:id=\"b\"></div></wicket:enclosure>",
			tester.getLastResponseAsString());

		// A is not visible, enclosure does not render

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("", tester.getLastResponseAsString());
	}

	@Test
	public void autos5()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<wicket:enclosure child='a'><div wicket:id='a'></div><div wicket:id='b'></div></wicket:enclosure>");
		A a = new A();
		B b = new B();
		p.queue(a);
		p.add(b);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof Enclosure);
		assertTrue(b.getParent() instanceof TestPage);


		// A is visible, enclosure renders

		assertEquals(
			"<wicket:enclosure child=\"a\"><div wicket:id=\"a\"></div><div wicket:id=\"b\"></div></wicket:enclosure>",
			tester.getLastResponseAsString());

		// A is not visible, enclosure does not render

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("", tester.getLastResponseAsString());
	}

	/**
	 * Test InlineEnclosure
	 */
	@Test
	public void autos6()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<div wicket:enclosure='a'><div wicket:id='a'></div><div wicket:id='b'></div></div>");
		A a = new A();
		B b = new B();
		p.queue(a, b);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof Enclosure);
		assertTrue(b.getParent() instanceof Enclosure);

		// A is visible, enclosure renders

		assertEquals(
				"<div wicket:enclosure=\"a\" id=\"wicket__InlineEnclosure_01\"><div wicket:id=\"a\"></div><div wicket:id=\"b\"></div></div>",
				tester.getLastResponseAsString());

		// A is not visible, inline enclosure render only itself (the placeholder tag)

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("<div id=\"wicket__InlineEnclosure_01\" style=\"display:none\"></div>", tester.getLastResponseAsString());
	}
	
	/**
	 * Test empty child attribute
	 */
	@Test
	public void autos7()
	{
		TestPage p = new TestPage();
		p.setPageMarkup("<wicket:enclosure child=''><div wicket:id='a'></div></wicket:enclosure>");
		A a = new A();
		
		p.queue(a);
		tester.startPage(p);

		assertTrue(a.getParent() instanceof Enclosure);
		

		// A is visible, enclosure renders

		assertEquals(
			"<wicket:enclosure child=\"a\"><div wicket:id=\"a\"></div></wicket:enclosure>",
			tester.getLastResponseAsString());

		// A is not visible, enclosure does not render

		a.setVisible(false);
		tester.startPage(p);
		assertEquals("", tester.getLastResponseAsString());
	}

	@Test
	public void border1()
	{
		MarkupContainer a = new A(), b = new B(), r = new R(), s = new S();

		TestBorder border = new TestBorder("border");
		border.setBorderMarkup("<wicket:border><b1 wicket:id='r'><b2 wicket:id='s'>" +
				"<wicket:body/></b2></b1></wicket:border>");
		border.queueToBorder(r, s);

		TestPage p = new TestPage();
		p.setPageMarkup("<out1 wicket:id='a'><p wicket:id='border'><in1 wicket:id='b'></in1></p></out1>");

		p.queue(a, border, b);

		tester.startPage(p);

		assertThat(p, hasPath(new Path(a, border, r, s, border.getBodyContainer(), b)));
	}


	@Test
	public void border_nested()
	{
		MarkupContainer a = new A(), b = new B(), c= new C(), d = new D(), r = new R(), s = new S();

		Border outerBorder = new OuterBorder("outerBorder");

		Border innerBorder = new InnerBorder("innerBorder");

		outerBorder.queueToBorder(r, innerBorder);

		innerBorder.queueToBorder(c, d);

		outerBorder.queueToBorder(s);


		TestPage p = new TestPage();
		p.setPageMarkup("<p wicket:id='a'><p wicket:id='outerBorder'><p wicket:id='b'></p></p></p>");
		
		p.queue(b, outerBorder, a);

		tester.startPage(p);
		
		assertThat(p, hasPath(new Path(a, outerBorder,  r, innerBorder, c, d, innerBorder.getBodyContainer(), s)));
		assertThat(p, hasPath(new Path(a, outerBorder, r, outerBorder.getBodyContainer(), b)));
	}

	@Test
	public void fragment1() {
		MarkupContainer a = new A(), b = new B(), r = new R(), s = new S();
		
		TestPage page = new TestPage();
		page.setPageMarkup("<a wicket:id='a'></a><f wicket:id='fragment'></f><b wicket:id='b'></b>"
			+ "<wicket:fragment wicket:id='f'><r wicket:id='r'></r><s wicket:id='s'></s></wicket:fragment>");
		
		Fragment fragment = new Fragment("fragment", "f", page);

		fragment.queue(r, s);
		page.queue(a, b, fragment);
		
		assertThat(page, hasPath(new Path(a)));
		assertThat(page, hasPath(new Path(b)));
		assertThat(page, hasPath(new Path(fragment, r)));
		assertThat(page, hasPath(new Path(fragment, s)));
	}

	@Test
	public void fragment_doesNotDequeueAcrossRegion()
	{
		MarkupContainer a = new A();

		TestPage page = new TestPage();
		page.setPageMarkup("<f wicket:id='fragment'></f><wicket:fragment wicket:id='f'><a wicket:id='a'></a></wicket:fragment>");

		Fragment fragment = new Fragment("fragment", "f", page);

		page.queue(a, fragment);

		assertThat(page, hasPath(new Path(fragment)));
		assertThat(a.getParent(), is(nullValue()));
	}

	
	@Test
	public void containerTag1()
	{
		MarkupContainer a = new A(), b = new B();

		TestPage page = new TestPage();
		page.setPageMarkup("<wicket:container wicket:id='a'><b wicket:id='b'></b></wicket:container>");

		page.queue(a, b);

		assertThat(page, hasPath(new Path(a, b)));
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

	private static class D extends WebMarkupContainer
	{
		public D()
		{
			super("d");
		}
	}

	private static class E extends WebMarkupContainer
	{
		public E()
		{
			super("e");
		}
	}

	private static class F extends WebMarkupContainer
	{
		public F()
		{
			super("f");
		}
	}
	private static class G extends WebMarkupContainer
	{
		public G()
		{
			super("g");
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
			ArrayList<Integer> values = new ArrayList<>();
			for (int i = 0; i < size; i++)
				values.add(i);
			setModel(new Model<>(values));
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

	private static class TestBorder extends Border implements IMarkupResourceStreamProvider
	{
		private String markup;

		public TestBorder(String id)
		{
			super(id);
		}

		protected void setBorderMarkup(String markup)
		{
			this.markup = markup;
		}

		protected String getBorderMarkup()
		{
			return markup;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
		                                               Class<?> containerClass)
		{
			return new StringResourceStream(getBorderMarkup());
		}
	}
}
