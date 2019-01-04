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

import static org.junit.Assert.*;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;
import org.apache.wicket.application.IOnComponentTagListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests {@link IOnComponentTagListener}
 *
 * @author Igor Vaynberg (ivaynberg)
 */
public class OnComponentTagListenerTest extends WicketTestCase {


	@Test
	public void multipleListeners() {
		TestListener listener1 = new TestListener();
		TestListener listener2 = new TestListener();
		tester.getApplication().getOnComponentTagListeners().add(listener1);
		tester.getApplication().getOnComponentTagListeners().add(listener2);

		TestPage page = new TestPage();
		tester.startPage(page);

		assertEquals(1, listener1.components.getCount("c1"));
		assertEquals(1, listener1.components.getCount("c2"));
		assertEquals(1, listener1.components.getCount("c3"));

		assertEquals(1, listener2.components.getCount("c1"));
		assertEquals(1, listener2.components.getCount("c2"));
		assertEquals(1, listener2.components.getCount("c3"));
	}

	@Test
	public void visibility() {
		TestListener listener = new TestListener();
		tester.getApplication().getOnComponentTagListeners().add(listener);

		TestPage page = new TestPage();
		page.c1.setVisible(false);
		tester.startPage(page);

		assertEquals(0, listener.components.getCount("c1"));
		assertEquals(0, listener.components.getCount("c2"));
		assertEquals(1, listener.components.getCount("c3"));
	}

	@Test
	public void calledEvenIfNoSuper() {

		class DoesntCallSuper extends WebMarkupContainer {
			public DoesntCallSuper(String id) {
				super(id);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				// missing call to super on purpose
			}
		}

		TestListener listener = new TestListener();
		tester.getApplication().getOnComponentTagListeners().add(listener);

		TestPage page = new TestPage();
		page.c3 = new DoesntCallSuper(page.c3.getId());
		page.replace(page.c3);

		tester.startPage(page);

		assertEquals(1, listener.components.getCount("c1"));
		assertEquals(1, listener.components.getCount("c2"));
		assertEquals(1, listener.components.getCount("c3"));
	}

	static class TestPage extends WebPage implements IMarkupResourceStreamProvider {
		private static final long serialVersionUID = 1L;

		private Component c1, c2, c3;

		TestPage() {
			c1 = new WebMarkupContainer("c1");
			c2 = new WebMarkupContainer("c2");
			c3 = new WebMarkupContainer("c3");
			queue(c1, c2, c3);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass) {
			return new StringResourceStream("<html><body><div wicket:id='c1'><div wicket:id='c2'></div></div><div wicket:id='c3'></div></body></html>");
		}
	}

	private static class TestListener implements IOnComponentTagListener {

		private MultiSet<String> components = new HashMultiSet<>();

		@Override
		public void onComponentTag(Component component, ComponentTag tag) {
			components.add(component.getId());
		}
	}
}
