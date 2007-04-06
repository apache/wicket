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
package wicket.util.tester;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormSubmitBehavior;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.AjaxLink;
import wicket.ajax.markup.html.form.AjaxSubmitLink;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.behavior.IBehavior;
import wicket.feedback.FeedbackMessage;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedbackMessageFilter;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.CheckGroup;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.protocol.http.MockWebApplication;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebRequestCycle;
import wicket.util.diff.DiffUtil;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;

/**
 * A helper to ease unit testing of Wicket applications without the need for a
 * servlet container. See javadoc of WicketTester for example usage. This class
 * can be used as is, but JUnit users should use derived class WicketTester.
 *
 * @see WicketTester
 *
 * @author Ingram Chen
 * @author Juergen Donnerstag
 * @author Frank Bille
 */
public class BaseWicketTester extends MockWebApplication
{
	/** log. */
	private static final Log log = LogFactory.getLog(BaseWicketTester.class);

	/**
	 * @author frankbille
	 */
	public static class DummyWebApplication extends WebApplication
	{
		public Class getHomePage()
		{
			return DummyHomePage.class;
		}
	}

	/**
	 * Create WicketTester and automatically create a WebApplication, but the
	 * tester will have no home page.
	 */
	public BaseWicketTester()
	{
		this(new DummyWebApplication(), null);
	}

	/**
	 * Create WicketTester and automatically create a WebApplication.
	 *
	 * @param homePage
	 */
	public BaseWicketTester(final Class homePage)
	{
		this(new WebApplication()
		{
			/**
			 * @see wicket.Application#getHomePage()
			 */
			public Class getHomePage()
			{
				return homePage;
			}
		}, null);
	}

	/**
	 * Create WicketTester
	 *
	 * @param application
	 *            The wicket tester object
	 */
	public BaseWicketTester(final WebApplication application)
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
	 * @see wicket.protocol.http.MockWebApplication#MockWebApplication(String)
	 */
	public BaseWicketTester(final WebApplication application, final String path)
	{
		super(application, path);
	}

	/**
	 * Render a page defined in <code>TestPageSource</code>. This is usually
	 * used when a page does not have default constructor. For example, a
	 * <code>ViewBook</code> page requires a <code>Book</code> instance:
	 *
	 * <pre>
	 * tester.startPage(new TestPageSource()
	 * {
	 * 	public Page getTestPage()
	 * 	{
	 * 		Book mockBook = new Book(&quot;myBookName&quot;);
	 * 		return new ViewBook(mockBook);
	 * 	}
	 * });
	 * </pre>
	 *
	 * @param testPageSource
	 *            a page factory that creating test page instance
	 * @return Page rendered page
	 */
	public final Page startPage(final ITestPageSource testPageSource)
	{
		startPage(DummyHomePage.class);
		DummyHomePage page = (DummyHomePage)getLastRenderedPage();
		page.setTestPageSource(testPageSource);

		executeListener(page.getTestPageLink());
		return getLastRenderedPage();
	}

	/**
	 * Builds and processes a request suitable for invoking a listener. The
	 * component must implement any of the known *Listener interfaces.
	 *
	 * @param component the listener to invoke
	 */
	public void executeListener(Component component)
	{
		setupRequestAndResponse();
		getServletRequest().setRequestToComponent(component);
		processRequestCycle();
	}

	/**
	 * Builds and processes a request suitable for executing an ajax behavior.
	 *
	 * @param behavior the ajax behavior to execute
	 */
	public void executeBehavior(final AbstractAjaxBehavior behavior)
	{
		setupRequestAndResponse();
		WebRequestCycle cycle = createRequestCycle();
		getServletRequest().setRequestToRedirectString(
				behavior.getCallbackUrl(false, false).toString());
		processRequestCycle(cycle);
	}

	/**
	 * Render the page
	 *
	 * @param page
	 * @return The page rendered
	 */
	public final Page startPage(final Page page)
	{
		processRequestCycle(page);

		Page last = getLastRenderedPage();
		//
		// createRequestCycle();
		// getWicketSession().touch(page);
		// if (page != last)
		// {
		// getWicketSession().touch(last);
		// }
		return last;
	}

	/**
	 * Render a page from its default constructor.
	 *
	 * @param pageClass
	 *            a test page class with default constructor
	 * @return Page Rendered Page
	 */
	public final Page startPage(Class pageClass)
	{
		processRequestCycle(pageClass);
		return getLastRenderedPage();
	}

	/**
	 * Render a panel defined in <code>TestPanelSource</code>. The usage is
	 * similar with {@link #startPage(ITestPageSource)}. Please note that
	 * testing panel must use supplied <code>panelId<code> as component id.
	 *
	 * <pre>
	 * tester.startPanel(new TestPanelSource()
	 * {
	 * 	public Panel getTestPanel(String panelId)
	 * 	{
	 * 		MyData mockMyData = new MyData();
	 * 		return new MyPanel(panelId, mockMyData);
	 * 	}
	 * });
	 * </pre>
	 *
	 * @param testPanelSource
	 *            a panel factory that creating test panel instance
	 * @return Panel rendered panel
	 */
	public final Panel startPanel(final TestPanelSource testPanelSource)
	{
		return (Panel)startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return new DummyPanelPage(testPanelSource);
			}
		}).get(DummyPanelPage.TEST_PANEL_ID);
	}

	/**
	 * Render a panel from <code>Panel(String id)</code> constructor.
	 *
	 * @param panelClass
	 *            a test panel class with <code>Panel(String id)</code>
	 *            constructor
	 * @return Panel rendered panel
	 */
	public final Panel startPanel(final Class panelClass)
	{
		return (Panel)startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return new DummyPanelPage(new TestPanelSource()
				{
					private static final long serialVersionUID = 1L;

					public Panel getTestPanel(String panelId)
					{
						try
						{
							Constructor c = panelClass.getConstructor(new Class[] { String.class });
							return (Panel)c.newInstance(new Object[] { panelId });
						}
						catch (SecurityException e)
						{
							throw convertoUnexpect(e);
						}
						catch (NoSuchMethodException e)
						{
							throw convertoUnexpect(e);
						}
						catch (InstantiationException e)
						{
							throw convertoUnexpect(e);
						}
						catch (IllegalAccessException e)
						{
							throw convertoUnexpect(e);
						}
						catch (InvocationTargetException e)
						{
							throw convertoUnexpect(e);
						}
					}
				});
			}
		}).get(DummyPanelPage.TEST_PANEL_ID);
	}

	/**
	 * Throw "standard" WicketRuntimeException
	 *
	 * @param e
	 * @return RuntimeException
	 */
	private RuntimeException convertoUnexpect(Exception e)
	{
		return new WicketRuntimeException("tester: unexpected", e);
	}

	/**
	 * Gets the component with the given path from last rendered page. This
	 * method fails in case the component couldn't be found, and it will return
	 * null if the component was found, but is not visible.
	 *
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 * @see wicket.MarkupContainer#get(String)
	 */
	public Component getComponentFromLastRenderedPage(String path)
	{
		final Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: "
					+ Classes.simpleName(getLastRenderedPage().getClass()));
			return component;
		}
		if (component.isVisibleInHierarchy())
		{
			return component;
		}
		return null;
	}

	/**
	 * assert the text of <code>Label</code> component.
	 *
	 * @param path
	 *            path to <code>Label</code> component
	 * @param expectedLabelText
	 *            expected label text
	 * @return
	 */
	public Result hasLabel(String path, String expectedLabelText)
	{
		Label label = (Label)getComponentFromLastRenderedPage(path);
		return isEqual(expectedLabelText, label.getModelObjectAsString());
	}

	/**
	 * assert <code>PageLink</code> link to page class.
	 *
	 * @param path
	 *            path to <code>PageLink</code> component
	 * @param expectedPageClass
	 *            expected page class to link
	 * @return
	 */
	public Result isPageLink(String path, Class expectedPageClass)
	{
		PageLink pageLink = (PageLink)getComponentFromLastRenderedPage(path);
		try
		{
			Field iPageLinkField = pageLink.getClass().getDeclaredField("pageLink");
			iPageLinkField.setAccessible(true);
			IPageLink iPageLink = (IPageLink)iPageLinkField.get(pageLink);
			return isEqual(expectedPageClass, iPageLink.getPageIdentity());
		}
		catch (SecurityException e)
		{
			throw convertoUnexpect(e);
		}
		catch (NoSuchFieldException e)
		{
			throw convertoUnexpect(e);
		}
		catch (IllegalAccessException e)
		{
			throw convertoUnexpect(e);
		}
	}

	/**
	 * assert component class
	 *
	 * @param path
	 *            path to component
	 * @param expectedComponentClass
	 *            expected component class
	 * @return
	 */
	public Result isComponent(String path, Class expectedComponentClass)
	{
		Component component = getComponentFromLastRenderedPage(path);
		return isTrue("component '" + Classes.simpleName(component.getClass())
				+ "' is not type:" + Classes.simpleName(expectedComponentClass),
				expectedComponentClass.isAssignableFrom(component.getClass()));
	}

	/**
	 * assert component visible.
	 *
	 * @param path
	 *            path to component
	 * @return
	 */
	public Result isVisible(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: "
					+ Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isTrue("component '" + path + "' is not visible", component.isVisible());
	}

	/**
	 * assert component invisible.
	 *
	 * @param path
	 *            path to component
	 * @return
	 */
	public Result isInvisible(String path)
	{
		return isNull("component '" + path + "' is visible",
				getComponentFromLastRenderedPage(path));
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 *
	 * @param pattern
	 *            reqex pattern to match
	 * @return
	 */
	public Result ifContains(String pattern)
	{
		return isTrue("pattern '" + pattern + "' not found", getServletResponse().getDocument()
				.matches("(?s).*" + pattern + ".*"));
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
	 * Click the {@link Link} in the last rendered Page.
	 * <p>
	 * Simulate that AJAX is enabled.
	 *
	 * @see WicketTester#clickLink(String, boolean)
	 * @param path
	 *            Click the <code>Link</code> in the last rendered Page.
	 */
	public void clickLink(String path)
	{
		clickLink(path, true);
	}

	/**
	 * Click the {@link Link} in the last rendered Page.
	 * <p>
	 * This method also works for {@link AjaxLink}, {@link AjaxFallbackLink}
	 * and {@link AjaxSubmitLink}.
	 * <p>
	 * On AjaxLinks and AjaxFallbackLinks the onClick method is invoked with a
	 * valid AjaxRequestTarget. In that way you can test the flow of your
	 * application when using AJAX.
	 * <p>
	 * When clicking an AjaxSubmitLink the form, which the AjaxSubmitLink is
	 * attached to is first submitted, and then the onSubmit method on
	 * AjaxSubmitLink is invoked. If you have changed some values in the form
	 * during your test, these will also be submitted. This should not be used
	 * as a replacement for the {@link FormTester} to test your forms. It should
	 * be used to test that the code in your onSubmit method in AjaxSubmitLink
	 * actually works.
	 * <p>
	 * This method is also able to simulate that AJAX (javascript) is disabled
	 * on the client. This is done by setting the isAjax parameter to false. If
	 * you have an AjaxFallbackLink you can then check that it doesn't fail when
	 * invoked as a normal link.
	 *
	 * @param path
	 *            path to <code>Link</code> component
	 * @param isAjax
	 *            Whether to simulate that AJAX (javascript) is enabled or not.
	 *            If it's false then AjaxLink and AjaxSubmitLink will fail,
	 *            since it wouldn't work in real life. AjaxFallbackLink will be
	 *            invoked with null as the AjaxRequestTarget parameter.
	 */
	public void clickLink(String path, boolean isAjax)
	{
		Component linkComponent = getComponentFromLastRenderedPage(path);

		// if the link is an AjaxLink, we process it differently
		// than a normal link
		if (linkComponent instanceof AjaxLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + "is an AjaxLink and will "
						+ "not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxLink link = (AjaxLink)linkComponent;

			setupRequestAndResponse();
			RequestCycle requestCycle = createRequestCycle();
			AjaxRequestTarget target = new AjaxRequestTarget();
			requestCycle.setRequestTarget(target);

			link.onClick(target);

			// process the request target
			target.respond(requestCycle);
		}
		// AjaxFallbackLinks is processed like an AjaxLink if isAjax is true
		// If it's not handling of the linkComponent is passed through to the
		// Link.
		else if (linkComponent instanceof AjaxFallbackLink && isAjax)
		{
			AjaxFallbackLink link = (AjaxFallbackLink)linkComponent;

			setupRequestAndResponse();
			RequestCycle requestCycle = createRequestCycle();
			AjaxRequestTarget target = new AjaxRequestTarget();
			requestCycle.setRequestTarget(target);

			link.onClick(target);

			// process the request target
			target.respond(requestCycle);
		}
		// if the link is an AjaxSubmitLink, we need to find the form
		// from it using reflection so we know what to submit.
		else if (linkComponent instanceof AjaxSubmitLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + "is an AjaxSubmitLink and "
						+ "will not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxSubmitLink link = (AjaxSubmitLink)linkComponent;

			// We cycle through the attached behaviors and select the
			// LAST matching behavior as the one we handle.
			List behaviors = link.getBehaviors();
			AjaxFormSubmitBehavior ajaxFormSubmitBehavior = null;
			for (Iterator iter = behaviors.iterator(); iter.hasNext();)
			{
				Object behavior = iter.next();
				if (behavior instanceof AjaxFormSubmitBehavior)
				{
					AjaxFormSubmitBehavior submitBehavior = (AjaxFormSubmitBehavior)behavior;
					ajaxFormSubmitBehavior = submitBehavior;
				}
			}

			String failMessage = "No form submit behavior found on the submit link. Strange!!";
			notNull(failMessage, ajaxFormSubmitBehavior);

			setupRequestAndResponse();
			RequestCycle requestCycle = createRequestCycle();

			submitAjaxFormSubmitBehavior(ajaxFormSubmitBehavior);

			// Ok, finally we "click" the link
			ajaxFormSubmitBehavior.onRequest();

			// process the request target
			requestCycle.getRequestTarget().respond(requestCycle);
		}
		// if the link is a normal link (or ResourceLink)
		else if (linkComponent instanceof Link)
		{
			Link link = (Link)linkComponent;

			/*
			 * If the link is a bookmarkable link, then we need to transfer the
			 * parameters to the next request.
			 */
			if (link instanceof BookmarkablePageLink)
			{
				BookmarkablePageLink bookmarkablePageLink = (BookmarkablePageLink)link;
				try
				{
					Field parametersField = BookmarkablePageLink.class
					.getDeclaredField("parameters");
					parametersField.setAccessible(true);
					PageParameters parameters = (PageParameters)parametersField
					.get(bookmarkablePageLink);
					setParametersForNextRequest(parameters);
				}
				catch (Exception e)
				{
					fail("Internal error in WicketTester. "
							+ "Please report this in Wickets Issue Tracker.");
				}

			}

			executeListener(link);
		}
		else
		{
			fail("Link " + path
					+ " is not a Link, AjaxLink, AjaxFallbackLink or AjaxSubmitLink");
		}
	}

	/**
	 * submit the <code>Form</code> in the last rendered Page.
	 *
	 * @param path
	 *            path to <code>Form</code> component
	 */
	public void submitForm(String path)
	{
		Form form = (Form)getComponentFromLastRenderedPage(path);
		executeListener(form);
	}

	/**
	 * Sets a parameter for the component with the given path to be used with
	 * the next request. NOTE: this method only works when a page was rendered
	 * first.
	 *
	 * @param componentPath
	 *            path of the component
	 * @param value
	 *            the parameter value to set
	 */
	public void setParameterForNextRequest(String componentPath, Object value)
	{
		if (getLastRenderedPage() == null)
		{
			fail("before using this method, at least one page has to be rendered");
		}

		Component c = getComponentFromLastRenderedPage(componentPath);
		if (c == null)
		{
			fail("component " + componentPath + " was not found");
			return;
		}

		if (c instanceof FormComponent)
		{
			getParametersForNextRequest().put(((FormComponent)c).getInputName(), value);
		}
		else
		{
			getParametersForNextRequest().put(c.getPath(), value);
		}

	}

	/**
	 * assert last rendered Page class
	 *
	 * FIXME explain why the code is so complicated to compare two classes, or simplify
	 *
	 * @param expectedRenderedPageClass
	 *            expected class of last renered page
	 * @return
	 */
	public Result isRenderedPage(Class expectedRenderedPageClass)
	{
		if (!getLastRenderedPage().getClass().isAssignableFrom(expectedRenderedPageClass))
		{
			return isEqual(Classes.simpleName(expectedRenderedPageClass), Classes
					.simpleName(getLastRenderedPage().getClass()));
		}
		return Result.pass();
	}

	/**
	 * assert last rendered Page against an expected HTML document
	 * <p>
	 * Use <code>-Dwicket.replace.expected.results=true</code> to
	 * automatically replace the expected output file.
	 * </p>
	 *
	 * @param pageClass
	 *            Used to load the file (relative to clazz package)
	 * @param filename
	 *            Expected output
	 * @throws Exception
	 */
	public void assertResultPage(final Class pageClass, final String filename) throws Exception
	{
		// Validate the document
		String document = getServletResponse().getDocument();
		DiffUtil.validatePage(document, pageClass, filename, true);
	}

	/**
	 * assert last rendered Page against an expected HTML document as a String
	 *
	 * @param expectedDocument
	 *            Expected output
	 * @return
	 * @throws Exception
	 */
	public Result isResultPage(final String expectedDocument) throws Exception
	{
		// Validate the document
		String document = getServletResponse().getDocument();
		return isTrue("expected rendered page equals", document.equals(expectedDocument));
	}

	/**
	 * assert no error feedback messages
	 * @return
	 */
	public Result hasNoErrorMessage()
	{
		List messages = getMessages(FeedbackMessage.ERROR);
		return isTrue("expect no error message, but contains\n"
				+ WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * assert no info feedback messages
	 * @return
	 */
	public Result hasNoInfoMessage()
	{
		List messages = getMessages(FeedbackMessage.INFO);
		return isTrue("expect no info message, but contains\n"
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
	 * get feedback messages
	 *
	 * @param level
	 *            level of feedback message, ex.
	 *            <code>FeedbackMessage.DEBUG or FeedbackMessage.INFO.. etc</code>
	 * @return List list of messages (in String)
	 * @see FeedbackMessage
	 */
	public List getMessages(final int level)
	{
		FeedbackMessages feedbackMessages = Session.get().getFeedbackMessages();
		List allMessages = feedbackMessages.messages(new IFeedbackMessageFilter()
		{
			private static final long serialVersionUID = 1L;

			public boolean accept(FeedbackMessage message)
			{
				return message.getLevel() == level;
			}
		});
		List actualMessages = new ArrayList();
		for (Iterator iter = allMessages.iterator(); iter.hasNext();)
		{
			actualMessages.add(((FeedbackMessage)iter.next()).getMessage());
		}
		return actualMessages;
	}

	/**
	 * dump the source of last rendered page
	 */
	public void dumpPage()
	{
		log.info(getServletResponse().getDocument());
	}

	/**
	 * dump component trees
	 */
	public void debugComponentTrees()
	{
		debugComponentTrees("");
	}


	/**
	 * Dump the component trees to log.
	 *
	 * @param filter
	 *            Show only the components, which path contains the
	 *            filterstring.
	 */
	public void debugComponentTrees(String filter)
	{
		log.info("debugging ----------------------------------------------");
		for (Iterator iter = WicketTesterHelper.getComponentData(getLastRenderedPage()).iterator(); iter
		.hasNext();)
		{
			WicketTesterHelper.ComponentData obj = (WicketTesterHelper.ComponentData)iter.next();
			if (obj.path.matches(".*" + filter + ".*"))
			{
				log.info("path\t" + obj.path + " \t" + obj.type + " \t[" + obj.value + "]");
			}
		}
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
	 * @return
	 */
	public Result isComponentOnAjaxResponse(Component component)
	{
		String failMessage = "A component which is null could not have been added to the AJAX response";
		notNull(failMessage, component);

		// Get the AJAX response
		String ajaxResponse = getServletResponse().getDocument();

		// Test that the previous response was actually a AJAX response
		failMessage = "The Previous response was not an AJAX response. "
			+ "You need to execute an AJAX event, using clickLink, before using this assert";
		boolean isAjaxResponse = ajaxResponse
		.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ajax-response>");
		Result result = isTrue(failMessage, isAjaxResponse);
		if(result.wasFailed()) return result;

		// See if the component has a markup id
		String markupId = component.getMarkupId();

		failMessage = "The component doesn't have a markup id, "
			+ "which means that it can't have been added to the AJAX response";
		result = isTrue(failMessage, !Strings.isEmpty(markupId));
		if(result.wasFailed()) return result;

		// Look for that the component is on the response, using the markup id
		boolean isComponentInAjaxResponse = ajaxResponse.matches(".*<component id=\"" + markupId
				+ "\" ?>.*");
		failMessage = "Component wasn't found in the AJAX response";
		return isTrue(failMessage, isComponentInAjaxResponse);
	}

	/**
	 * Simulate that an AJAX event has been fired.
	 *
	 * @see #executeAjaxEvent(Component, String)
	 *
	 * @since 1.2.3
	 * @param componentPath
	 *            The component path.
	 * @param event
	 *            The event which we simulate is fired. If the event is null,
	 *            the test will fail.
	 */
	public void executeAjaxEvent(String componentPath, String event)
	{
		Component component = getComponentFromLastRenderedPage(componentPath);
		executeAjaxEvent(component, event);
	}

	/**
	 * Simulate that an AJAX event has been fired. You add an AJAX event to a
	 * component by using:
	 *
	 * <pre>
	 *     ...
	 *     component.add(new AjaxEventBehavior(&quot;ondblclick&quot;) {
	 *         public void onEvent(AjaxRequestTarget) {}
	 *     });
	 *     ...
	 * </pre>
	 *
	 * You can then test that the code inside onEvent actually does what it's
	 * supposed to, using the WicketTester:
	 *
	 * <pre>
	 *     ...
	 *     tester.executeAjaxEvent(component, &quot;ondblclick&quot;);
	 *
	 *     // Test that the code inside onEvent is correct.
	 *     ...
	 * </pre>
	 *
	 * This also works with AjaxFormSubmitBehavior, where it will "submit" the
	 * form before executing the command.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the component in the
	 * client DOM tree, using javascript.
	 *
	 *
	 * @param component
	 *            The component which has the AjaxEventBehavior we wan't to
	 *            test. If the component is null, the test will fail.
	 * @param event
	 *            The event which we simulate is fired. If the event is null,
	 *            the test will fail.
	 */
	public void executeAjaxEvent(Component component, String event)
	{
		String failMessage = "Can't execute event on a component which is null.";
		notNull(failMessage, component);

		failMessage = "event must not be null";
		notNull(failMessage, event);

		// Run through all the behavior and select the LAST ADDED behavior which
		// matches the event parameter.
		AjaxEventBehavior ajaxEventBehavior = null;
		List behaviors = component.getBehaviors();
		for (Iterator iter = behaviors.iterator(); iter.hasNext();)
		{
			IBehavior behavior = (IBehavior)iter.next();

			// AjaxEventBehavior is the one to look for
			if (behavior instanceof AjaxEventBehavior)
			{
				AjaxEventBehavior tmp = (AjaxEventBehavior)behavior;

				if (event.equals(tmp.getEvent()))
				{
					ajaxEventBehavior = tmp;
				}
			}
		}

		// If there haven't been found any event behaviors on the component
		// which maches the parameters we fail.
		failMessage = "No AjaxEventBehavior found on component: " + component.getId()
		+ " which matches the event: " + event.toString();
		notNull(failMessage, ajaxEventBehavior);

		setupRequestAndResponse();
		RequestCycle requestCycle = createRequestCycle();

		// If the event is an FormSubmitBehavior then also "submit" the form
		if (ajaxEventBehavior instanceof AjaxFormSubmitBehavior)
		{
			AjaxFormSubmitBehavior ajaxFormSubmitBehavior = (AjaxFormSubmitBehavior)ajaxEventBehavior;
			submitAjaxFormSubmitBehavior(ajaxFormSubmitBehavior);
		}

		ajaxEventBehavior.onRequest();

		// process the request target
		requestCycle.getRequestTarget().respond(requestCycle);
	}

	/**
	 * Get a TagTester based on a wicket:id. If more components exists with the
	 * same wicket:id in the markup only the first one is returned.
	 *
	 * @param wicketId
	 *            The wicket:id to search for.
	 * @return The TagTester for the tag which has the given wicket:id.
	 */
	public TagTester getTagByWicketId(String wicketId)
	{
		return TagTester.createTagByAttribute(getServletResponse().getDocument(), "wicket:id",
				wicketId);
	}

	/**
	 * Get a TagTester based on an dom id. If more components exists with the
	 * same id in the markup only the first one is returned.
	 *
	 * @param id
	 *            The dom id to search for.
	 * @return The TagTester for the tag which has the given dom id.
	 */
	public TagTester getTagById(String id)
	{
		return TagTester.createTagByAttribute(getServletResponse().getDocument(), "id", id);
	}

	/**
	 * Helper method for all the places where an AjaxCall should submit an
	 * associated form.
	 *
	 * @param behavior
	 *            The AjaxFormSubmitBehavior with the form to "submit"
	 */
	private void submitAjaxFormSubmitBehavior(AjaxFormSubmitBehavior behavior)
	{
		// We need to get the form submitted, using reflection.
		// It needs to be "submitted".
		Form form = null;
		try
		{
			Field formField = AjaxFormSubmitBehavior.class.getDeclaredField("form");
			formField.setAccessible(true);
			form = (Form)formField.get(behavior);
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		String failMessage = "No form attached to the submitlink.";
		notNull(failMessage, form);

		form.visitFormComponents(new FormComponent.AbstractVisitor()
		{
			public void onFormComponent(FormComponent formComponent)
			{
				if (!(formComponent instanceof Button) && !(formComponent instanceof RadioGroup)
						&& !(formComponent instanceof CheckGroup))
				{
					String name = formComponent.getInputName();
					String value = formComponent.getValue();

					getServletRequest().setParameter(name, value);
				}
			}
		});
	}

	private Result isTrue(String message, boolean condition)
	{
		if (condition)
		{
			return Result.pass();
		}
		return Result.fail(message);
	}

	private Result isEqual(Object expected, Object actual)
	{
		if (expected == null && actual == null)
		{
			return Result.pass();
		}
		if (expected != null && expected.equals(actual))
		{
			return Result.pass();
		}
		String message = "expected:<" + expected + "> but was:<" + actual + ">";
		return Result.fail(message);
	}

	private void notNull(String message, Object object)
	{
		if (object == null) {
			fail(message);
		}
	}

	private Result isNull(String message, Object object)
	{
		if (object != null) {
			return Result.fail(message);
		}
		return Result.pass();
	}

	private void fail(String message)
	{
		throw new WicketRuntimeException(message);
	}

	private void fail(String message, Throwable t)
	{
		throw new WicketRuntimeException(message, t);
	}
}
