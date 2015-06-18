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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/** IBehavior array management tests */
public class ImmutableBehaviorIdsTest extends WicketTestCase
{
	/** Tests simple behavior */
	@Test
	public void simple()
	{
		MyPage page = new MyPage();
		page.getContainer().add(new AttributeModifier("class", "border"));
		tester.startPage(page);

		assertTrue(tester.getLastResponseAsString().contains("class=\"border\""));
	}

	/**
	 * Tests the fact that url-behavior indexes do not change even if behaviors are removed/added
	 */
	@Test
	public void urlIndexRendering()
	{
		Behavior border = new AttributeModifier("class", "border");
		Behavior border2 = new AttributeModifier("class2", "border");
		Behavior auto = new AttributeModifier("autocomplete", "off");
		Behavior auto2 = new AttributeModifier("autocomplete2", "off");
		Behavior link = new LinkBehavior("href");
		Behavior link2 = new LinkBehavior("onclick");

		MyPage page = new MyPage();

		page.getContainer().add(border, auto, link, border2, link2, auto2);
		tester.startPage(page);

		String output = tester.getLastResponseAsString();
//		System.out.println(output);
		assertTrue(output.contains("class=\"border\""));
		assertTrue(output.contains("autocomplete=\"off\""));
		assertTrue(output.contains("class2=\"border\""));
		assertTrue(output.contains("autocomplete2=\"off\""));
		assertTrue(output.contains("IBehaviorListener.0"));
		assertTrue(output.contains("IBehaviorListener.1"));
		assertEquals(link, page.getContainer().getBehaviorById(0));
		assertEquals(link2, page.getContainer().getBehaviorById(1));


		// if we remove a behavior that is before the ibehaviorlistener its url index should not
		// change

		page.getContainer().remove(border);
		page.getContainer().remove(border2);
		page.getContainer().remove(auto);
		page.getContainer().remove(auto2);
		tester.startPage(page);
		output = tester.getLastResponseAsString();
//		System.out.println(output);
		assertTrue(output.contains("IBehaviorListener.0"));
		assertTrue(output.contains("IBehaviorListener.1"));
		assertEquals(link, page.getContainer().getBehaviorById(0));
		assertEquals(link2, page.getContainer().getBehaviorById(1));
	}

	/**
	 * Tests that removal of behaviors properly cleans up the data array
	 */
	@Test
	public void behaviorDataArrayCleanup()
	{
		Behavior border = new AttributeModifier("class", "border");
		Behavior border2 = new AttributeModifier("class2", "border");
		Behavior auto = new AttributeModifier("autocomplete", "off");
		Behavior auto2 = new AttributeModifier("autocomplete2", "off");
		Behavior link = new LinkBehavior("href");
		Behavior link2 = new LinkBehavior("onclick");

		MyPage page = new MyPage();
		page.getContainer().add(border, auto, link, border2, link2, auto2);

		int borderId = page.container.getBehaviorId(border);
		int border2Id = page.container.getBehaviorId(border2);
		int autoId = page.container.getBehaviorId(auto);
		int auto2Id = page.container.getBehaviorId(auto2);
		int linkId = page.container.getBehaviorId(link);
		int link2Id = page.container.getBehaviorId(link2);

		List<? extends Behavior> behaviors = page.getContainer().getBehaviors();
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

	private static class LinkBehavior extends Behavior implements IBehaviorListener
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
			tag.put(attr, component.urlFor(this, IBehaviorListener.INTERFACE, new PageParameters()));
		}

		@Override
		public void onRequest()
		{
		}
	}

	private static class MyPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;
		private final WebMarkupContainer container;

		public MyPage()
		{
			container = new WebMarkupContainer("container");
			add(container);
		}

		@Override
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
