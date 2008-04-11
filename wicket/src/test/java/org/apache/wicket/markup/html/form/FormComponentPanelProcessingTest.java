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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;

/**
 * Test form component panel related form processing
 * 
 * @author ivaynberg
 */
public class FormComponentPanelProcessingTest extends WicketTestCase
{

	/**
	 * Test processing order of form component panel and its containing children. The children
	 * should be processed first.
	 */
	public void testProcessingOrder()
	{
		tester.startPage(new TestPage());
		tester.assertRenderedPage(TestPage.class);
		FormTester ft = tester.newFormTester("form");
		ft.submit();
	}

	private static class TestFormComponentPanel extends FormComponentPanel
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private boolean childValidated = false;
		private boolean childModelUpdated = false;

		private TestFormComponentPanel(String id, IModel model)
		{
			super(id, model);
			add(new TextField("text", new Model())
			{
				private static final long serialVersionUID = 1L;

				protected void convertInput()
				{
					super.convertInput();
					childValidated = true;
				}

				public void updateModel()
				{
					super.updateModel();
					childModelUpdated = true;
				}
			});
		}

		protected void onBeforeRender()
		{
			super.onBeforeRender();
			childValidated = false;
			childModelUpdated = false;

		}

		protected void convertInput()
		{
			if (childValidated == false)
			{
				fail("Child should have been validated before parent");
			}
			super.convertInput();
		}

		public void updateModel()
		{
			if (childModelUpdated == false)
			{
				fail("Child's model not updated before parent's");

			}
			super.updateModel();
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class containerClass)
		{
			return new StringResourceStream(
				"<wicket:panel><input wicket:id='text' type='text'/></wicket:panel>");
		}
	}


	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		public TestPage()
		{
			Form form = new Form("form");
			add(form);
			form.add(new TestFormComponentPanel("panel", new Model()));
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class containerClass)
		{
			return new StringResourceStream(
				"<body><form wicket:id='form'><div wicket:id='panel'></div></form></body>");
		}

	}
}
