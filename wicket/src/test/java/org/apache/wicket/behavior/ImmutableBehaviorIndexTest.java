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
package org.apache.wicket.behavior;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/** IBehavior array management tests */
public class ImmutableBehaviorIndexTest extends WicketTestCase
{
	/** Tests simple behavior */
	public void testSimple()
	{
		MyPage page = new MyPage();
		page.getContainer().add(new SimpleAttributeModifier("class", "border"));
		tester.startPage(page);

		assertTrue(tester.getLastResponseAsString().contains("class=\"border\""));
	}

	/**
	 * Tests the fact that url-behavior indexes do not change even if behaviors are removed/added
	 */
	public void testUrlIndexRendering()
	{
		IBehavior border = new SimpleAttributeModifier("class", "border");
		IBehavior border2 = new SimpleAttributeModifier("class2", "border");
		IBehavior auto = new SimpleAttributeModifier("autocomplete", "off");
		IBehavior auto2 = new SimpleAttributeModifier("autocomplete2", "off");
		IBehavior link = new LinkBehavior("href");
		IBehavior link2 = new LinkBehavior("onclick");

		MyPage page = new MyPage();

		page.getContainer().add(border, auto, link, border2, link2, auto2);
		tester.startPage(page);

		String output = tester.getLastResponseAsString();
		assertTrue(output.contains("class=\"border\""));
		assertTrue(output.contains("autocomplete=\"off\""));
		assertTrue(output.contains("class2=\"border\""));
		assertTrue(output.contains("autocomplete2=\"off\""));
		assertTrue(output.contains("IBehaviorListener.0"));
		assertTrue(output.contains("IBehaviorListener.1"));

		// if we remove a behavior that is before the ibehaviorlistener its url index should not
		// change

		page.getContainer().remove(border);
		page.getContainer().remove(border2);
		page.getContainer().remove(auto);
		page.getContainer().remove(auto2);
		tester.startPage(page);
		output = tester.getLastResponseAsString();
		assertTrue(output.contains("IBehaviorListener.0"));
		assertTrue(output.contains("IBehaviorListener.1"));
	}

	/**
	 * Tests that removal of behaviors properly cleans up the data array
	 */
	public void testBehaviorDataArrayCleanup()
	{
		IBehavior border = new SimpleAttributeModifier("class", "border");
		IBehavior border2 = new SimpleAttributeModifier("class2", "border");
		IBehavior auto = new SimpleAttributeModifier("autocomplete", "off");
		IBehavior auto2 = new SimpleAttributeModifier("autocomplete2", "off");
		IBehavior link = new LinkBehavior("href");
		IBehavior link2 = new LinkBehavior("onclick");

		MyPage page = new MyPage();
		page.getContainer().add(border, auto, link, border2, link2, auto2);

		int borderId = page.container.getBehaviorId(border);
		int border2Id = page.container.getBehaviorId(border2);
		int autoId = page.container.getBehaviorId(auto);
		int auto2Id = page.container.getBehaviorId(auto2);
		int linkId = page.container.getBehaviorId(link);
		int link2Id = page.container.getBehaviorId(link2);

		List<? extends IBehavior> behaviors = page.getContainer().getBehaviors();
		assertEquals(6, behaviors.size());

		// test removal of various behaviors and make sure they preserve indexes as long as there is
		// a IBehaviorListener in the list

		// border,auto,link,border2,link2,auto2
		page.getContainer().remove(border);
		behaviors = page.getContainer().getBehaviors();
		assertEquals(5, behaviors.size());
		assertEquals(autoId, page.container.getBehaviorId(auto));
		assertEquals(link2Id, page.container.getBehaviorId(link2));

		// auto,link,border2,link2,auto2
		page.getContainer().remove(link);
		behaviors = page.getContainer().getBehaviors();
		assertEquals(4, behaviors.size());
		assertEquals(autoId, page.container.getBehaviorId(auto));
		assertEquals(link2Id, page.container.getBehaviorId(link2));

		// auto,border2,link2,auto2
		page.getContainer().remove(auto2);
		behaviors = page.getContainer().getBehaviors();
		assertEquals(3, behaviors.size());
		assertEquals(autoId, page.container.getBehaviorId(auto));
		assertEquals(link2Id, page.container.getBehaviorId(link2));

		// auto,border2,link2
		page.getContainer().remove(link2); // last IBehaviorListener
		behaviors = page.getContainer().getBehaviors();
		assertEquals(2, behaviors.size());
		assertEquals(autoId, page.container.getBehaviorId(auto));
		assertEquals(border2Id, page.container.getBehaviorId(border2));

	}

	private static class LinkBehavior extends AbstractBehavior implements IBehaviorListener
	{
		private static final long serialVersionUID = 1L;

		private final String attr;

		public LinkBehavior(String attr)
		{
			this.attr = attr;
		}

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			super.onComponentTag(component, tag);
			tag.put(attr, component.urlFor(this, IBehaviorListener.INTERFACE));
		}

		public void onRequest()
		{
		}
	}

	private static class MyPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private final WebMarkupContainer container;

		public MyPage()
		{
			container = new WebMarkupContainer("container");
			add(container);
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><a wicket:id='container'></a></body></html>");
		}

		public WebMarkupContainer getContainer()
		{
			return container;
		}

	}


}
