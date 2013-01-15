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

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import junit.framework.AssertionFailedError;

import org.apache.wicket.Component;
import org.apache.wicket.MockPageParametersAware;
import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.MockPageWithOneComponent;
import org.apache.wicket.MockPanelWithLink;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.pages.AccessDeniedPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResource.PackageResourceBlockedException;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.DummyPage;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.tester.DummyHomePage.TestLink;
import org.apache.wicket.util.tester.MockPageParameterPage.MockInnerClassPage;
import org.apache.wicket.util.tester.MockPageWithFormAndAjaxFormSubmitBehavior.Pojo;
import org.apache.wicket.util.tester.apps_1.Book;
import org.apache.wicket.util.tester.apps_1.CreateBook;
import org.apache.wicket.util.tester.apps_1.MyMockApplication;
import org.apache.wicket.util.tester.apps_1.SuccessPage;
import org.apache.wicket.util.tester.apps_1.ViewBook;
import org.apache.wicket.util.tester.apps_6.LinkPage;
import org.apache.wicket.util.tester.apps_6.ResultPage;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Juergen Donnerstag
 */
public class WicketTesterTest extends WicketTestCase
{
	private boolean eventExecuted;

	/**
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception
	{
		eventExecuted = false;
	}

	@Override
	protected WebApplication newApplication()
	{
		return new MyMockApplication();
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void viewBook() throws Exception
	{
		Book mockBook = new Book("xxId", "xxName");
		Page page = new ViewBook(mockBook);
		tester.startPage(page);

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.assertLabel("id", "xxId");
		tester.assertLabel("name", "xxName");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void createBook_validateFail() throws Exception
	{
		Session.get().setLocale(Locale.US); // fix locale
		tester.startPage(CreateBook.class);

		FormTester formTester = tester.newFormTester("createForm");

		formTester.setValue("id", "");
		formTester.setValue("name", "");
		formTester.submit();

		tester.assertRenderedPage(CreateBook.class);

		// assert error message from validation
		tester.assertErrorMessages("id is required", "name is required");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void createBook_validatePass() throws Exception
	{
		tester.startPage(CreateBook.class);

		FormTester formTester = tester.newFormTester("createForm");

		formTester.setValue("id", "xxId");
		formTester.setValue("name", "xxName");
		formTester.submit();

		tester.assertRenderedPage(SuccessPage.class);

		// assert info message present.
		tester.assertInfoMessages("book 'xxName' created");

		// assert previous page expired.
		// TODO Post 1.2: General: No longer a valid test
		// tester.assertExpirePreviousPage();
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void bookmarkableLink() throws Exception
	{
		// for WebPage without default constructor, I define a TestPageSource to
		// let the page be instatiated lately.
		Book mockBook = new Book("xxId", "xxName");
		tester.startPage(new ViewBook(mockBook));

		// assertion
		tester.assertRenderedPage(ViewBook.class);
		tester.clickLink("link");
		tester.assertRenderedPage(CreateBook.class);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void clickLink_setResponsePageClass() throws Exception
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
	@Test
	public void clickLink_setResponsePage() throws Exception
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
	@Test
	public void clickLink_ajaxLink_setResponsePageClass() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		// Set the response page class in the link callback
		tester.clickLink("ajaxLinkWithSetResponsePageClass");
		tester.assertRenderedPage(ResultPage.class);
		tester.assertLabel("label", "No Parameter");
	}

	/**
	 * WICKET-3164
	 * 
	 * @throws Exception
	 */
	@Test
	public void clickLink_ajaxLink_notEnabled() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		tester.getComponentFromLastRenderedPage("ajaxLinkWithSetResponsePageClass").setEnabled(
			false);
		try
		{
			tester.clickLink("ajaxLinkWithSetResponsePageClass");
			throw new RuntimeException("Disabled link should not be clickable.");
		}
		catch (AssertionFailedError _)
		{
			;
		}
	}

	/**
	 * WICKET-3164
	 * 
	 * @throws Exception
	 */
	@Test
	public void executeAjaxEvent_componentNotEnabled() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		tester.getComponentFromLastRenderedPage("ajaxLinkWithSetResponsePageClass").setEnabled(
			false);
		try
		{
			tester.executeAjaxEvent("ajaxLinkWithSetResponsePageClass", "onclick");
			throw new RuntimeException("Disabled link should not be clickable.");
		}
		catch (AssertionFailedError _)
		{
			;
		}
	}

	/**
	 * WICKET-3152
	 * 
	 * @throws Exception
	 */
	@Test
	public void assertEnabled() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		tester.getComponentFromLastRenderedPage("ajaxLinkWithSetResponsePageClass").setEnabled(
			false);
		try
		{
			tester.assertEnabled("ajaxLinkWithSetResponsePageClass");
			fail("The link must not be enabled.");
		}
		catch (AssertionFailedError _)
		{
			;
		}
	}

	/**
	 * WICKET-3152
	 * 
	 * @throws Exception
	 */
	@Test
	public void assertDisabled() throws Exception
	{
		tester.startPage(LinkPage.class);
		tester.assertRenderedPage(LinkPage.class);

		tester.getComponentFromLastRenderedPage("ajaxLinkWithSetResponsePageClass")
			.setEnabled(true);
		try
		{
			tester.assertDisabled("ajaxLinkWithSetResponsePageClass");
			fail("The link must not be disabled.");
		}
		catch (AssertionFailedError _)
		{
			;
		}
	}

	/**
	 * WICKET-3152
	 * 
	 * @throws Exception
	 */
	@Test
	public void assertRequired() throws Exception
	{
		tester.startPage(CreateBook.class);
		tester.assertRenderedPage(CreateBook.class);

		// test #1: "id" is required by default
		tester.assertRequired("createForm:id");

		FormComponent<?> bookId = (FormComponent<?>)tester.getComponentFromLastRenderedPage("createForm:id");
		try
		{
			// test #2: set it manually to not required
			bookId.setRequired(false);
			tester.assertRequired("createForm:id");
			fail("Book ID component must not be required anymore!");
		}
		catch (AssertionFailedError _)
		{
			;
		}

		try
		{
			// test #3: "createForm" is not a FormComponent
			tester.assertRequired("createForm");
		}
		catch (AssertionFailedError _)
		{
			;
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void clickLink_ajaxLink_setResponsePage() throws Exception
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
	@Test
	public void clickLink_ajaxFallbackLink_setResponsePageClass() throws Exception
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
	@Test
	public void clickLink_ajaxFallbackLink_setResponsePage() throws Exception
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
	@Test
	public void clickLink_ajaxSubmitLink_setResponsePage() throws Exception
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
	@Test
	public void pageConstructor() throws Exception
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
	@Test
	public void constructorAndInnerPage() throws Exception
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
	@Test
	public void assertComponentOnAjaxResponse()
	{
		final Page page = new MockPageWithLink();
		AjaxLink<Void> ajaxLink = new AjaxLink<Void>(MockPageWithLink.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				// Replace the link with a normal Link
				Link<Void> link = new Link<Void>(MockPageWithLink.LINK_ID)
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick()
					{
					}
				};
				link.setOutputMarkupId(true);

				page.replace(link);

				target.add(link);
			}
		};
		ajaxLink.setOutputMarkupId(true);

		page.add(ajaxLink);

		tester.startPage(page);

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
	@Test
	public void assertComponentOnAjaxResponse_encoding()
	{
		final IModel<String> labelModel = new IModel<String>()
		{
			private static final long serialVersionUID = 1L;

			private String value;

			@Override
			public String getObject()
			{
				return value;
			}

			@Override
			public void setObject(String object)
			{
				value = object;
			}

			@Override
			public void detach()
			{
			}
		};

		labelModel.setObject("Label 1");
		final Label label = new Label(MockPageWithLinkAndLabel.LABEL_ID, labelModel);
		label.setOutputMarkupId(true);

		final Page page = new MockPageWithLinkAndLabel();
		AjaxLink<Void> ajaxLink = new AjaxLink<Void>(MockPageWithLinkAndLabel.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				labelModel.setObject("Label which needs encoding: [] ][");
				target.add(label);
			}
		};
		ajaxLink.setOutputMarkupId(true);

		page.add(ajaxLink);
		ajaxLink.add(label);

		tester.startPage(page);

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
	@Test
	public void executeAjaxEvent()
	{
		// Setup mocks
		final MockPageWithOneComponent page = new MockPageWithOneComponent();

		Label label = new Label("component", "Dblclick This To See Magick");
		label.add(new AjaxEventBehavior("ondblclick")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				eventExecuted = true;
			}
		});
		page.add(label);

		// Start the page
		tester.startPage(page);

// tester.setupRequestAndResponse();

		// Execute the event
		tester.executeAjaxEvent(label, "ondblclick");

		assertTrue(eventExecuted);
	}

	/**
	 * Test that the clickLink works when submitting a form with a checkgroup inside.
	 */
	@Test
	public void clickLink_ajaxSubmitLink_checkGroup()
	{
		tester.startPage(MockPageWithFormAndCheckGroup.class);

		// Click the submit
		tester.clickLink("submitLink");
	}

	/**
	 * Test that the clickLink on AjaxSubmitLink will re-submit/preserve the values for all form
	 * components WICKET-3053
	 */
	@Test
	public void clickLink_ajaxSubmitLink_preservesFormComponentValues()
	{
		tester.startPage(MockPageAjaxSubmitLinkSubmitsWholeForm.class);

		tester.assertRenderedPage(MockPageAjaxSubmitLinkSubmitsWholeForm.class);

		FormTester formTester = tester.newFormTester("form");

		formTester.setValue("name", "Bob");

// do not call 'formTester.submit()'. The value of 'name' will be submitted by
// AjaxSubmitLink
// formTester.submit();

		// this will submit 'name=Bob'
		tester.clickLink("helloSubmit", true);

		tester.assertModelValue("form:name", "Bob");

		tester.assertComponentOnAjaxResponse("text");

		tester.assertModelValue("text", "Hello Bob");

		// this should preserve the value of 'name'
		tester.clickLink("goodbyeSubmit", true);

		tester.assertModelValue("form:name", "Bob");

		tester.assertComponentOnAjaxResponse("text");

		tester.assertModelValue("text", "Goodbye Bob");
	}

	/**
	 * Test that the executeAjaxEvent "submits" the form if the event is a AjaxFormSubmitBehavior.
	 */
	@Test
	public void executeAjaxEvent_ajaxFormSubmitLink()
	{
		tester.startPage(MockPageWithFormAndAjaxFormSubmitBehavior.class);

		// Get the page
		MockPageWithFormAndAjaxFormSubmitBehavior page = (MockPageWithFormAndAjaxFormSubmitBehavior)tester.getLastRenderedPage();

		Pojo pojo = page.getPojo();

		assertEquals("Mock name", pojo.getName());
		TextField<?> name = (TextField<?>)tester.getComponentFromLastRenderedPage("form:name");
		assertEquals("Mock name", name.getValue());

		assertFalse(page.isExecuted());

		tester.getRequest().getPostParameters().setParameterValue(name.getInputName(), "Mock name");

		// Execute the ajax event
		tester.executeAjaxEvent(MockPageWithFormAndAjaxFormSubmitBehavior.EVENT_COMPONENT,
			"onclick");

		assertTrue("AjaxFormSubmitBehavior.onSubmit() has not been executed in " +
			MockPageWithFormAndAjaxFormSubmitBehavior.class, page.isExecuted());

		assertEquals("Mock name",
			((TextField<?>)tester.getComponentFromLastRenderedPage("form:name")).getValue());

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
	@Test
	public void submittingFormWithAjaxEventSubmitsFormValues()
	{
		tester.startPage(MockPageWithFormAndAjaxFormSubmitBehavior.class);
		FormTester form = tester.newFormTester("form");
		form.setValue("name", "New name");
		tester.executeAjaxEvent(MockPageWithFormAndAjaxFormSubmitBehavior.EVENT_COMPONENT,
			"onclick");

		MockPageWithFormAndAjaxFormSubmitBehavior page = (MockPageWithFormAndAjaxFormSubmitBehavior)tester.getLastRenderedPage();
		Pojo pojo = page.getPojo();
		assertEquals("New name", pojo.getName());
	}

	/**
	 *
	 */
	@Test
	public void redirectWithPageParameters()
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
	 * Asserting that parameters set by user in request make part of the processed request
	 * parameters
	 */
	@Test
	public void setQueryParameter()
	{
		tester.getRequest().setParameter("p1", "v1");

		tester.startPage(MockPageParametersAware.class);

		MockPageParametersAware page = (MockPageParametersAware)tester.getLastRenderedPage();
		assertEquals("v1", page.getLastQueryParameters().getParameterValue("p1").toString());
	}

	/**
	 * Asserting that parameters set in request get processed, even if the request URL already has
	 * query parameters
	 */
	@Test
	public void setQueryParameterWhenRequestHasAnQueryUrl()
	{
		PageParameters parameters = new PageParameters();
		parameters.set("q_1", "q_1_value");
		IPageProvider testPageProvider = new PageProvider(MockPageParametersAware.class, parameters);
		IRequestHandler pageRequestHandler = new BookmarkablePageRequestHandler(testPageProvider);
		Url url = tester.getApplication().getRootRequestMapper().mapHandler(pageRequestHandler);
		tester.getRequest().setParameter("q_2", "q_2_value");
		tester.getRequest().setUrl(url);

		tester.processRequest();

		MockPageParametersAware page = (MockPageParametersAware)tester.getLastRenderedPage();
		assertEquals("q_1_value", page.getLastQueryParameters().getParameterValue("q_1").toString());
		assertEquals("q_2_value", page.getLastQueryParameters().getParameterValue("q_2").toString());
	}

	/**
	 * Asserting that multiple parameters added in request and PageParameters get processed
	 */
	@Test
	public void setMultiValueQueryParameter()
	{
		PageParameters parameters = new PageParameters();
		parameters.add("q_1", "q_1_value_1");
		parameters.add("q_1", "q_1_value_2");
		IPageProvider testPageProvider = new PageProvider(MockPageParametersAware.class, parameters);
		IRequestHandler pageRequestHandler = new BookmarkablePageRequestHandler(testPageProvider);
		Url url = tester.getApplication().getRootRequestMapper().mapHandler(pageRequestHandler);
		tester.getRequest().addParameter("q_2", "q_2_value_1");
		tester.getRequest().addParameter("q_2", "q_2_value_2");
		tester.getRequest().setUrl(url);

		tester.processRequest();

		MockPageParametersAware page = (MockPageParametersAware)tester.getLastRenderedPage();
		IRequestParameters lastQueryParameter = page.getLastQueryParameters();
		List<StringValue> q1ParameterValues = lastQueryParameter.getParameterValues("q_1");
		assertTrue(q1ParameterValues.contains(StringValue.valueOf("q_1_value_1")));
		assertTrue(q1ParameterValues.contains(StringValue.valueOf("q_1_value_2")));
		List<StringValue> q2ParameterValues = lastQueryParameter.getParameterValues("q_2");
		assertTrue(q2ParameterValues.contains(StringValue.valueOf("q_2_value_1")));
		assertTrue(q2ParameterValues.contains(StringValue.valueOf("q_2_value_2")));
	}

	/**
	 * Asserting the parameters set by user in request get processed when submitting a form
	 */
	@Test
	public void parametersOnFormSubmit()
	{
		tester.startPage(MockPageParametersAware.class);

		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("textfield", "v1");
		tester.getRequest().getPostParameters().setParameterValue("p_1", "p_1_value");
		tester.getRequest().setParameter("q_1", "q_1_value");

		formTester.submit();

		MockPageParametersAware page = (MockPageParametersAware)tester.getLastRenderedPage();
		assertEquals("v1", page.getLastPostParameters().getParameterValue("textfield").toString());
		assertEquals("p_1_value", page.getLastPostParameters().getParameterValue("p_1").toString());
		assertEquals("q_1_value", page.getLastQueryParameters().getParameterValue("q_1").toString());
	}

	/**
	 * Asserting the parameters set by user on request get processed when submitting a form
	 */
	@Test
	public void multiValueParametersOnFormSubmit()
	{
		tester.startPage(MockPageParametersAware.class);

		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("textfield", "v1");
		tester.getRequest().getPostParameters().setParameterValue("p_1", "p_1_value");
		tester.getRequest().setParameter("q_1", "q_1_value");

		formTester.submit();

		MockPageParametersAware page = (MockPageParametersAware)tester.getLastRenderedPage();
		assertEquals("v1", page.getLastPostParameters().getParameterValue("textfield").toString());
		assertEquals("p_1_value", page.getLastPostParameters().getParameterValue("p_1").toString());
		assertEquals("q_1_value", page.getLastQueryParameters().getParameterValue("q_1").toString());
	}

	/**
	 * Loading of page markup resource should not be allowed
	 */
	@Test(expected = PackageResourceBlockedException.class)
	public void loadPageMarkupTemplate()
	{
		String url = "wicket/resource/" + BlockedResourceLinkPage.class.getName() + "/" +
			BlockedResourceLinkPage.class.getSimpleName() + ".html";
		tester.executeUrl(url);
	}

	/**
	 * WICKET-280 Allow to access html resources (non-page markup templates)
	 */
	@Test
	public void loadNonPageMarkupTemplate()
	{
		String url = "wicket/resource/" + BlockedResourceLinkPage.class.getName() + "/test.html";
		tester.executeUrl(url);
		assertEquals("This is a test!", tester.getLastResponseAsString());
	}

	/**
	 * Comma separated extensions should not be allowed. The result is kinda error code 404
	 * (resource not found)
	 */
	@Test
	public void clickResourceLinkWithSomeCommaAppendedUrl()
	{
		String url = "wicket/resource/" + BlockedResourceLinkPage.class.getName() + "/" +
			BlockedResourceLinkPage.class.getSimpleName() + ".html,xml";
		tester.executeUrl(url);
		assertNull("Comma separated extensions are not supported and wont find any resource",
			tester.getLastResponse());
	}

	/**
	 * Toggle submit button to disabled state.
	 */
	@Test
	public void toggleButtonEnabledState()
	{
		tester.startPage(MockFormPage.class);
		Component submit = tester.getComponentFromLastRenderedPage("form:submit");
		assertTrue(submit.isEnabled());
		submit.setEnabled(false);
		assertFalse(submit.isEnabled());
	}

	/**
	 * Toggle submit button to enabled when text field validates.
	 */
	@Test
	public void toggleAjaxFormButton()
	{
		tester.startPage(new MockAjaxFormPage());
		Button submit = getSubmitButton();
		assertFalse(submit.isEnabled());
		FormTester form = tester.newFormTester("form");

		form.setValue("text", "XX");
		setTextFieldAndAssertSubmit(false);
		tester.clearFeedbackMessages();

		form.setValue("text", "XXXYYYXXX");
		setTextFieldAndAssertSubmit(true);

		form.setValue("text", "");
		setTextFieldAndAssertSubmit(false);
	}

	/**
	 *
	 */
	@Test
	public void cookieIsFoundWhenAddedToRequest()
	{
		tester.getRequest().addCookie(new Cookie("name", "value"));
		assertEquals("value", tester.getRequest().getCookie("name").getValue());
	}

	/**
	 *
	 */
	@Test
	public void cookieIsFoundWhenAddedToResponse()
	{
		tester.startPage(CreateBook.class);
		tester.getLastResponse().addCookie(new Cookie("name", "value"));
		Collection<Cookie> cookies = tester.getLastResponse().getCookies();
		assertEquals(cookies.iterator().next().getValue(), "value");
	}

	/**
	 *
	 */
	@Test
	public void cookieIsFoundOnNextRequestWhenAddedToResponse()
	{
		// Test that maxAge == -1 (Default) works properly
		tester.startPage(CreateBook.class);
		Cookie cookie = new Cookie("name", "value");
		tester.getRequest().addCookie(cookie);
		tester.startPage(CreateBook.class);
		assertEquals("value", tester.getLastRequest()
			.getCookiesAsList()
			.iterator()
			.next()
			.getValue(), "value");

		tester.startPage(CreateBook.class);
		cookie = new Cookie("name", "value");
		cookie.setMaxAge(60);
		tester.getLastResponse().addCookie(cookie);
		tester.startPage(CreateBook.class);
		assertEquals("value", tester.getLastRequest()
			.getCookiesAsList()
			.iterator()
			.next()
			.getValue(), "value");

		// Should copy persisted cookie from browser
		tester.startPage(CreateBook.class);
		assertEquals("value", tester.getLastRequest()
			.getCookiesAsList()
			.iterator()
			.next()
			.getValue(), "value");
	}

	/**
	 * Test for WICKET-3123
	 */
	@Test
	public void sessionBinding()
	{
		Session session = tester.getSession();
		assertTrue(session.isTemporary());
		session.bind();
		assertFalse(session.isTemporary());
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

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3543">WICKET-3543</a>
	 */
	@Test
	public void startResourceReference()
	{
		tester.startResourceReference(tester.getApplication()
			.getJavaScriptLibrarySettings()
			.getWicketAjaxReference());
		// verify that a random string from that resource is in the response
		tester.assertContains("getAjaxBaseUrl");
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3543">WICKET-3543</a>
	 */
	@Test
	public void startResource()
	{
		tester.startResource(tester.getApplication()
			.getJavaScriptLibrarySettings()
			.getWicketAjaxReference()
			.getResource());
		// verify that a random string from that resource is in the response
		tester.assertContains("getAjaxBaseUrl");
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3716">WICKET-3716</a>
	 * 
	 * @throws Exception
	 */
	@Test
	public void assertRenderedPageErrorMessage() throws Exception
	{
		tester.startPage(MockPageParametersAware.class);
		tester.assertRenderedPage(MockPageParametersAware.class);
		// assure false assertion
		Result result = tester.isRenderedPage(DummyPage.class);
		assertEquals(String.format("classes not the same, expected '%s', current '%s'",
			DummyPage.class, MockPageParametersAware.class), result.getMessage());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3913
	 */
	@Test
	public void startPanelGoesToAnotherPage()
	{
		tester.startComponentInPage(new MockPanelWithLink("testPanel")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onLinkClick(AjaxRequestTarget target)
			{
				setResponsePage(DummyHomePage.class);
			}
		});
		tester.clickLink("testPanel:link");

		tester.assertRenderedPage(DummyHomePage.class);
		tester.assertComponent("testPage", TestLink.class);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3940
	 */
	@Test
	public void rerenderComponentInPage()
	{
		tester.startComponentInPage(new Label("testLabel"));
		tester.startPage(tester.getLastRenderedPage());
	}

	/**
	 * Tests that setting a cookie with age > 0 before creating the page will survive after the
	 * rendering of the page and it will be used for the next request cycle.
	 */
	@Test
	public void transferCookies()
	{
		String cookieName = "wicket4289Name";
		String cookieValue = "wicket4289Value";
		int cookieAge = 1; // age > 0 => the cookie will be preserved for the the next request cycle

		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(cookieAge);
		tester.getRequest().addCookie(cookie);

		CookiePage page = new CookiePage(cookieName, cookieValue);

		tester.startPage(page);

		// assert that the cookie is in the next request
		List<Cookie> cookies = tester.getRequest().getCookiesAsList();
		assertEquals(1, cookies.size());
		Cookie cookie2 = cookies.get(0);
		assertEquals(cookieName, cookie2.getName());
		assertEquals(cookieValue, cookie2.getValue());
		assertEquals(cookieAge, cookie2.getMaxAge());

		// assert that the cookie will be preserved for the next request
		assertEquals(cookieValue, tester.getRequest().getCookie(cookieName).getValue());
	}

	/**
	 * Tests that setting a cookie with age == 0 will not be stored after the request cycle.
	 */
	@Test
	public void dontTransferCookiesWithNegativeAge()
	{
		String cookieName = "wicket4289Name";
		String cookieValue = "wicket4289Value";
		int cookieAge = 0; // age = 0 => do not store it

		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(cookieAge);
		tester.getRequest().addCookie(cookie);

		CookiePage page = new CookiePage(cookieName, cookieValue);

		tester.startPage(page);

		// assert that the cookie is not preserved for the next request cycle
		assertNull(tester.getRequest().getCookies());
	}

	/**
	 * Tests that setting a cookie with age < 0 will not be stored after the request cycle.
	 */
	@Test
	public void dontTransferCookiesWithZeroAge()
	{
		String cookieName = "wicket4289Name";
		String cookieValue = "wicket4289Value";
		int cookieAge = 0; // age == 0 => delete the cookie

		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(cookieAge);
		tester.getRequest().addCookie(cookie);

		CookiePage page = new CookiePage(cookieName, cookieValue);

		tester.startPage(page);

		// assert that the cookie is not preserved for the next request cycle
		assertNull(tester.getRequest().getCookies());
	}

	/**
	 * Tests if the access-denied-page is rendered if a page is rerendered for which you don't have
	 * permission anymore
	 */
	@Test
	public void rerenderNotAllowed()
	{
		tester.setExposeExceptions(false);
		class YesNoPageAuthorizationStrategy implements IAuthorizationStrategy
		{
			private boolean allowed = true;

			@Override
			public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
				Class<T> componentClass)
			{
				if (componentClass == AccessDeniedPage.class)
					return true;
				return allowed || !WebPage.class.isAssignableFrom(componentClass);
			}

			@Override
			public boolean isActionAuthorized(Component component, Action action)
			{
				if (component instanceof AccessDeniedPage)
					return true;
				return allowed || !(component instanceof WebPage);
			}
		}
		YesNoPageAuthorizationStrategy strategy = new YesNoPageAuthorizationStrategy();
		tester.getApplication().getSecuritySettings().setAuthorizationStrategy(strategy);
		DummyHomePage start = tester.startPage(DummyHomePage.class);
		tester.assertRenderedPage(DummyHomePage.class);
		strategy.allowed = false;
		tester.startPage(start);
		tester.assertRenderedPage(tester.getApplication()
			.getApplicationSettings()
			.getAccessDeniedPage());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4437
	 * 
	 * Clicking on ResourceLink should deliver the resource content
	 */
	@Test
	public void clickResourceLinkWithResource()
	{
		MockPageWithLink page = new MockPageWithLink();
		String content = "content";
		ByteArrayResource resource = new ByteArrayResource("text/plain", content.getBytes(),
			"fileName.txt");
		ResourceLink<Void> link = new ResourceLink<Void>(MockPageWithLink.LINK_ID, resource);
		page.add(link);
		tester.startPage(page);
		tester.clickLink(MockPageWithLink.LINK_ID, false);
		assertEquals(tester.getContentTypeFromResponseHeader(), "text/plain");
		assertEquals(content, tester.getLastResponseAsString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4810
	 * 
	 * Clicking on ResourceLink should deliver the resource reference's content
	 */
	@Test
	public void clickResourceLinkWithResourceReference()
	{
		MockPageWithLink page = new MockPageWithLink();
		String content = "content";
		final ByteArrayResource resource = new ByteArrayResource("text/plain", content.getBytes(),
			"fileName.txt");
		ResourceReference reference = new ResourceReference(WicketTesterTest.class,
			"resourceLinkWithResourceReferenceTest")
		{
			@Override
			public IResource getResource()
			{
				return resource;
			}
		};
		ResourceLink<Void> link = new ResourceLink<Void>(MockPageWithLink.LINK_ID, reference);
		page.add(link);
		tester.startPage(page);
		tester.clickLink(MockPageWithLink.LINK_ID, false);
		assertEquals(tester.getContentTypeFromResponseHeader(), "text/plain");
		assertEquals(content, tester.getLastResponseAsString());
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4507
	 * 
	 * When WicketTester#startComponentInPage() is used then #getLastResponseAsString() should
	 * return only the component's markup, without the autogenerated markup for the page
	 */
	@Test
	public void renderOnlyComponentsMarkup()
	{
		tester.startComponentInPage(new Label("label", "content")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.put("test", "123");
			}
		});

		assertEquals("<span wicket:id=\"label\" test=\"123\">content</span>",
			tester.getLastResponseAsString());
	}

	@Test
	public void catchExternalRedirect() throws Exception
	{
		class RedirectPage extends WebPage
		{
			@Override
			protected void onConfigure()
			{
				throw new RedirectToUrlException(
					"https://issues.apache.org/jira/browse/WICKET-4558");
			}
		}
		tester.startPage(new RedirectPage());
		tester.assertRedirectUrl("https://issues.apache.org/jira/browse/WICKET-4558");
	}

	@Test
	public void catchExternalRedirectFailure() throws Exception
	{
		class RedirectPage extends WebPage
		{
			@Override
			protected void onConfigure()
			{
				throw new RedirectToUrlException("http://some.url/");
			}
		}
		tester.startPage(new RedirectPage());
		boolean caught;
		try
		{
			tester.assertRedirectUrl("http://this.did.not.happen");
			caught = false;
		}
		catch (AssertionFailedError e)
		{
			caught = true;
		}
		assertTrue(caught);
	}

	@Test
	public void dontCatchInternalRedirect() throws Exception
	{
		class RedirectPage extends WebPage
		{
			@Override
			protected void onConfigure()
			{
				throw new RedirectToUrlException("wicket/bookmarkable/" +
					CreateBook.class.getName());
			}
		}
		tester.startPage(new RedirectPage());
		tester.assertRenderedPage(CreateBook.class);
	}

	public static class AlwaysRedirectPage extends WebPage
	{
		public AlwaysRedirectPage()
		{
			// redirects to another web server on the same computer
			throw new RedirectToUrlException("http://localhost:4333/");
		}
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4610
	 */
	@Test
	public void redirectToAbsoluteUrlTest()
	{
		tester.setFollowRedirects(false);
		tester.startPage(AlwaysRedirectPage.class);
		tester.assertRedirectUrl("http://localhost:4333/");
		assertEquals(HttpServletResponse.SC_FOUND, tester.getLastResponse().getStatus());
	}
}
