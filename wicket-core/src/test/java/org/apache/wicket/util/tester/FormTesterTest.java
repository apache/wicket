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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.MockFormFileUploadPage.MockDomainObjectFileUpload;
import org.apache.wicket.util.tester.MockFormPage.MockDomainObject;
import org.junit.Test;


/**
 * Test of FormTester.
 * 
 * @author frankbille
 */
public class FormTesterTest extends WicketTestCase
{

	/**
	 * Test that normal use of the formtester (no file uploads) works.
	 */
	@Test
	public void formTester()
	{
		tester.startPage(MockFormPage.class);
		MockFormPage page = (MockFormPage)tester.getLastRenderedPage();
		MockDomainObject domainObject = page.getDomainObject();

		assertNotNull(domainObject);
		assertNull(domainObject.getText());
		assertNull(domainObject.getTextarea());
		assertFalse(domainObject.isCheckbox());

		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("text", "Mock text value");
		formTester.setValue("textarea", "Mock textarea value");
		formTester.setValue("checkbox", "true");
		formTester.submit();

		assertNotNull(domainObject);
		assertNotNull(domainObject.getText());
		assertNotNull(domainObject.getTextarea());
		assertTrue(domainObject.isCheckbox());
	}

	/**	 */
	@Test
	public void checkboxValuesCanBeSelectedWithBoolean()
	{
		tester.startPage(MockFormPage.class);
		MockFormPage page = (MockFormPage)tester.getLastRenderedPage();
		MockDomainObject domainObject = page.getDomainObject();
		assertFalse(domainObject.isCheckbox());

		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("checkbox", true);
		formTester.submit();
		assertTrue(domainObject.isCheckbox());

		formTester = tester.newFormTester("form");
		formTester.setValue("checkbox", false);
		formTester.submit();
		assertFalse(domainObject.isCheckbox());
	}


	/**
	 * Test that the user can use
	 * {@link FormTester#setFile(String, org.apache.wicket.util.file.File, String)} to test that
	 * upload to a FileUploadField works.
	 */
	@Test
	public void addFile()
	{
		tester.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();
		MockDomainObjectFileUpload domainObject = page.getDomainObject();

		assertNull(page.getFileUpload());
		assertNotNull(domainObject);
		assertNull(domainObject.getText());


		FormTester formTester = tester.newFormTester("form");
		formTester.setFile("file", new File("pom.xml"), "text/xml");
		formTester.setValue("text", "Mock value");
		formTester.submit();


		assertNotNull(domainObject);
		assertNotNull(domainObject.getText());
		assertEquals("Mock value", domainObject.getText());

		FileUpload fileUpload = page.getFileUpload();
		assertNotNull(fileUpload);

		assertTrue("setFile failed, no upload content detected.", fileUpload.getBytes().length > 0);
		assertEquals("pom.xml", fileUpload.getClientFileName());
		assertEquals("text/xml", fileUpload.getContentType());
	}

	/**
	 * Test that the user can use
	 * {@link FormTester#setFile(String, org.apache.wicket.util.file.File, String)} to test that
	 * upload to a FileUploadField works.
	 */
	@Test
	public void addBinaryFile()
	{
		tester.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();
		MockDomainObjectFileUpload domainObject = page.getDomainObject();

		assertNull(page.getFileUpload());
		assertNotNull(domainObject);
		assertNull(domainObject.getText());


		FormTester formTester = tester.newFormTester("form");
		formTester.setFile("file", new File(getBasedir() +
			"src/test/java/org/apache/wicket/util/tester/bg.jpg"), "image/jpeg");
		formTester.setValue("text", "Mock value");
		formTester.submit();


		assertNotNull(domainObject);
		assertNotNull(domainObject.getText());
		assertEquals("Mock value", domainObject.getText());

		FileUpload fileUpload = page.getFileUpload();
		assertNotNull(fileUpload);

		assertTrue(
			"uploaded content does not have the right size, expected 428, got " +
				fileUpload.getBytes().length, fileUpload.getBytes().length == 428);
		assertEquals("bg.jpg", fileUpload.getClientFileName());
		assertEquals("image/jpeg", fileUpload.getContentType());
	}

	/**
	 * Test that formTester deal with Multipart form correctly when no actual upload
	 */
	@Test
	public void submitWithoutUploadFile()
	{
		// tester.startPage(MockFormFileUploadPage.class, new PageParameters("required=true"));
		tester.startPage(MockFormFileUploadPage.class);
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();

		Session.get().setLocale(Locale.US);

		FormTester formTester = tester.newFormTester("form");

		tester.getRequest().setUseMultiPartContentType(true);
		// without file upload
		formTester.submit();
		assertNull(page.getFileUpload());

		tester.assertErrorMessages("'file' is required.");
	}

	/**
	 * Test that formTester deal with Multipart form correctly when no actual upload
	 */
	@Test
	public void submitMultipartForm()
	{
		tester.startPage(MockFormFileUploadPage.class, new PageParameters().set("required", false));
		MockFormFileUploadPage page = (MockFormFileUploadPage)tester.getLastRenderedPage();
		MockDomainObjectFileUpload domainObject = page.getDomainObject();

		Session.get().setLocale(Locale.US);

		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("text", "Mock Value");

		tester.getRequest().setUseMultiPartContentType(true);
		formTester.submit();

		assertFalse(formTester.getForm().hasError());
		assertNull(page.getFileUpload());
		assertEquals("Mock Value", domainObject.getText());
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void noParametersCreatedForDisabledComponents() throws Exception
	{
		tester.startPage(new MockFormPage()
		{
			private static final long serialVersionUID = -3023635650340910221L;

			@Override
			protected void onBeforeRender()
			{
				super.onBeforeRender();
				// on first rendering there can't be any form parameters.
				// on second rendering there must not be any since we disable the form.
				// the components all get rendered as disabled, so the browser would not send
				// any parameters. thus FormTester must not send any either.
				assertTrue(getRequest().getPostParameters().getParameterNames().isEmpty());
			}
		});
		final Component form = tester.getComponentFromLastRenderedPage("form");
		form.setEnabled(false);
		assertFalse(form.isEnabled());
		Component check = tester.getComponentFromLastRenderedPage("form:checkbox");
		assertTrue(check.isEnabled());
		assertFalse(check.isEnabledInHierarchy());
		FormTester formTester = tester.newFormTester("form");
		try
		{
			formTester.submit();
			fail("Executing the listener on disabled component is not allowed.");
		}
		catch (ListenerInvocationNotAllowedException expected)
		{
			// expected
		}
	}

	@Test
	public void wantOnChangeSelectionNotification()
	{
		class TestPage extends WebPage implements IMarkupResourceStreamProvider
		{
			private String selection;

			public TestPage()
			{
				Form<Object> form = new Form<Object>("form");
				add(form);
				List<String> choices = Arrays.asList(new String[] { "opt 1", "opt 2" });
				form.add(new DropDownChoice<String>("selector", Model.of(""), choices)
				{
					@Override
					protected boolean wantOnSelectionChangedNotifications()
					{
						return true;
					}

					@Override
					protected void onSelectionChanged(final String newSelection)
					{
						selection = newSelection;
					}
				});
			}

			@Override
			public IResourceStream getMarkupResourceStream(MarkupContainer container,
				Class<?> containerClass)
			{
				return new StringResourceStream(
					"<html><body><form wicket:id='form'><select wicket:id='selector'></select></form></body></html>");
			}
		}

		TestPage page = new TestPage();
		tester.startPage(page);

		final FormTester form = tester.newFormTester("form");
		form.select("selector", 0);

		assertEquals("opt 1", page.selection);
	}

	@Test
	public void testNestedFormHandlingOnInnerSubmit() throws Exception
	{
		NestedFormPage page = tester.startPage(NestedFormPage.class);
		FormTester form = tester.newFormTester("outer:inner");
		form.submit("submit");
		assertFalse("should not directly submit inner form - browsers submit the outer form!",
			page.url.contains("inner"));
		assertFalse("outer form should not be processed", page.outerSubmitted);
		assertTrue("inner form should be processed", page.innerSubmitted);
	}

	@Test
	public void testNestedFormHandlingOnInnerSubmitWithOuterForm() throws Exception
	{
		NestedFormPage page = tester.startPage(NestedFormPage.class);
		FormTester form = tester.newFormTester("outer");
		form.submit("inner:submit");
		assertFalse("should not directly submit inner form - browsers submit the outer form!",
			page.url.contains("inner"));
		assertFalse("outer form should not be processed", page.outerSubmitted);
		assertTrue("inner form should be processed", page.innerSubmitted);
	}

	@Test
	public void testNestedFormHandlingOnOuterSubmit() throws Exception
	{
		NestedFormPage page = tester.startPage(NestedFormPage.class);
		FormTester form = tester.newFormTester("outer");
		form.submit();
		assertFalse("should not directly submit inner form - browsers submit the outer form!",
			page.url.contains("inner"));
		assertTrue("outer form should be processed", page.outerSubmitted);
		assertTrue("inner form should be processed", page.innerSubmitted);
	}

}
