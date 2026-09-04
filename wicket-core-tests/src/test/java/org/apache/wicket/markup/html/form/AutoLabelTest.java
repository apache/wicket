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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests {@code wicket:for} attribute functionality
 * 
 * @author igor
 */
class AutoLabelTest extends WicketTestCase
{
	/** */
	@Test
	void labelIntoMarkupInsertion()
	{
		class MyTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			MyTestPage(String labelMarkup)
			{
				super("<label wicket:for='t'>" + labelMarkup + "</label>");
				field.setLabel(Model.of("t"));
			}
		}

		// simple insertion
		assertRendered(new MyTestPage("<wicket:label>text</wicket:label>"), ">t</label>");

		// preserves markup before and after
		assertRendered(new MyTestPage(" <div> a </div> <wicket:label>text</wicket:label> b "),
			" <div> a </div> t b ");

		// embedded span tags
		assertRendered(new MyTestPage(" a <div> b <wicket:label>text</wicket:label> c </div> d"),
			" a <div> b t c </div> d");

		// no span - no insertion
		assertRendered(new MyTestPage(" text "), " text ");

		// empty label tag
		assertRendered(new MyTestPage(""), "></label>");

		// empty span tag
		assertRendered(new MyTestPage("<wicket:label></wicket:label>"), ">t</label>");

		// open/close span tag
		assertRendered(new MyTestPage("<wicket:label/>"), ">t</label>");
	}

	/** */
	@Test
	void markupIntoLabelInsertion()
	{
		class MyTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			MyTestPage(String labelMarkup)
			{
				super("<label wicket:for='t'>" + labelMarkup + "</label>");
			}
		}

		// test form component label is defaulted to the contents of span class='label-text'

		MyTestPage page = new MyTestPage("<wicket:label>text</wicket:label>");
		tester.startPage(page);
		assertEquals("text",
			((MyTestPage)tester.getLastRenderedPage()).field.getLabel().getObject());
	}

	/** */
	@Test
	void labelTagClasses()
	{
		class MyTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			MyTestPage()
			{
				super("<label wicket:for='t'><span class='label-text'>field</span></label>");
			}
		}

		class MyErrorTestPage extends MyTestPage
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				field.error("too short");
			}
		}

		// test required class
		TestPage page = new MyTestPage();
		assertNotRendered(page, "class='required'");
		page.field.setRequired(true);
		assertRendered(page, "class='required'");

		// test error class
		page = new MyTestPage();
		assertNotRendered(page, "class='error'");
		page = new MyErrorTestPage();
		assertRendered(page, "class='error'");

		// test classes are appended and not overridden
		page = new MyErrorTestPage();
		page.field.setRequired(true);
		tester.startPage(page);
		String markup = tester.getLastResponse().getDocument();
		assertTrue(markup.contains("class=\"required error\"") ||
			markup.contains("class=\"error required\""));

		// test existing classes are preserved
		class MyTestPage2 extends TestPage
		{
			private static final long serialVersionUID = 1L;

			MyTestPage2()
			{
				super(
					"<label class='long' wicket:for='t'><wicket:label>field</wicket:label></label>");
			}
		}

		MyTestPage2 page2 = new MyTestPage2();
		page2.field.setRequired(true);
		tester.startPage(page2);
		markup = tester.getLastResponse().getDocument();
		assertTrue(markup.contains("class=\"required long\"") ||
			markup.contains("class=\"long required\""));

	}

	void labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated(String autoLabelMarkup)
	{
		class MyAjaxTestPage extends TestPage
		{
			private static final long serialVersionUID = 1L;

			// we define a label that is auto-synchronized with component
			MyAjaxTestPage()
			{
				super(autoLabelMarkup);
			}
		}

		TestPage page = new MyAjaxTestPage();
		assertNotRendered(page, "class='required'");
		page.field.setRequired(true);
		assertRendered(page, "class='required'");

		tester.executeAjaxEvent(page.submit, "click");
		assertRendered("class='required error'");
		// toggling required and updating the field.
		tester.executeAjaxEvent(page.toggleRequired, "click");
		//required should be removed from associated label
		assertRendered(toggleString(page.field, "required", false));
		// now when submitting label has no error.
		tester.executeAjaxEvent(page.submit, "click");
		assertNotRendered( "class='error'");
		// set required back.
		tester.executeAjaxEvent(page.toggleRequired, "click");
		//required should be added from associated label
		assertRendered(toggleString(page.field, "required", true));
		// again whe have reqiered and error
		tester.executeAjaxEvent(page.submit, "click");
		assertRendered("class='required error'");
		// clear the errors
		tester.executeAjaxEvent(page.clearErrors, "click");
		assertRendered(toggleString(page.field, "error", false));
		// add some error
		tester.executeAjaxEvent(page.addError, "click");
		assertRendered(toggleString(page.field, "error", true));
	}

	// WICKET-7101
	@Test
	void labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdatedExplicitAutoAttributeIsSetToTrue()
	{
		labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated("<label wicket:for='t' wicket:auto='true'><span class='label-text'>field</span></label>");
	}

	@Test
	void labelTagClassesAreNotUpdatedWhenRelatedFormComponentIsUpdatedExplicitAutoAttributeIsSetToFalse()
	{
		try {
			labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated("<label wicket:for='t' wicket:auto='false'><span class='label-text'>field</span></label>");
			fail("Auto-label auto refresh should fave failed");
		} catch (AssertionError e) {
			// ok
		}

	}

	@Test
	void labelTagClassesAreNotUpdatedWhenRelatedFormComponentIsUpdatedWhenNoAutoAttributeIsSet()
	{
		try
		{
			labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated("<label wicket:for='t'><span class='label-text'>field</span></label>");
			fail("Auto-label auto refresh should fave failed");
		} catch (AssertionError e) {
			// ok
		}
	}

	@Test
	void labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdatedWhenRelatedMarkupSettingsIsSet()
	{
		tester.getApplication().getMarkupSettings().setUpdateAutoLabelsTogetherWithFormComponent(true);
		labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated("<label wicket:for='t'><span class='label-text'>field</span></label>");
	}

	@Test
	void labelTagClassesAreNotUpdatedWhenRelatedFormComponentIsUpdatedWhenRelatedMarkupSettingsIsSetToFalseOrNotSet()
	{
		try {
			// updateAutoLabelsTogetherWithFormComponent is set to false by default
			labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated("<label wicket:for='t'><span class='label-text'>field</span></label>");
			fail("Auto-label auto refresh should fave failed");
		} catch (AssertionError e) {
			//ok.
		}

		try {
			tester.getApplication().getMarkupSettings().setUpdateAutoLabelsTogetherWithFormComponent(false);
			labelTagClassesAreUpdatedWhenRelatedFormComponentIsUpdated("<label wicket:for='t'><span class='label-text'>field</span></label>");
			fail("Auto-label auto refresh should fave failed");
		} catch (AssertionError e) {
			// ok
		}
	}

	private String toggleString(FormComponent<?> component, String cssClass, boolean flag) {
		// check
		return "Wicket.DOM.toggleClass('" + component.getMarkupId() + "-w-lbl', '" + cssClass + "', " + flag + ")";
	}

	private void assertRendered(Page page, String markupFragment)
	{
		tester.startPage(page);
		assertRendered(markupFragment);
	}

	private void assertRendered(String markupFragment)
	{
		String markup = tester.getLastResponse().getDocument();
		markup = markup.replace("'", "\"");
		assertTrue(markup.contains(markupFragment.replace("'", "\"")),
				"fragment: [" + markupFragment + "] not found in generated markup: [" + markup + "]");
	}

	private void assertNotRendered(Page page, String markupFragment) {
		tester.startPage(page);
		assertNotRendered(markupFragment);
	}

	private void assertNotRendered(String markupFragment)
	{
		String markup = tester.getLastResponse().getDocument();
		markup = markup.replace("'", "\"");
		assertFalse(markup.contains(markupFragment.replace("'", "\"")),
				"fragment: [" + markupFragment + "] not found in generated markup: [" + markup + "]");
	}

	private static class TestPage extends WebPage
	{
		private static final long serialVersionUID = 1L;
		private final String labelMarkup;
		TextField<String> field;

		AjaxSubmitLink submit;

		FeedbackPanel feedback;

		AjaxLink<Void> toggleRequired;

		AjaxLink<Void> clearErrors;

		AjaxLink<Void> addError;

		TestPage(String labelMarkup)
		{
			this.labelMarkup = labelMarkup;
			feedback = new FeedbackPanel("feedback");
			feedback.setOutputMarkupId(true);
			add(feedback);
			Form<?> form = new Form<Void>("f");
			form.setOutputMarkupPlaceholderTag(true);
			add(form);
			form.add(field = new TextField<String>("t", Model.of("")));

			field.setOutputMarkupId(true);
			submit = new AjaxSubmitLink("submit")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget target)
				{
					target.add(form, feedback);
				}

				@Override
				protected void onError(AjaxRequestTarget target)
				{
					target.add(form, feedback);
				}
			};

			form.add(submit);
			toggleRequired = new AjaxLink<Void>("toggleRequired")
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					field.setRequired(!field.isRequired());
					target.add(field, feedback);
				}
			};

			add(toggleRequired);

			clearErrors = new AjaxLink<Void>("clearErrors") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					field.getFeedbackMessages().clear();
					target.add(field, feedback);
				}
			};
			add(clearErrors);

			addError = new AjaxLink<Void>("addError") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					field.error("Test");
					target.add(field, feedback);
				}
			};
			add(addError);
		}

		@Override
		public IMarkupFragment getMarkup()
		{
			return Markup.of("<html><body><div wicket:id='feedback'></div><form wicket:id='f'>\n" + labelMarkup +
				"\n<input type='text' wicket:id='t'/>\n<a wicket:id='submit'>Submit</a></form>\n<a wicket:id='toggleRequired'>Toggle required</a>" +
					"\n<a wicket:id='clearErrors'>clearErrors</a>\n<a wicket:id='addError'>addError</a></body></html>");
		}
	}
}
