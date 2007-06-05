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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.protocol.http.UnitTestSettings;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.diff.DiffUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A helper to ease unit testing of Wicket applications without the need for a
 * servlet container. To start a test, either use startPage() or startPanel():
 * 
 * <pre>
 * // production page
 * public class MyPage extends WebPage
 * {
 * 	public MyPage()
 * 	{
 * 		add(new Label(&quot;myMessage&quot;, &quot;Hello!&quot;));
 * 		add(new Link(&quot;toYourPage&quot;)
 * 		{
 * 			public void onClick()
 * 			{
 * 				setResponsePage(new YourPage(&quot;Hi!&quot;));
 * 			}
 * 		});
 * 	}
 * }
 * </pre>
 * 
 * <pre>
 * // test code
 * private WicketTester tester;
 * 
 * public void setUp()
 * {
 * 	tester = new WicketTester();
 * }
 * 
 * public void testRenderMyPage()
 * {
 * 	//start and render the test page
 * 	tester.startPage(MyPage.class);
 * 	//assert rendered page class
 * 	tester.assertRenderedPage(MyPage.class);
 * 	//assert rendered label component
 * 	tester.assertLabel(&quot;myMessage&quot;, &quot;Hello!&quot;);
 * }
 * </pre>
 * 
 * Above example is straight forward: start MyPage.class and assert Label it
 * rendered. Next, we try to navigate through link:
 * 
 * <pre>
 * // production page
 * public class YourPage extends WebPage
 * {
 * 	public YourPage(String message)
 * 	{
 * 		add(new Label(&quot;yourMessage&quot;, message));
 * 		info(&quot;Wicket Rocks ;-)&quot;);
 * 	}
 * }
 * 
 * //test code
 * public void testLinkToYourPage()
 * {
 * 	tester.startPage(MyPage.class);
 * 	//click link and render
 * 	tester.clickLink(&quot;toYourPage&quot;);
 * 	tester.assertRenderedPage(YourPage.class);
 * 	tester.assertLabel(&quot;yourMessage&quot;, &quot;Hi!&quot;);
 * }
 * </pre>
 * 
 * <code>tester.clickLink(path);</code> will simulate user click on the
 * component (in this case, it's a <code>Link</code>) and render the response
 * page <code>YourPage</code>. Ok, unit test of <code>MyPage</code> is
 * completed. Now we test <code>YourPage</code> standalone:
 * 
 * <pre>
 * //test code
 * public void testRenderYourPage()
 * {
 * 	// provide page instance source for WicketTester
 * 	tester.startPage(new TestPageSource()
 * 	{
 * 		public Page getTestPage()
 * 		{
 * 			return new YourPage(&quot;mock message&quot;);
 * 		}
 * 	});
 * 	tester.assertRenderedPage(YourPage.class);
 * 	tester.assertLabel(&quot;yourMessage&quot;, &quot;mock message&quot;);
 * 	// assert feedback messages in INFO Level
 * 	tester.assertInfoMessages(new String[] { &quot;Wicket Rocks ;-)&quot; });
 * }
 * </pre>
 * 
 * Instead of <code>tester.startPage(pageClass)</code>, we define a
 * {@link org.apache.wicket.util.tester.ITestPageSource} to provide testing page
 * instance for WicketTester. This is necessary because <code>YourPage</code>
 * uses a custom constructor, which is very common for transferring model data,
 * but can not be instantiated by reflection. Finally, we use
 * <code>assertInfoMessages</code> to assert there is a feedback message
 * "Wicket Rocks ;-)" in INFO level.
 * 
 * TODO General: Example usage of FormTester
 * 
 * @author Ingram Chen
 * @author Juergen Donnerstag
 * @author Frank Bille
 */
public class WicketTester extends BaseWicketTester
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WicketTester.class);

	/**
	 */
	public static class DummyWebApplication extends WebApplication
	{
		/**
		 * @see org.apache.wicket.Application#getHomePage()
		 */
		public Class getHomePage()
		{
			return DummyHomePage.class;
		}

		/**
		 * @see org.apache.wicket.protocol.http.WebApplication#newWebResponse(javax.servlet.http.HttpServletResponse)
		 */
		protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
		{
			return new WebResponse(servletResponse);
		}
		
		protected void outputDevelopmentModeWarning()
		{
			// do nothing
		}
	}

	/**
	 * Create WicketTester and automatically create a WebApplication, but the
	 * tester will have no home page.
	 */
	public WicketTester()
	{
		this(new DummyWebApplication());
	}

	/**
	 * Create WicketTester and automatically create a WebApplication.
	 * 
	 * @param homePage
	 */
	public WicketTester(final Class homePage)
	{
		this(new WebApplication()
		{
			/**
			 * @see org.apache.wicket.Application#getHomePage()
			 */
			public Class getHomePage()
			{
				return homePage;
			}

			protected WebResponse newWebResponse(final HttpServletResponse servletResponse)
			{
				return new WebResponse(servletResponse);
			}
		});
	}

	/**
	 * Create WicketTester
	 * 
	 * @param application
	 *            The wicket tester object
	 */
	public WicketTester(final WebApplication application)
	{
		this(application, null);
	}

	/**
	 * Create WicketTester to help unit testing
	 * 
	 * @param application
	 *            The wicket tester object
	 * @param path
	 *            The absolute path on disk to the web application contents
	 *            (e.g. war root) - may be null
	 * 
	 * @see org.apache.wicket.protocol.http.MockWebApplication#MockWebApplication(String)
	 */
	public WicketTester(final WebApplication application, final String path)
	{
		super(application, path);

		// We need to turn this on for unit testing so that url encoding will be
		// done on sorted maps of parameters and they will string compare
		UnitTestSettings.setSortUrlParameters(true);
	}


	/**
	 * assert the text of <code>Label</code> component.
	 * 
	 * @param path
	 *            path to <code>Label</code> component
	 * @param expectedLabelText
	 *            expected label text
	 */
	public void assertLabel(String path, String expectedLabelText)
	{
		Label label = (Label)getComponentFromLastRenderedPage(path);
		Assert.assertEquals(expectedLabelText, label.getModelObjectAsString());
	}

	/**
	 * assert <code>PageLink</code> link to page class.
	 * 
	 * @param path
	 *            path to <code>PageLink</code> component
	 * @param expectedPageClass
	 *            expected page class to link
	 */
	public void assertPageLink(String path, Class expectedPageClass)
	{
		assertResult(isPageLink(path, expectedPageClass));
	}

	/**
	 * assert component class
	 * 
	 * @param path
	 *            path to component
	 * @param expectedComponentClass
	 *            expected component class
	 */
	public void assertComponent(String path, Class expectedComponentClass)
	{
		assertResult(isComponent(path, expectedComponentClass));
	}

	/**
	 * assert component visible.
	 * 
	 * @param path
	 *            path to component
	 */
	public void assertVisible(String path)
	{
		assertResult(isVisible(path));
	}

	/**
	 * assert component invisible.
	 * 
	 * @param path
	 *            path to component
	 */
	public void assertInvisible(String path)
	{
		assertResult(isInvisible(path));
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 * 
	 * @param pattern
	 *            reqex pattern to match
	 */
	public void assertContains(String pattern)
	{
		assertResult(ifContains(pattern));
	}

	/**
	 * assert the model of {@link ListView} use expectedList
	 * 
	 * @param path
	 *            path to {@link ListView} component
	 * @param expectedList
	 *            expected list in the model of {@link ListView}
	 */
	public void assertListView(String path, List expectedList)
	{
		ListView listView = (ListView)getComponentFromLastRenderedPage(path);
		WicketTesterHelper.assertEquals(expectedList, listView.getList());
	}

	/**
	 * create a {@link FormTester} for the form at path, and fill all child
	 * {@link org.apache.wicket.markup.html.form.FormComponent}s with blank
	 * String initially.
	 * 
	 * @param path
	 *            path to {@link Form} component
	 * @return FormTester A FormTester instance for testing form
	 * @see #newFormTester(String, boolean)
	 */
	public FormTester newFormTester(String path)
	{
		return newFormTester(path, true);
	}

	/**
	 * create a {@link FormTester} for the form at path.
	 * 
	 * @param path
	 *            path to {@link Form} component
	 * @param fillBlankString
	 *            specify whether fill all child
	 *            {@link org.apache.wicket.markup.html.form.FormComponent}s
	 *            with blankString initially.
	 * @return FormTester A FormTester instance for testing form
	 * @see FormTester
	 */
	public FormTester newFormTester(String path, boolean fillBlankString)
	{
		return new FormTester(path, (Form)getComponentFromLastRenderedPage(path), this,
				fillBlankString);
	}

	/**
	 * assert last rendered Page class
	 * 
	 * @param expectedRenderedPageClass
	 *            expected class of last renered page
	 */
	public void assertRenderedPage(Class expectedRenderedPageClass)
	{
		assertResult(isRenderedPage(expectedRenderedPageClass));
	}

	/**
	 * Assert last rendered Page against an expected HTML document
	 * <p>
	 * Use <code>-Dwicket.replace.expected.results=true</code> to
	 * automatically replace the expected output file.
	 * </p>
	 * 
	 * @param clazz
	 *            Used to load the file (relative to clazz package)
	 * @param filename
	 *            Expected output
	 * @throws Exception
	 */
	public void assertResultPage(final Class clazz, final String filename) throws Exception
	{
		String document = getServletResponse().getDocument();
		setupRequestAndResponse();
		DiffUtil.validatePage(document, clazz, filename, true);
	}

	/**
	 * assert last rendered Page against an expected HTML document as a String
	 * 
	 * @param expectedDocument
	 *            Expected output
	 * @throws Exception
	 */
	public void assertResultPage(final String expectedDocument) throws Exception
	{
		// Validate the document
		String document = getServletResponse().getDocument();
		Assert.assertTrue(document.equals(expectedDocument));
	}

	/**
	 * assert no error feedback messages
	 */
	public void assertNoErrorMessage()
	{
		List messages = getMessages(FeedbackMessage.ERROR);
		Assert.assertTrue("expect no error message, but contains\n"
				+ WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * assert no info feedback messages
	 */
	public void assertNoInfoMessage()
	{
		List messages = getMessages(FeedbackMessage.INFO);
		Assert.assertTrue("expect no info message, but contains\n"
				+ WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * assert error feedback messages
	 * 
	 * @param expectedErrorMessages
	 *            expected error messages
	 */
	public void assertErrorMessages(String[] expectedErrorMessages)
	{
		List actualMessages = getMessages(FeedbackMessage.ERROR);
		List msgs = new ArrayList();
		for (Iterator iterator = actualMessages.iterator(); iterator.hasNext();)
		{
			msgs.add(iterator.next().toString());
		}
		WicketTesterHelper.assertEquals(Arrays.asList(expectedErrorMessages), msgs);
	}

	/**
	 * assert info feedback message
	 * 
	 * @param expectedInfoMessages
	 *            expected info messages
	 */
	public void assertInfoMessages(String[] expectedInfoMessages)
	{
		List actualMessages = getMessages(FeedbackMessage.INFO);
		WicketTesterHelper.assertEquals(Arrays.asList(expectedInfoMessages), actualMessages);
	}
	
	/**
	 * Test that a component has been added to a AjaxRequestTarget, using
	 * {@link AjaxRequestTarget#addComponent(Component)}. This method actually
	 * tests that a component is on the AJAX response sent back to the client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the component in the
	 * client DOM tree, using javascript. But it shouldn't be needed because you
	 * have to trust that the Wicket Ajax Javascript just works.
	 * 
	 * @param componentPath
	 *            The component path to the component to test whether it's on
	 *            the response.
	 */
	public void assertComponentOnAjaxResponse(String componentPath)
	{
		assertComponentOnAjaxResponse(getComponentFromLastRenderedPage(componentPath));
	}

	/**
	 * Test that a component has been added to a AjaxRequestTarget, using
	 * {@link AjaxRequestTarget#addComponent(Component)}. This method actually
	 * tests that a component is on the AJAX response sent back to the client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the component in the
	 * client DOM tree, using javascript. But it shouldn't be needed because you
	 * have to trust that the Wicket Ajax Javascript just works.
	 * 
	 * @param component
	 *            The component to test whether it's on the response.
	 */
	public void assertComponentOnAjaxResponse(Component component)
	{
		Result result = isComponentOnAjaxResponse(component);
		assertResult(result);
	}

	private void assertResult(Result result)
	{
		if (result.wasFailed())
		{
			throw new AssertionFailedError(result.getMessage());
		}
	}

	/**
	 * Assert that the ajax location header is present
	 */
	public void assertAjaxLocation()
	{
		if (null != ((MockHttpServletResponse)getWicketResponse().getHttpServletResponse())
						.getRedirectLocation())
		{
			throw new AssertionFailedError("Location header should *not* be present when using Ajax");
		}
		
		String ajaxLocation = ((MockHttpServletResponse)getWicketResponse()
				.getHttpServletResponse()).getHeader("Ajax-Location");
		if (null == ajaxLocation)
		{
			throw new AssertionFailedError("Ajax-Location header should be present when using Ajax");
		}
		
		int statusCode = ((MockHttpServletResponse)getWicketResponse()
				.getHttpServletResponse()).getStatus();
		if (statusCode != 200)
		{
			throw new AssertionFailedError("Expected HTTP status code to be 200 (OK)");
		}
	}
}
