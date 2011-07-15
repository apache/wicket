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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTester;

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
		}
	}


	public void testLabelIsPrintedFromModel() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new PrintLabelPage(Model.of("label from model")));
		System.out.println(tester.getServletResponse().getDocument());
		tester.assertContains("<label wicket:for=\"input\" for=\"input2\">\\|label from model\\|</label>");
	}

	public void testLabelIsPrintedFromProperties() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new PrintLabelPage(Model.of((String)null)));
		tester.assertContains("<label wicket:for=\"input\" for=\"input2\">\\|label from properties\\|</label>");
	}

	public void testLabelIsPickedUpFromMarkup() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"label from markup",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputMarkupLabel")).getLabel()
				.getObject());
	}

	public void testLabelIsPickedUpFromProperties() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new PickUpLabelPage(null));
		assertEquals(
			"label from properties",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputPropertiesLabel")).getLabel()
				.getObject());
	}

	public void testWithoutAutolabel() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.startPage(new PickUpLabelPage(null));
		tester.dumpPage();
		tester.assertContains("<label>label from markup without autolabel</label>");
		assertEquals(
			"label from markup without autolabel",
			((FormComponent)tester.getComponentFromLastRenderedPage("form:inputWithoutAutolabel")).getLabel()
				.getObject());
	}
}
