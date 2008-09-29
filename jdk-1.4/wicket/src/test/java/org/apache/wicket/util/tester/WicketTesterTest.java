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
package org.apache.wicket.util.tester;

import java.util.Locale;

import junit.framework.TestCase;

import org.apache.wicket.*;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.PackageResource.PackageResourceBlockedException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.util.tester.MockPageParameterPage.MockInnerClassPage;
import org.apache.wicket.util.tester.MockPageWithFormAndAjaxFormSubmitBehavior.Pojo;
import org.apache.wicket.util.tester.apps_1.Book;
import org.apache.wicket.util.tester.apps_1.CreateBook;
import org.apache.wicket.util.tester.apps_1.MyMockApplication;
import org.apache.wicket.util.tester.apps_1.SuccessPage;
import org.apache.wicket.util.tester.apps_1.ViewBook;
import org.apache.wicket.util.tester.apps_6.LinkPage;
import org.apache.wicket.util.tester.apps_6.ResultPage;

/**
 * 
 * @author Juergen Donnerstag
 */
public class WicketTesterTest extends TestCase
{
	private boolean eventExecuted;
	private WicketTester tester;

	protected void setUp() throws Exception
	{
		eventExecuted = false;
		tester = new WicketTester(new MyMockApplication());
	}

	protected void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testViewBook() throws Exception
	{
		// for WebPage without default constructor, I define a TestPageSource to
		// let the page be instatiated lately.
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				Book mockBook = new Book("xxId", "xxName");
				return new ViewBook(mockBook);
			}
		});

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.assertLabel("id", "xxId");
		tester.assertLabel("name", "xxName");
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testCreateBook_validateFail() throws Exception
	{
		Session.get().setLocale(Locale.US); // fix locale
		tester.startPage(CreateBook.class);

		FormTester formTester = tester.newFormTester("createForm");

		formTester.setValue("id", "");
		formTester.setValue("name", "");
		formTester.submit();

		tester.assertRenderedPage(CreateBook.class);

		// assert error message from validation
		tester.assertErrorMessages(new String[] { "id is required", "name is required" });
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void testCreateBook_validatePass() throws Exception
	{
		tester.startPage(CreateBook.class);

		FormTester formTester = tester.newFormTester("createForm");

		formTester.setValue("id", "xxId");
		formTester.setValue("name", "xxName");
		formTester.submit();

		tester.assertRenderedPage(SuccessPage.class);

		// assert info message present.
		tester.assertInfoMessages(new String[] { "book 'xxName' created" });

		// assert previous page expired.
		// TODO Post 1.2: General: No longer a valid test
		// tester.assertExpirePreviousPage();
	}

	/**
	 * @throws Exception
	 */
	public void testBookmarkableLink() throws Exception
	{
		// for WebPage without default constructor, I define a TestPageSource to
		// let the page be instatiated lately.
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				Book mockBook = new Book("xxId", "xxName");
				return new ViewBook(mockBook);
			}
		});

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.clickLink("link");
		tester.assertRenderedPage(CreateBook.class);
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_setResponsePageClass() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page class in the link callback
		tester.clickLink("linkWithSetResponsePageClass");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "No Parameter");
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_setResponsePage() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page instance in the link callback
		tester.clickLink("linkWithSetResponsePage");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "A special label");
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_ajaxLink_setResponsePageClass() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page class in the link callback
		tester.clickLink("ajaxLinkWithSetResponsePageClass");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "No Parameter");
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_ajaxLink_setResponsePage() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page instance in the link callback
		tester.clickLink("ajaxLinkWithSetResponsePage");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "A special label");
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_ajaxFallbackLink_setResponsePageClass() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page class in the link callback
		tester.clickLink("ajaxFallbackLinkWithSetResponsePageClass");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "No Parameter");
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_ajaxFallbackLink_setResponsePage() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page instance in the link callback
		tester.clickLink("ajaxFallbackLinkWithSetResponsePage");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "A special label");
	}

	/**
	 * @throws Exception
	 */
	public void testClickLink_ajaxSubmitLink_setResponsePage() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page instance in the form submit
		tester.clickLink("form:submit");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "A form label");
	}

	/**
	 * @throws Exception
	 */
	public void testPageConstructor() throws Exception
	{
		Book mockBook = new Book("xxId", "xxName");
		Page page = new ViewBook(mockBook);
		tester.startPage(page);

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.clickLink("link");
		tester.assertRenderedPage(CreateBook.class);
	}

	/**
	 * Test instance constructor and inner page class
	 * 
	 * @throws Exception
	 */
	public void testConstructorAndInnerPage() throws Exception
	{
		tester.startPage(new MockInnerClassPage());

		// assertion
		tester.assertRenderedPage(MockInnerClassPage.class);
		tester.assertComponent("title", Label.class);
		tester.assertContains("Hello world!");
	}

	/**
	 * 
	 */
	public void testAssertComponentOnAjaxResponse()
	{
		final Page page = new MockPageWithLink();
		AjaxLink ajaxLink = new AjaxLink(MockPageWithLink.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				// Replace the link with a normal Link
				Link link = new Link(MockPageWithLink.LINK_ID)
				{
					private static final long serialVersionUID = 1L;

					public void onClick()
					{
						// Do nothing
					}
				};
				link.setOutputMarkupId(true);

				page.replace(link);

				target.addComponent(link);
			}
		};
		ajaxLink.setOutputMarkupId(true);

		page.add(ajaxLink);

		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});


		// Click the link
		tester.clickLink(MockPageWithLink.LINK_ID);

		// The link must be a Link :)
		tester.assertComponent(MockPageWithLink.LINK_ID, Link.class);

		// This must not fail
		tester.assertComponentOnAjaxResponse(MockPageWithLink.LINK_ID);

		tester.dumpPage();
	}

	/**
	 * Test that testing if a component is on the ajax response can handle if the response is
	 * encoded.
	 */
	public void testAssertComponentOnAjaxResponse_encoding()
	{
		final IModel labelModel = new IModel()
		{
			private static final long serialVersionUID = 1L;

			private String value;

			public Object getObject()
			{
				return value;
			}

			public void setObject(Object object)
			{
				value = (String)object;
			}

			public void detach()
			{
			}
		};

		labelModel.setObject("Label 1");
		final Label label = new Label(MockPageWithLinkAndLabel.LABEL_ID, labelModel);
		label.setOutputMarkupId(true);

		final Page page = new MockPageWithLinkAndLabel();
		AjaxLink ajaxLink = new AjaxLink(MockPageWithLinkAndLabel.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			public void onClick(AjaxRequestTarget target)
			{
				labelModel.setObject("Label which needs encoding: [] ][");
				target.addComponent(label);
			}
		};
		ajaxLink.setOutputMarkupId(true);

		page.add(ajaxLink);
		ajaxLink.add(label);

		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});


		// Click the link
		tester.clickLink(MockPageWithLinkAndLabel.LINK_ID);

		tester.assertComponent(MockPageWithLinkAndLabel.LABEL_PATH, Label.class);

		tester.dumpPage();

		// This must not fail
		tester.assertComponentOnAjaxResponse(MockPageWithLinkAndLabel.LABEL_PATH);

	}

	/**
	 * Test that the executeAjaxEvent on the WicketTester works.
	 */
	public void testExecuteAjaxEvent()
	{
		// Setup mocks
		final MockPageWithOneComponent page = new MockPageWithOneComponent();

		Label label = new Label("component", "Dblclick This To See Magick");
		label.add(new AjaxEventBehavior("ondblclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				eventExecuted = true;
			}
		});
		page.add(label);

		// Start the page
		tester.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		// Execute the event
		tester.executeAjaxEvent(label, "ondblclick");

		assertTrue(eventExecuted);
	}

	/**
	 * Test that the clickLink works when submitting a form with a checkgroup inside.
	 */
	public void testClickLink_ajaxSubmitLink_checkGroup()
	{
		tester.startPage(MockPageWithFormAndCheckGroup.class);

		// Click the submit
		tester.clickLink("submitLink");
	}

	public void testTesterCanBeOverridenToNotReuseExistingRequestCycleInExecuteAjaxEvent()
	{
		tester = new WicketTester(new MyMockApplication()) {
			protected WebRequestCycle resolveRequestCycle()
			{
				return setupRequestAndResponse(true);
			}
		};
		tester.startPage(MockPageWithFormAndCheckGroup.class);
		tester.executeAjaxEvent("submitLink", "onclick");
		tester.assertComponentOnAjaxResponse("submitLink");
	}

	/**
	 * Test that the executeAjaxEvent "submits" the form if the event is a AjaxFormSubmitBehavior.
	 */
	public void testExecuteAjaxEvent_ajaxFormSubmitLink()
	{
		tester.startPage(MockPageWithFormAndAjaxFormSubmitBehavior.class);

		// Get the page
		MockPageWithFormAndAjaxFormSubmitBehavior page = (MockPageWithFormAndAjaxFormSubmitBehavior)tester
				.getLastRenderedPage();

		Pojo pojo = page.getPojo();

		assertEquals("Mock name", pojo.getName());
		assertEquals("Mock name", ((TextField)tester.getComponentFromLastRenderedPage("form" +
				Component.PATH_SEPARATOR + "name")).getValue());

		assertFalse(page.isExecuted());

		// Execute the ajax event
		tester.executeAjaxEvent(MockPageWithFormAndAjaxFormSubmitBehavior.EVENT_COMPONENT,
				"onclick");

		assertTrue("AjaxFormSubmitBehavior.onSubmit() has not been executed in " +
				MockPageWithFormAndAjaxFormSubmitBehavior.class, page.isExecuted());

		assertEquals("Mock name", ((TextField)tester.getComponentFromLastRenderedPage("form" +
				Component.PATH_SEPARATOR + "name")).getValue());

		// The name of the pojo should still be the same. If the
		// executeAjaxEvent weren't submitting the form the name would have been
		// reset to null, because the form would have been updated but there
		// wouldn't be any data to update it with.
		assertNotNull("executeAjaxEvent() did not properly submit the form", pojo.getName());
		assertEquals("Mock name", pojo.getName());
	}

	/**
	 * 
	 */
	public void testRedirectWithPageParameters()
	{
		tester.startPage(MockPageParameterPage.class);

		tester.assertLabel("label", "");

		// Click the bookmarkable link
		tester.clickLink("link");

		// It should still be the MockPageParameterPage, but the
		// label should now have "1" in it because that's what comes
		// from the page parameter.
		tester.assertLabel("label", "1");
	}

	/**
	 * Test that clickLink on a ResourceLink with a ResourceReference on it works.
	 * 
	 * <p>
	 * See also WICKET-280 Allow to access html resources
	 * </p>
	 */
	public void testClickResourceLink()
	{
		try
		{
			tester.startPage(BlockedResourceLinkPage.class);
			fail("Accessing " + BlockedResourceLinkPage.class + " should have raised a " +
					PackageResourceBlockedException.class);
		}
		catch (PackageResourceBlockedException e)
		{

		}

		tester.startPage(MockResourceLinkPage.class);
		tester.clickLink("link");
		assertNull(getRequestCodingStrategy());
	}

	IRequestTargetUrlCodingStrategy getRequestCodingStrategy()
	{
		String relativePath = tester.getApplication().getWicketFilter().getRelativePath(
				tester.getServletRequest());
		return tester.getApplication().getRequestCycleProcessor().getRequestCodingStrategy()
				.urlCodingStrategyForPath(relativePath);
	}

	/**
	 * Toggle submit button to disabled state.
	 */
	public void testToggleButtonEnabledState()
	{
		tester.startPage(MockFormPage.class);
		Component submit = tester.getComponentFromLastRenderedPage("form:submit");
		assertTrue(submit.isEnabled());
		tester.createRequestCycle();
		submit.setEnabled(false);
		assertFalse(submit.isEnabled());
	}

	/**
	 * Toggle submit button to enabled when text field validates.
	 */
	public void testToggleAjaxFormButton()
	{
		tester.startPage(new MockAjaxFormPage());
		Button submit = getSubmitButton();
		assertFalse(submit.isEnabled());
		FormTester form = tester.newFormTester("form");

		tester.setupRequestAndResponse();
		form.setValue("text", "XX");
		setTextFieldAndAssertSubmit(false);
		Session.get().cleanupFeedbackMessages();

		tester.setupRequestAndResponse();
		form.setValue("text", "XXXYYYXXX");
		setTextFieldAndAssertSubmit(true);

		tester.setupRequestAndResponse();
		form.setValue("text", "");
		setTextFieldAndAssertSubmit(false);
	}

	private void setTextFieldAndAssertSubmit(boolean expected)
	{
		tester.executeAjaxEvent("form:text", "onkeyup");
		Button submit = getSubmitButton();
// System.out.println(Session.get().getFeedbackMessages());
		assertEquals(expected, submit.isEnabled());
	}

	private Button getSubmitButton()
	{
		return (Button)tester.getComponentFromLastRenderedPage("form:submit");
	}
}
