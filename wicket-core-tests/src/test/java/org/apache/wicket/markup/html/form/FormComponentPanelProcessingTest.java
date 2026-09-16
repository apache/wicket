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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Serializable;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Test form component panel related form processing
 * 
 * @author ivaynberg
 */
class FormComponentPanelProcessingTest extends WicketTestCase
{

	/**
	 * Test processing order of form component panel and its containing children. The children
	 * should be processed first.
	 */
	@Test
	void processingOrder()
	{
		tester.startPage(new TestPage());
		tester.assertRenderedPage(TestPage.class);
		FormTester ft = tester.newFormTester("form");
		ft.submit();
	}

	@Test
	void clearInput()
	{
		tester.startPage(new TestPage());
		tester.assertRenderedPage(TestPage.class);

		TestFormComponentPanel fcp = (TestFormComponentPanel) tester.getComponentFromLastRenderedPage("form:panel");
		assertEquals(false, fcp.isChildClearInputCalled());

		fcp.clearInput();
		assertEquals(true, fcp.isChildClearInputCalled());
	}

	@Test
	void ajaxFormComponentUpdatingBehavior()
	{
		AjaxFormComponentUpdatingBehavior ajaxBehavior = new AjaxFormComponentUpdatingBehavior("update")
		{
			
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				//"updating"
			}			
		};
		
		OuterFormComponentPanel ofcp = new OuterFormComponentPanel("outPanel");
		ofcp.add(ajaxBehavior);
		ofcp.setOutputMarkupId(true);
		
		tester.startComponentInPage(ofcp);
		
		tester.executeBehavior(ajaxBehavior);
		
		//inner component has a required field we haven't set
		assertEquals(false, ofcp.isValid());
		assertEquals(false, ofcp.childModelUpdated);
		
		//let's set the inner field
		tester.getRequest()
			.getPostParameters()
			.setParameterValue(((TextField<?>) ofcp.inner.get("subfield")).getInputName(), "textVal");
	
		//now submission should regularly happen 
		tester.executeBehavior(ajaxBehavior);
		assertEquals(true, ofcp.childModelUpdated);
		assertEquals(true, ofcp.inner.isValid());
	}
	
	private static class TestFormComponentPanel extends FormComponentPanel<Serializable>
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private boolean childValidated = false;
		private boolean childModelUpdated = false;
		private boolean childClearInputCalled = false;

		private TestFormComponentPanel(String id, IModel<Serializable> model)
		{
			super(id, model);
			add(new TextField<Serializable>("text", new Model<>())
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void convertInput()
				{
					super.convertInput();
					childValidated = true;
				}

				@Override
				public void updateModel()
				{
					super.updateModel();
					childModelUpdated = true;
				}

				@Override
				public void clearInput()
				{
					super.clearInput();
					childClearInputCalled = true;
				}
			});
		}

		private boolean isChildClearInputCalled() {
			return childClearInputCalled;
		}

		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
			childValidated = false;
			childModelUpdated = false;

		}

		@Override
		public void convertInput()
		{
			if (childValidated == false)
			{
				fail("Child should have been validated before parent");
			}
			super.convertInput();
		}

		@Override
		public void updateModel()
		{
			if (childModelUpdated == false)
			{
				fail("Child's model not updated before parent's");

			}
			super.updateModel();
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<wicket:panel><input wicket:id='text' type='text'/></wicket:panel>");
		}
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		TestPage()
		{
			Form<Void> form = new Form<>("form");
			add(form);
			form.add(new TestFormComponentPanel("panel", new Model<>()));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<body><form wicket:id='form'><div wicket:id='panel'></div></form></body>");
		}

	}
	
	private static class OuterFormComponentPanel extends FormComponentPanel<String> implements
		IMarkupResourceStreamProvider
		{
			public InnerFormComponentPanel inner = new InnerFormComponentPanel("inner");
			public boolean childModelUpdated;
	
			public OuterFormComponentPanel(String id)
			{
				super(id, Model.of());
	
				add(inner);
				add(new TextField<String>("username", Model.of("")));
			}
			
			@Override
			public void updateModel() 
			{
				super.updateModel();
				childModelUpdated = true;
			}
			
			@Override
			public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
			{
				return new StringResourceStream(
					"<wicket:panel><span wicket:id=\"inner\"></span><input type=\"text\" id=\"username\" name=\"username\" wicket:id=\"username\"></wicket:panel>");
			}
		
		}
	
		private static class InnerFormComponentPanel extends FormComponentPanel<String> implements
		IMarkupResourceStreamProvider
		{
			public boolean wasAskedToProcessInput;
	
			public InnerFormComponentPanel(String id)
			{
				super(id, Model.of());
				add(new TextField<String>("subfield", Model.of("")).setRequired(true));
			}
	
			
			@Override
			public void validate()
			{
				wasAskedToProcessInput = true;
	
				super.validate();
			}
			
			@Override
			public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
			{
				return new StringResourceStream(
					"<wicket:panel><input type=\"text\" id=\"subfield\" name=\"subfield\" wicket:id=\"subfield\" required></wicket:panel>");
			}
		}
}
