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

import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class AutoFormLabelPickupTest extends WicketTestCase
{
	public static class PrintLabelPage extends WebPage
	{
		public PrintLabelPage(IModel labelModel)
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField("input", Model.of("")).setLabel(labelModel));
		}
	}
	public static class PickUpLabelPage extends WebPage
	{
		public PickUpLabelPage(IModel labelModel)
		{
			Form form = new Form("form");
			add(form);
			form.add(new TextField("inputMarkupLabel", Model.of("")).setLabel(labelModel));
			form.add(new TextField("inputPropertiesLabel", Model.of("")).setLabel(labelModel));
			form.add(new TextField("inputWithoutAutolabel", Model.of("")).setLabel(labelModel));
			form.add(new TextField("inputWithDefaultLabel", Model.of("")));
		}
	}

	/**
	 * 
	 */
	@Before
	public void before()
	{
		Session.get().setLocale(Locale.US);
	}

	@Test
	public void labelIsPrintedFromModel() throws Exception
	{
		tester.startPage(new PrintLabelPage(Model.of("label from model")));
		tester.assertContains("<label wicket:for=\"input\" id=\"input2-w-lbl\" for=\"input2\">\\|label from model\\|</label>");
	}

	@Test
	public void labelIsPrintedFromProperties() throws Exception
	{
		tester.startPage(new PrintLabelPage(Model.of((String)null)));
		tester.assertContains("<label wicket:for=\"input\" id=\"input2-w-lbl\" for=\"input2\">\\|label from properties\\|</label>");
	}

	@Test
	public void labelIsPickedUpFromMarkup() throws Exception
	{
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"label from markup",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputMarkupLabel")).getLabel()
				.getObject());
	}

	@Test
	public void labelIsPickedUpFromProperties() throws Exception
	{
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"label from properties",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputPropertiesLabel")).getLabel()
				.getObject());
	}

	@Test
	public void withoutAutolabel() throws Exception
	{
		tester.startPage(new PickUpLabelPage(null));
		tester.assertContains("<label>label from markup without autolabel</label>");
		assertEquals(
			"label from markup without autolabel",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputWithoutAutolabel")).getLabel()
				.getObject());
	}

	@Test
	public void localeChangesAreDetectedWithExplicitMessageKeys() throws Exception
	{
		Session.get().setLocale(Locale.GERMAN);
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"label from properties DE",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputPropertiesLabel")).getLabel()
				.getObject());
		tester.assertContains("label from properties DE");

		Session.get().setLocale(Locale.FRENCH); // change locale to see whether it picks it up
		Page page = tester.getLastRenderedPage();
		page.detach(); // make sure everything is detached after we just talked to that label model
		tester.startPage(page); // just re-render the same page instance with the new locale
		assertEquals(
			"label from properties FR",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputPropertiesLabel")).getLabel()
				.getObject());
		tester.assertContains("label from properties FR");
	}

	@Test
	public void localeChangesAreDetectedWithDefaultLabels() throws Exception
	{
		Session.get().setLocale(Locale.GERMAN);
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"propertiesDefaultLabel DE",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputWithDefaultLabel")).getLabel()
				.getObject());
		tester.assertContains("propertiesDefaultLabel DE");

		Session.get().setLocale(Locale.FRENCH); // change locale to see whether it picks it up
		Page page = tester.getLastRenderedPage();
		page.detach(); // make sure everything is detached after we just talked to that label model
		tester.startPage(page); // just re-render the same page instance with the new locale
		assertEquals(
			"propertiesDefaultLabel FR",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputWithDefaultLabel")).getLabel()
				.getObject());
		tester.assertContains("propertiesDefaultLabel FR");
	}

	@Test
	public void defaultLabelIsPickedUpFromProperties() throws Exception
	{
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"propertiesDefaultLabel",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputWithDefaultLabel")).getLabel()
				.getObject());
	}
}