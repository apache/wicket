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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.ExactLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ValidationErrorFeedback;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Objects;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to ease unit testing of Wicket applications without the need for a servlet
 * container. To start a test, either use <code>startPage</code> or <code>startPanel</code>:
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
 * 	// start and render the test page
 * 	tester.startPage(MyPage.class);
 * 	// assert rendered page class
 * 	tester.assertRenderedPage(MyPage.class);
 * 	// assert rendered label component
 * 	tester.assertLabel(&quot;myMessage&quot;, &quot;Hello!&quot;);
 * }
 * </pre>
 *
 * The above example is straight forward: start <code>MyPage.class</code> and assert
 * <code>Label</code> it rendered. Next, we try to navigate through a <code>Link</code>:
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
 * // test code
 * public void testLinkToYourPage()
 * {
 * 	tester.startPage(MyPage.class);
 * 	// click link and render
 * 	tester.clickLink(&quot;toYourPage&quot;);
 * 	tester.assertRenderedPage(YourPage.class);
 * 	tester.assertLabel(&quot;yourMessage&quot;, &quot;Hi!&quot;);
 * }
 * </pre>
 *
 * <code>tester.clickLink(path);</code> will simulate user click on the component (in this case,
 * it's a <code>Link</code>) and render the response page <code>YourPage</code>. Ok, unit test of
 * <code>MyPage</code> is completed. Now we test <code>YourPage</code> standalone:
 *
 * <pre>
 * // test code
 * public void testRenderYourPage()
 * {
 * 	// provide page instance source for WicketTester
 * 	tester.startPage(new YourPage(&quot;mock message&quot;));
 * 	tester.assertRenderedPage(YourPage.class);
 * 	tester.assertLabel(&quot;yourMessage&quot;, &quot;mock message&quot;);
 * 	// assert feedback messages in INFO Level
 * 	tester.assertInfoMessages(new String[] { &quot;Wicket Rocks ;-)&quot; });
 * }
 * </pre>
 *
 * Many methods require a 'path' parameter. E.g. the page relative path can be obtained via
 * {@link Component#getPageRelativePath()}. Since each Component has an ID/name, any Component can
 * also be referenced by its ID {@link MarkupContainer#get(String)}. And since MarkupContainer's and
 * its subclasses are containers which allow to add Components (in sync with the markup hierarchy),
 * you may not only access direct childs but also subchilds like get("myPanel:myForm:myNameField")
 * separating each ID with a ':'.
 *
 * Cookie handling:
 *
 * There are some expectations about wicket tester cookie handling which should match as best as it
 * can be with a real client server request response cycle: - all valid cookies set before a request
 * is made (tester.getRequest().addCookie()) should appear in the page request - all cookies set in
 * the response should appear in the last response (tester.getLastResponse()) after the request is
 * made (expired cookies and others) - all cookies set in the response should appear even after a
 * redirect response is made until the final response (tester.getLastResponse()) is written to the
 * client (wicket tester) - all valid cookies (maxAge!=0) from the last response should be added to
 * the next request cookies (tester.getRequest().getCookies())
 *
 *
 * TODO General: Example usage of FormTester
 *
 * @author Ingram Chen
 * @author Juergen Donnerstag
 * @author Frank Bille
 * @since 1.2.6
 */
public class WicketTester extends BaseWicketTester
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(WicketTester.class);

	/**
	 * Creates a <code>WicketTester</code> and automatically creates a <code>WebApplication</code>,
	 * but the tester will have no home page.
	 */
	public WicketTester()
	{
	}

	/**
	 * Creates a <code>WicketTester</code> and automatically creates a <code>WebApplication</code>.
	 *
	 * @param homePage
	 *            a home page <code>Class</code>
	 */
	public WicketTester(final Class<? extends Page> homePage)
	{
		super(homePage);
	}

	/**
	 * Creates a <code>WicketTester</code>.
	 *
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 */
	public WicketTester(final WebApplication application)
	{
		super(application);
	}

	/**
	 * Creates a <code>WicketTester</code> to help unit testing.
	 *
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param path
	 *            the absolute path on disk to the web application's contents (e.g. war root) - may
	 *            be <code>null</code>
	 *
	 * @see org.apache.wicket.mock.MockApplication#MockApplication()
	 */
	public WicketTester(final WebApplication application, final String path)
	{
		super(application, path);
	}

	/**
	 * Creates a <code>WicketTester</code> to help unit testing.
	 *
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param servletCtx
	 *            the servlet context used as backend
	 */
	public WicketTester(WebApplication application, ServletContext servletCtx)
	{
		super(application, servletCtx);
	}

	/**
	 * Creates a <code>WicketTester</code> to help unit testing.
	 *
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param init
	 *            force the application to be initialized (default = true)
	 */
	public WicketTester(WebApplication application, boolean init)
	{
		super(application, init);
	}

	/**
	 * Creates a <code>WicketTester</code> to help unit testing.
	 *
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param servletCtx
	 *            the servlet context used as backend
	 * @param init
	 *            force the application to be initialized (default = true)
	 */
	public WicketTester(WebApplication application, ServletContext servletCtx, boolean init)
	{
		super(application, servletCtx, init);
	}

	/**
	 * Returns the current Maven build directory taken from the <tt>basedir</tt> system property, or
	 * null if not set
	 *
	 * @return path with a trailing slash
	 */
	public static String getBasedir()
	{
		String basedir = System.getProperty("basedir");
		if (basedir != null)
		{
			basedir = basedir + "/";
		}
		else
		{
			basedir = "";
		}
		return basedir;
	}

	/**
	 * Asserts that the Ajax location header is present.
	 */
	public void assertAjaxLocation()
	{
		if (null != getLastResponse().getHeader("Location"))
		{
			throw new AssertionFailedError(
				"Location header should *not* be present when using Ajax");
		}

		String ajaxLocation = getLastResponse().getHeader("Ajax-Location");
		if (null == ajaxLocation)
		{
			throw new AssertionFailedError(
				"Ajax-Location header should be present when using Ajax");
		}

		int statusCode = getLastResponse().getStatus();
		if (statusCode != 200)
		{
			throw new AssertionFailedError("Expected HTTP status code to be 200 (OK)");
		}
	}

	/**
	 * Asserts a <code>Component</code> class.
	 *
	 * @param path
	 *            path to <code>Component</code>
	 * @param expectedComponentClass
	 *            expected <code>Component</code> class
	 */
	public void assertComponent(String path, Class<? extends Component> expectedComponentClass)
	{
		assertResult(isComponent(path, expectedComponentClass));
	}

	/**
	 * Asserts that the <code>Component</code> a the given path has a behavior of the given type.
	 *
	 * @param path
	 *            path to <code>Component</code>
	 * @param expectedBehaviorClass
	 *            expected <code>Behavior</code> class
	 */
	public void assertBehavior(String path, Class<? extends Behavior> expectedBehaviorClass)
	{
		Args.notNull(expectedBehaviorClass, "expectedBehaviorClass");

		Component component = assertExists(path);
		List<? extends Behavior> behaviors = component.getBehaviors(expectedBehaviorClass);
		final String message = String.format("Component '%s' has no behaviors of type '%s'",
			component.getPageRelativePath(), expectedBehaviorClass);
		assertResult(new Result(CollectionUtils.isEmpty(behaviors), message));
	}

	/**
	 * Tests that a <code>Component</code> has been added to a <code>AjaxRequestTarget</code>, using
	 * {@link org.apache.wicket.ajax.AjaxRequestTarget#add(Component...)}. This method actually
	 * tests that a <code>Component</code> is on the Ajax response sent back to the client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using JavaScript. But it shouldn't be needed because you just have to trust that Wicket
	 * Ajax JavaScript works.
	 *
	 * @param component
	 *            a <code>Component</code> to be tested
	 */
	public void assertComponentOnAjaxResponse(Component component)
	{
		Result result = isComponentOnAjaxResponse(component);
		assertResult(result);
	}

	/**
	 * Tests that a <code>Component</code> has been added to a <code>AjaxRequestTarget</code>, using
	 * {@link org.apache.wicket.ajax.AjaxRequestTarget#add(Component...)}. This method actually
	 * tests that a <code>Component</code> is on the Ajax response sent back to the client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using JavaScript. But it shouldn't be needed because you just have to trust that Wicket
	 * Ajax JavaScript works.
	 *
	 * @param componentPath
	 *            a <code>Component</code> path to test
	 */
	public void assertComponentOnAjaxResponse(String componentPath)
	{
		Component component = getComponentFromLastRenderedPage(componentPath, false);
		assertComponentOnAjaxResponse(component);
	}

	/**
	 * Asserts the content of last rendered page contains (matches) a given regex pattern.
	 *
	 * @param pattern
	 *            a regex pattern to match
	 */
	public void assertContains(String pattern)
	{
		assertResult(ifContains(pattern));
	}

	/**
	 * The opposite of {@link #assertContains(String)}.
	 *
	 * @param pattern
	 *            pattern
	 */
	public void assertContainsNot(String pattern)
	{
		assertResult(ifContainsNot(pattern));
	}

	/**
	 * Asserts that a component's markup has loaded with the given variation
	 *
	 * @param component
	 *            The component which markup to check
	 * @param expectedVariation
	 *            The expected variation of the component's markup
	 */
	public void assertMarkupVariation(Component component, String expectedVariation)
	{
		Result result = Result.PASS;
		IMarkupFragment markup = getMarkupFragment(component);

		String actualVariation = markup.getMarkupResourceStream().getVariation();
		if (Objects.equal(expectedVariation, actualVariation) == false)
		{
			result = Result.fail(
				String.format("Wrong variation for component '%s'. Actual: '%s', expected: '%s'",
					component.getPageRelativePath(), actualVariation, expectedVariation));
		}

		assertResult(result);
	}

	/**
	 * Asserts that a component's markup has loaded with the given style.
	 *
	 * @param component
	 *            The component which markup to check
	 * @param expectedStyle
	 *            The expected style of the component's markup. For example: <em>green</em> in
	 *            <code>MyPanel_green.html</code>
	 */
	public void assertMarkupStyle(Component component, String expectedStyle)
	{
		Result result = Result.PASS;
		IMarkupFragment markup = getMarkupFragment(component);

		String actualStyle = markup.getMarkupResourceStream().getStyle();
		if (Objects.equal(expectedStyle, actualStyle) == false)
		{
			result = Result
				.fail(String.format("Wrong style for component '%s'. Actual: '%s', expected: '%s'",
					component.getPageRelativePath(), actualStyle, expectedStyle));
		}

		assertResult(result);
	}

	/**
	 * Asserts that a component's markup has loaded with the given locale
	 *
	 * @param component
	 *            The component which markup to check
	 * @param expectedLocale
	 *            The expected locale of the component's markup
	 */
	public void assertMarkupLocale(Component component, Locale expectedLocale)
	{
		Result result = Result.PASS;
		IMarkupFragment markup = getMarkupFragment(component);

		Locale actualLocale = markup.getMarkupResourceStream().getLocale();
		if (Objects.equal(expectedLocale, actualLocale) == false)
		{
			result = Result
				.fail(String.format("Wrong locale for component '%s'. Actual: '%s', expected: '%s'",
					component.getPageRelativePath(), actualLocale, expectedLocale));
		}

		assertResult(result);
	}

	private IMarkupFragment getMarkupFragment(Component component)
	{
		IMarkupFragment markup = null;
		if (component instanceof MarkupContainer)
		{
			markup = ((MarkupContainer)component).getAssociatedMarkup();
		}

		if (markup == null)
		{
			markup = component.getMarkup();
		}

		if (markup == null)
		{
			throw new AssertionFailedError(String.format("Cannot find the markup of component: %s",
				component.getPageRelativePath()));
		}

		return markup;
	}

	/**
	 * Asserts error-level feedback messages.
	 *
	 * @param expectedErrorMessages
	 *            expected error messages
	 */
	public void assertErrorMessages(Serializable... expectedErrorMessages)
	{
		assertFeedbackMessages(new ExactLevelFeedbackMessageFilter(FeedbackMessage.ERROR),
			expectedErrorMessages);
	}

	/**
	 * Assert info-level feedback messages.
	 *
	 * @param expectedInfoMessages
	 *            expected info messages
	 */
	public void assertInfoMessages(Serializable... expectedInfoMessages)
	{
		assertFeedbackMessages(new ExactLevelFeedbackMessageFilter(FeedbackMessage.INFO),
			expectedInfoMessages);
	}

	/**
	 * Assert there are feedback messages accepted by the provided filter.
	 *
	 * @param filter
	 *            the filter that will decide which messages to check
	 * @param expectedMessages
	 *            expected feedback messages
	 */
	public void assertFeedbackMessages(IFeedbackMessageFilter filter,
		Serializable... expectedMessages)
	{
		List<FeedbackMessage> feedbackMessages = getFeedbackMessages(filter);
		List<Serializable> actualMessages = getActualFeedbackMessages(feedbackMessages);
		WicketTesterHelper.assertEquals(Arrays.asList(expectedMessages), actualMessages);
	}

	/**
	 * Asserts that there is a feedback message provided by a given component
	 *
	 * @param component
	 *            the component that provided the expected feedback message. Optional.
	 * @param key
	 *            the resource key for the feedback message. Mandatory.
	 * @param model
	 *            the model used for interpolating the feedback message. Optional.
	 * @param filter
	 *            the filter that decides in which messages to look in. E.g. with a specific level,
	 *            rendered or not, etc.
	 */
	public void assertComponentFeedbackMessage(Component component, String key, IModel<?> model,
		IFeedbackMessageFilter filter)
	{
		Args.notNull(key, "key");

		String expectedMessage = getApplication().getResourceSettings().getLocalizer().getString(
			key, component, model);

		List<FeedbackMessage> feedbackMessages = getFeedbackMessages(filter);
		List<Serializable> actualMessages = getActualFeedbackMessages(feedbackMessages);

		assertTrue(actualMessages.contains(expectedMessage), String
			.format("Feedback message with key '%s' cannot be found in %s", key, actualMessages));
	}

	/**
	 * Extracts the actual messages from the passed feedback messages. Specially handles
	 * ValidationErrorFeedback messages by extracting their String message
	 *
	 * @param feedbackMessages
	 *            the feedback messages
	 * @return the FeedbackMessages' messages
	 */
	private List<Serializable> getActualFeedbackMessages(List<FeedbackMessage> feedbackMessages)
	{
		List<Serializable> actualMessages = new ArrayList<>();
		for (FeedbackMessage feedbackMessage : feedbackMessages)
		{
			Serializable message = feedbackMessage.getMessage();
			if (message instanceof ValidationErrorFeedback)
			{
				actualMessages.add(message.toString());
			}
			else
			{
				actualMessages.add(message);
			}
		}
		return actualMessages;
	}

	/**
	 * Assert that a particular feedback panel is rendering certain messages.
	 *
	 * NOTE: this casts the component at the specified path to a {@link FeedbackPanel}, so it will
	 * not work with custom {@link IFeedback} implementations unless you are subclassing
	 * {@link FeedbackPanel}
	 *
	 * @param path
	 *            path to the feedback panel
	 * @param messages
	 *            messages expected to be rendered
	 */
	public void assertFeedback(String path, Serializable... messages)
	{
		final FeedbackPanel fbp = (FeedbackPanel)getComponentFromLastRenderedPage(path);
		final IModel<List<FeedbackMessage>> model = fbp.getFeedbackMessagesModel();
		final List<FeedbackMessage> renderedMessages = model.getObject();
		if (renderedMessages == null)
		{
			throw new AssertionFailedError(
				String.format("feedback panel at path [%s] returned null messages", path));
		}
		if (messages.length != renderedMessages.size())
		{
			throw new AssertionFailedError(String.format(
				"you expected '%d' messages for the feedback panel [%s], but there were actually '%d'",
				messages.length, path, renderedMessages.size()));
		}
		for (int i = 0; i < messages.length && i < renderedMessages.size(); i++)
		{
			final Serializable expected = messages[i];
			boolean found = false;
			for (FeedbackMessage actual : renderedMessages)
			{
				if (Objects.equal(expected, actual.getMessage()))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				assertResult(Result.fail("Missing expected feedback message: " + expected));
			}
		}
	}

	/**
	 * Asserts that a <code>Component</code> is invisible.
	 *
	 * @param path
	 *            path to <code>Component</code>
	 */
	public void assertInvisible(String path)
	{
		assertResult(isInvisible(path));
	}

	/**
	 * Asserts the text of a <code>Label</code> <code>Component</code>.
	 *
	 * @param path
	 *            path to <code>Label</code> <code>Component</code>
	 * @param expectedLabelText
	 *            expected text of the <code>Label</code>
	 */
	public void assertLabel(String path, String expectedLabelText)
	{
		Label label = (Label)getComponentFromLastRenderedPage(path);
		assertEquals(expectedLabelText, label.getDefaultModelObjectAsString());
	}

	/**
	 * Asserts the model value of a component.
	 *
	 * @param path
	 *            path to the component on the page
	 * @param expectedValue
	 *            expected value of the component's model
	 */
	public void assertModelValue(String path, Object expectedValue)
	{
		Component component = getComponentFromLastRenderedPage(path);
		assertEquals(expectedValue, component.getDefaultModelObject());
	}

	/**
	 * Asserts the model of a {@link ListView}.
	 *
	 * @param path
	 *            path to a {@link ListView} <code>Component</code>
	 * @param expectedList
	 *            expected <code>List</code> in the model of the given {@link ListView}
	 * @Deprecated use {@link #assertComponent(String, Class) combined with
	 *             {@link #assertModelValue(String, Object)} instead
	 */
	@Deprecated
	@Override
	public void assertListView(String path, List<?> expectedList)
	{
		ListView<?> listView = (ListView<?>)getComponentFromLastRenderedPage(path);
		WicketTesterHelper.assertEquals(expectedList, listView.getList());
	}

	/**
	 * Asserts no error-level feedback messages.
	 */
	public void assertNoErrorMessage()
	{
		assertNoFeedbackMessage(FeedbackMessage.ERROR);
	}

	/**
	 * Asserts no info-level feedback messages.
	 */
	public void assertNoInfoMessage()
	{
		assertNoFeedbackMessage(FeedbackMessage.INFO);
	}

	/**
	 * Asserts there are no feedback messages with a certain level.
	 *
	 * @param level
	 *            the level to check for
	 */
	public void assertNoFeedbackMessage(int level)
	{
		Result result = hasNoFeedbackMessage(level);
		assertFalse(result.wasFailed(), result.getMessage());
	}

	/**
	 * Asserts a last-rendered <code>Page</code> class.
	 *
	 * @param expectedRenderedPageClass
	 *            expected class of last rendered <code>Page</code>
	 */
	public void assertRenderedPage(Class<? extends Page> expectedRenderedPageClass)
	{
		assertResult(isRenderedPage(expectedRenderedPageClass));
	}

	/**
	 * Asserts last-rendered <code>Page</code> against an expected HTML document.
	 * <p>
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 *
	 * @param clazz
	 *            <code>Class</code> used to load the file (relative to <code>clazz</code> package)
	 * @param filename
	 *            expected output filename <code>String</code>
	 * @throws Exception
	 */
	@Override
	public void assertResultPage(final Class<?> clazz, final String filename) throws Exception
	{
		String document = getLastResponseAsString();
		DiffUtil.validatePage(document, clazz, filename, true);
	}

	/**
	 * Asserts last-rendered <code>Page</code> against an expected HTML document as a
	 * <code>String</code>
	 *
	 * @param expectedDocument
	 *            expected output <code>String</code>
	 * @throws Exception
	 */
	public void assertResultPage(final String expectedDocument) throws Exception
	{
		// Validate the document
		String document = getLastResponseAsString();
		assertEquals(expectedDocument, document);
	}

	/**
	 * Asserts that a <code>Component</code> is visible.
	 *
	 * @param path
	 *            path to a <code>Component</code>
	 */
	public void assertVisible(String path)
	{
		assertResult(isVisible(path));
	}

	/**
	 * assert component is enabled.
	 *
	 * @param path
	 *            path to component
	 *
	 */
	public void assertEnabled(String path)
	{
		assertResult(isEnabled(path));
	}

	/**
	 * assert component is enabled.
	 *
	 * @param path
	 *            path to component
	 */
	public void assertDisabled(String path)
	{
		assertResult(isDisabled(path));
	}

	/**
	 * assert form component is required.
	 *
	 * @param path
	 *            path to form component
	 */
	public void assertRequired(String path)
	{
		assertResult(isRequired(path));
	}

	/**
	 * assert form component is required.
	 *
	 * @param path
	 *            path to form component
	 */
	public void assertNotRequired(String path)
	{
		assertResult(isNotRequired(path));
	}

	/**
	 *
	 * @param result
	 */
	private void assertResult(Result result)
	{
		if (result.wasFailed())
		{
			throw new AssertionFailedError(result.getMessage());
		}
	}

	/**
	 * Checks whether a component is visible and/or enabled before usage
	 *
	 * @param component
	 */
	public void assertUsability(final Component component)
	{
		checkUsability(component, true);
	}

	/**
	 *
	 * @param link
	 */
	public void clickLink(Component link)
	{
		clickLink(link.getPageRelativePath());
	}

	/**
	 * Asserts that that the BookmarkablePageLink<?> identified by "id" points to the page as
	 * expected - including parameters.
	 *
	 * @param id
	 * @param pageClass
	 * @param parameters
	 */
	public void assertBookmarkablePageLink(final String id,
		final Class<? extends WebPage> pageClass, final PageParameters parameters)
	{
		BookmarkablePageLink<?> pageLink;
		try
		{
			pageLink = (BookmarkablePageLink<?>)getComponentFromLastRenderedPage(id);
		}
		catch (ClassCastException e)
		{
			throw new IllegalArgumentException(
				"Component with id:" + id + " is not a BookmarkablePageLink");
		}

		assertEquals(pageClass, pageLink.getPageClass(),
			"BookmarkablePageLink: " + id + " is pointing to the wrong page");

		assertEquals(parameters, pageLink.getPageParameters(),
			"One or more of the parameters associated with the BookmarkablePageLink: " + id +
				" do not match");
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 *
	 * @param <T>
	 * @param testClass
	 * @param pageClass
	 * @param filename
	 * @throws Exception
	 */
	public <T extends Page> void executeTest(final Class<?> testClass, final Class<T> pageClass,
		final String filename) throws Exception
	{
		log.info("=== " + pageClass.getName() + " ===");

		startPage(pageClass);
		assertRenderedPage(pageClass);
		assertResultPage(testClass, filename);
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 *
	 * @param testClass
	 * @param page
	 * @param filename
	 * @throws Exception
	 */
	public void executeTest(final Class<?> testClass, final Page page, final String filename)
		throws Exception
	{
		log.info("=== " + page.getClass().getName() + " ===");

		startPage(page);
		assertRenderedPage(page.getClass());
		assertResultPage(testClass, filename);
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 *
	 * @param testClass
	 * @param component
	 * @param filename
	 * @throws Exception
	 */
	public void executeTest(final Class<?> testClass, final Component component,
		final String filename) throws Exception
	{
		log.info("=== " + component.getClass().getName() + " ===");

		startComponentInPage(component);
		assertResultPage(testClass, filename);
	}

	/**
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 *
	 * @param <T>
	 * @param testClass
	 * @param pageClass
	 * @param parameters
	 * @param filename
	 * @throws Exception
	 */
	public <T extends Page> void executeTest(final Class<?> testClass, final Class<T> pageClass,
		PageParameters parameters, final String filename) throws Exception
	{
		log.info("=== " + pageClass.getName() + " ===");

		startPage(pageClass, parameters);
		assertRenderedPage(pageClass);
		assertResultPage(testClass, filename);
	}

	/**
	 *
	 * @param testClass
	 * @param component
	 * @param filename
	 * @throws Exception
	 */
	public void executeListener(final Class<?> testClass, final Component component,
		final String filename) throws Exception
	{
		assertNotNull(component);

		log.info("=== " + testClass.getName() + " : " + component.getPageRelativePath() + " ===");

		executeListener(component);
		assertResultPage(testClass, filename);
	}

	/**
	 *
	 * @param testClass
	 * @param behavior
	 * @param filename
	 * @throws Exception
	 */
	public void executeBehavior(final Class<?> testClass, final AbstractAjaxBehavior behavior,
		final String filename) throws Exception
	{
		assertNotNull(behavior);

		log.info("=== " + testClass.getName() + " : " + behavior.toString() + " ===");

		executeBehavior(behavior);
		assertResultPage(testClass, filename);
	}

	/**
	 * Assert that the last request redirected to the given Url.
	 *
	 * @param expectedRedirectUrl
	 *            expected
	 */
	public void assertRedirectUrl(String expectedRedirectUrl)
	{
		String actualRedirectUrl = getLastResponse().getRedirectLocation();
		assertEquals(expectedRedirectUrl, actualRedirectUrl);
	}
}
