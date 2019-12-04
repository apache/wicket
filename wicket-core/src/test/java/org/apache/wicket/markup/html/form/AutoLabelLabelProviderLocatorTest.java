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
package org.apache.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@code wicket:for} attribute functionality using {@link ILabelProviderLocator}
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class AutoLabelLabelProviderLocatorTest extends WicketTestCase
{
	static class ILabelProviderLocatorPanel1 extends Panel implements ILabelProviderLocator
	{

		private TextField<String> text;

		ILabelProviderLocatorPanel1(String id)
		{
			super(id);
			WebMarkupContainer dummy = new WebMarkupContainer("dummy");
			dummy.add(AttributeModifier.replace("class", "dummy1"));
			add(dummy);
			text = new TextField<>("text", Model.of(""));
			dummy.add(text);
		}

		@Override
		public Component getAutoLabelComponent()
		{
			return text;
		}
	}

	static class ILabelProviderLocatorPanel2 extends Panel implements ILabelProviderLocator
	{

		private TextField<String> text;

		ILabelProviderLocatorPanel2(String id)
		{
			super(id);
			WebMarkupContainer dummy = new WebMarkupContainer("dummy");
			dummy.add(AttributeModifier.replace("class", "dummy"));
			add(dummy);

			WebMarkupContainer dummy1 = new WebMarkupContainer("dummy1");
			dummy1.add(AttributeModifier.replace("class", "dummy1"));
			dummy.add(dummy1);

			text = new TextField<>("text", Model.of(""));
			dummy1.add(text);
		}

		@Override
		public Component getAutoLabelComponent()
		{
			return text;
		}
	}

	static class WrongLabelProviderLocatorPanel extends Panel implements ILabelProviderLocator
	{

		private WebMarkupContainer dummy;

		WrongLabelProviderLocatorPanel(String id)
		{
			super(id);
			dummy = new WebMarkupContainer("dummy");
			dummy.add(AttributeModifier.replace("class", "dummy1"));
			add(dummy);
			TextField<String> text = new TextField<>("text", Model.of(""));
			dummy.add(text);
		}

		@Override
		public Component getAutoLabelComponent()
		{
			return dummy;
		}
	}

	private interface IEditPanelProvider
	{
		IModel<String> getLabelText();

		Panel createEditPanel(String id);
	}

	static class EditPage extends WebPage
	{
		EditPage(List<IEditPanelProvider> editPanelProviders)
		{
			Form<Void> form = new Form<>("form");
			add(form);
			RepeatingView editRow = new RepeatingView("edit-row");
			form.add(editRow);
			for (IEditPanelProvider panelProvider: editPanelProviders) {
				WebMarkupContainer mc = new WebMarkupContainer(editRow.newChildId());
				mc.add(new Label("label", panelProvider.getLabelText()));
				mc.add(panelProvider.createEditPanel("edit-component"));
				editRow.add(mc);
			}
		}
	}

	@Test
	public void testILabelProviderLocator()
	{
		List<IEditPanelProvider> providers = new ArrayList<>();

		providers.add(new IEditPanelProvider()
		{
			@Override
			public IModel<String> getLabelText()
			{
				return Model.of("Example1");
			}

			@Override
			public Panel createEditPanel(String id)
			{
				return new ILabelProviderLocatorPanel1(id);
			}
		});

		providers.add(new IEditPanelProvider()
		{
			@Override
			public IModel<String> getLabelText()
			{
				return Model.of("Example2");
			}

			@Override
			public Panel createEditPanel(String id)
			{
				return new ILabelProviderLocatorPanel2(id);
			}
		});

		EditPage editPage = new EditPage(providers);
		tester.startPage(editPage);
		tester.assertRenderedPage(EditPage.class);
		tester.assertContains("for=\"dummy_text\"><span wicket:id=\"label\">Example1</span>");
		tester.assertContains("for=\"dummy_dummy1_text\"><span wicket:id=\"label\">Example2</span>");
	}

	@Test
	public void testFailingILabelProviderLocator()
	{
		List<IEditPanelProvider> providers = new ArrayList<>();

		providers.add(new IEditPanelProvider()
		{
			@Override
			public IModel<String> getLabelText()
			{
				return Model.of("Example1");
			}

			@Override
			public Panel createEditPanel(String id)
			{
				return new ILabelProviderLocatorPanel1(id);
			}
		});

		providers.add(new IEditPanelProvider()
		{
			@Override
			public IModel<String> getLabelText()
			{
				return Model.of("Example2");
			}

			@Override
			public Panel createEditPanel(String id)
			{
				return new WrongLabelProviderLocatorPanel(id);
			}
		});

		try
		{
			EditPage editPage = new EditPage(providers);
			tester.startPage(editPage);
			Assert.fail("Page rendering should produce a WicketRuntimeException");
		}
		catch (WicketRuntimeException e)
		{
            Assert.assertEquals("Component 'org.apache.wicket.markup.html.WebMarkupContainer', pointed to by wicket:for attribute 'edit-component', does not implement org.apache.wicket.markup.html.form.ILabelProvider", e.getMessage());
		}
 	}

}
