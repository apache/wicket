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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.Component.IVisitor;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.BehaviorsUtil;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.form.IFormVisitorParticipant;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PageLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.protocol.http.MockWebApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.diff.DiffUtil;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to ease unit testing of Wicket applications without the need for a servlet
 * container. See javadoc of <code>WicketTester</code> for example usage. This class can be used as
 * is, but JUnit users should use derived class <code>WicketTester</code>.
 * 
 * @see WicketTester
 * 
 * @author Ingram Chen
 * @author Juergen Donnerstag
 * @author Frank Bille
 * @since 1.2.6
 */
public class BaseWicketTester extends MockWebApplication
{
	/** log. */
	private static final Logger log = LoggerFactory.getLogger(BaseWicketTester.class);

	/**
	 * @author jcompagner
	 */
	private static final class TestPageSource implements ITestPageSource
	{
		private final Page page;

		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 * 
		 * @param page
		 */
		private TestPageSource(Page page)
		{
			this.page = page;
		}

		public Page getTestPage()
		{
			return page;
		}
	}

	/**
	 * @author frankbille
	 */
	public static class DummyWebApplication extends WebApplication
	{
		@Override
		public Class<? extends Page> getHomePage()
		{
			return DummyHomePage.class;
		}

		@Override
		protected void outputDevelopmentModeWarning()
		{
			// Do nothing.
		}

		@Override
		protected ISessionStore newSessionStore()
		{
			// Don't use a filestore, or we spawn lots of threads, which makes
			// things slow.
			return new HttpSessionStore(this);
		}
	}

	/**
	 * Creates <code>WicketTester</code> and automatically create a <code>WebApplication</code>, but
	 * the tester will have no home page.
	 */
	public BaseWicketTester()
	{
		this(new DummyWebApplication(), null);
	}

	/**
	 * Creates <code>WicketTester</code> and automatically creates a <code>WebApplication</code>.
	 * 
	 * @param <C>
	 * 
	 * @param homePage
	 *            a home page <code>Class</code>
	 */
	public <C extends Page> BaseWicketTester(final Class<C> homePage)
	{
		this(new WebApplication()
		{
			/**
			 * @see org.apache.wicket.Application#getHomePage()
			 */
			@Override
			public Class<? extends Page> getHomePage()
			{
				return homePage;
			}

			@Override
			protected void outputDevelopmentModeWarning()
			{
				// Do nothing.
			}

			@Override
			protected ISessionStore newSessionStore()
			{
				// Don't use a filestore, or we spawn lots of threads, which
				// makes things slow.
				return new HttpSessionStore(this);
			}

		}, null);
	}

	/**
	 * Creates a <code>WicketTester</code>.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 */
	public BaseWicketTester(final WebApplication application)
	{
		this(application, null);
	}

	/**
	 * Creates a <code>WicketTester</code> for unit testing.
	 * 
	 * @param application
	 *            a <code>WicketTester</code> <code>WebApplication</code> object
	 * @param path
	 *            the absolute path on disk to the <code>WebApplication</code>'s contents (e.g. war
	 *            root) - may be <code>null</code>
	 * 
	 * @see org.apache.wicket.protocol.http.MockWebApplication#MockWebApplication(org.apache.wicket.protocol.http.WebApplication,
	 *      String)
	 */
	public BaseWicketTester(final WebApplication application, final String path)
	{
		super(application, path);
	}

	/**
	 * Renders a <code>Page</code> defined in <code>TestPageSource</code>. This is usually used when
	 * a page does not have default constructor. For example, a <code>ViewBook</code> page requires
	 * a <code>Book</code> instance:
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
	 *            a <code>Page</code> factory that creates a test page instance
	 * @return the rendered Page
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
	 * Builds and processes a request suitable for invoking a listener. The <code>Component</code>
	 * must implement any of the known <code>IListener</code> interfaces.
	 * 
	 * @param component
	 *            the listener to invoke
	 */
	public void executeListener(Component component)
	{
		WebRequestCycle cycle = setupRequestAndResponse();
		getServletRequest().setRequestToComponent(component);
		processRequestCycle(cycle);
	}

	/**
	 * Builds and processes a request suitable for executing an <code>AbstractAjaxBehavior</code>.
	 * 
	 * @param behavior
	 *            an <code>AbstractAjaxBehavior</code> to execute
	 */
	public void executeBehavior(final AbstractAjaxBehavior behavior)
	{
		CharSequence url = behavior.getCallbackUrl(false);
		WebRequestCycle cycle = setupRequestAndResponse(true);
		getServletRequest().setRequestToRedirectString(url.toString());
		processRequestCycle(cycle);
	}

	/**
	 * Renders the <code>Page</code>.
	 * 
	 * @param page
	 *            a <code>Page</code> to render
	 * @return the rendered <code>Page</code>
	 */
	public final Page startPage(final Page page)
	{
		return startPage(new TestPageSource(page));
	}

	/**
	 * Renders a <code>Page</code> from its default constructor.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            a test <code>Page</code> class with default constructor
	 * @return the rendered <code>Page</code>
	 */
	public final <C extends Page> Page startPage(Class<C> pageClass)
	{
		processRequestCycle(pageClass);
		return getLastRenderedPage();
	}

	/**
	 * Renders a <code>Page</code> from its default constructor.
	 * 
	 * @param <C>
	 * 
	 * @param pageClass
	 *            a test <code>Page</code> class with default constructor
	 * @param parameters
	 *            the parameters to use for the class.
	 * @return the rendered <code>Page</code>
	 */
	public final <C extends Page> Page startPage(Class<C> pageClass, PageParameters parameters)
	{
		processRequestCycle(pageClass, parameters);
		return getLastRenderedPage();
	}

	/**
	 * Creates a {@link FormTester} for the <code>Form</code> at a given path, and fills all child
	 * {@link org.apache.wicket.markup.html.form.FormComponent}s with blank <code>String</code>s.
	 * 
	 * @param path
	 *            path to <code>FormComponent</code>
	 * @return a <code>FormTester</code> instance for testing the <code>Form</code>
	 * @see #newFormTester(String, boolean)
	 */
	public FormTester newFormTester(String path)
	{
		return newFormTester(path, true);
	}

	/**
	 * Creates a {@link FormTester} for the <code>Form</code> at a given path.
	 * 
	 * @param path
	 *            path to <code>FormComponent</code>
	 * @param fillBlankString
	 *            specifies whether to fill all child <code>FormComponent</code> s with blank
	 *            <code>String</code>s
	 * @return a <code>FormTester</code> instance for testing the <code>Form</code>
	 * @see FormTester
	 */
	public FormTester newFormTester(String path, boolean fillBlankString)
	{
		return new FormTester(path, (Form<?>)getComponentFromLastRenderedPage(path), this,
			fillBlankString);
	}

	/**
	 * Renders a <code>Panel</code> defined in <code>TestPanelSource</code>. The usage is similar to
	 * {@link #startPage(ITestPageSource)}. Please note that testing <code>Panel</code> must use the
	 * supplied <code>panelId<code> as a <code>Component</code> id.
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
	 *            a <code>Panel</code> factory that creates test <code>Panel</code> instances
	 * @return a rendered <code>Panel</code>
	 */
	public final Panel startPanel(final ITestPanelSource testPanelSource)
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
	 * Renders a <code>Panel</code> from a <code>Panel(String id)</code> constructor.
	 * 
	 * @param <C>
	 * 
	 * @param panelClass
	 *            a test <code>Panel</code> class with <code>Panel(String id)</code> constructor
	 * @return a rendered <code>Panel</code>
	 */
	public final <C extends Panel> Panel startPanel(final Class<C> panelClass)
	{
		return (Panel)startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return new DummyPanelPage(new ITestPanelSource()
				{
					private static final long serialVersionUID = 1L;

					public Panel getTestPanel(String panelId)
					{
						try
						{
							Constructor<? extends Panel> c = panelClass.getConstructor(String.class);
							return c.newInstance(panelId);
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
	 * A helper method for starting a component for a test without attaching it to a Page.
	 * 
	 * Components which are somehow dependent on the page structure can not be currently tested with
	 * this method.
	 * 
	 * Example:
	 * 
	 * UserDataView view = new UserDataView("view", new ListDataProvider(userList));
	 * tester.startComponent(view); assertEquals(4, view.size());
	 * 
	 * @param component
	 */
	public void startComponent(Component component)
	{
		if (component instanceof FormComponent)
		{
			((FormComponent<?>)component).processInput();
		}
		component.beforeRender();
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
	 * Gets the component with the given path from last rendered page. This method fails in case the
	 * component couldn't be found, and it will return null if the component was found, but is not
	 * visible.
	 * 
	 * @param path
	 *            Path to component
	 * @return The component at the path
	 * @see org.apache.wicket.MarkupContainer#get(String)
	 */
	public Component getComponentFromLastRenderedPage(String path)
	{
		final Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does not exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
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
	 * @return a <code>Result</code>
	 */
	public Result hasLabel(String path, String expectedLabelText)
	{
		Label label = (Label)getComponentFromLastRenderedPage(path);
		return isEqual(expectedLabelText, label.getDefaultModelObjectAsString());
	}

	/**
	 * assert <code>PageLink</code> link to page class.
	 * 
	 * @param <C>
	 * 
	 * @param path
	 *            path to <code>PageLink</code> component
	 * @param expectedPageClass
	 *            expected page class to link
	 * @return a <code>Result</code>
	 */
	public <C extends Page> Result isPageLink(String path, Class<C> expectedPageClass)
	{
		PageLink<?> pageLink = (PageLink<?>)getComponentFromLastRenderedPage(path);
		try
		{
			for (Class<?> type = pageLink.getClass(); type != PageLink.class.getSuperclass(); type = type.getSuperclass())
			{
				try
				{
					Field iPageLinkField = type.getDeclaredField("pageLink");
					iPageLinkField.setAccessible(true);
					IPageLink iPageLink = (IPageLink)iPageLinkField.get(pageLink);
					return isEqual(expectedPageClass, iPageLink.getPageIdentity());
				}

				catch (NoSuchFieldException e)
				{
					continue;
				}
			}
			throw new WicketRuntimeException(
				"Is this realy a PageLink? Cannot find 'pageLink' field");
		}
		catch (SecurityException e)
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
	 * @param <C>
	 * 
	 * @param path
	 *            path to component
	 * @param expectedComponentClass
	 *            expected component class
	 * @return a <code>Result</code>
	 */
	public <C extends Component> Result isComponent(String path, Class<C> expectedComponentClass)
	{
		Component component = getComponentFromLastRenderedPage(path);
		if (component == null)
		{
			return Result.fail("Component not found: " + path);
		}
		return isTrue("component '" + Classes.simpleName(component.getClass()) + "' is not type:" +
			Classes.simpleName(expectedComponentClass),
			expectedComponentClass.isAssignableFrom(component.getClass()));
	}

	/**
	 * assert component visible.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isVisible(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isTrue("component '" + path + "' is not visible", component.isVisibleInHierarchy());
	}

	/**
	 * assert component invisible.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isInvisible(String path)
	{
		return isNull("component '" + path + "' is visible", getComponentFromLastRenderedPage(path));
	}

	/**
	 * assert component enabled.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isEnabled(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isTrue("component '" + path + "' is disabled", component.isEnabledInHierarchy());
	}

	/**
	 * assert component disabled.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isDisabled(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}

		return isFalse("component '" + path + "' is enabled", component.isEnabledInHierarchy());
	}

	/**
	 * assert component required.
	 * 
	 * @param path
	 *            path to component
	 * @return a <code>Result</code>
	 */
	public Result isRequired(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			fail("path: '" + path + "' does no exist for page: " +
				Classes.simpleName(getLastRenderedPage().getClass()));
		}
		else if (component instanceof FormComponent == false)
		{
			fail("path: '" + path + "' is not a form component");
		}

		return isRequired((FormComponent<?>)component);
	}

	/**
	 * assert component required.
	 * 
	 * @param component
	 *            a form component
	 * @return a <code>Result</code>
	 */
	public Result isRequired(FormComponent<?> component)
	{
		return isTrue("component '" + component + "' is not required", component.isRequired());
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 * 
	 * @param pattern
	 *            reqex pattern to match
	 * @return a <code>Result</code>
	 */
	public Result ifContains(String pattern)
	{
		return isTrue("pattern '" + pattern + "' not found", getServletResponse().getDocument()
			.matches("(?s).*" + pattern + ".*"));
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 * 
	 * @param pattern
	 *            reqex pattern to match
	 * @return a <code>Result</code>
	 */
	public Result ifContainsNot(String pattern)
	{
		return isFalse("pattern '" + pattern + "' found", getServletResponse().getDocument()
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
	public void assertListView(String path, List<?> expectedList)
	{
		ListView<?> listView = (ListView<?>)getComponentFromLastRenderedPage(path);
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
	 * This method also works for {@link AjaxLink}, {@link AjaxFallbackLink} and
	 * {@link AjaxSubmitLink}.
	 * <p>
	 * On AjaxLinks and AjaxFallbackLinks the onClick method is invoked with a valid
	 * AjaxRequestTarget. In that way you can test the flow of your application when using AJAX.
	 * <p>
	 * When clicking an AjaxSubmitLink the form, which the AjaxSubmitLink is attached to is first
	 * submitted, and then the onSubmit method on AjaxSubmitLink is invoked. If you have changed
	 * some values in the form during your test, these will also be submitted. This should not be
	 * used as a replacement for the {@link FormTester} to test your forms. It should be used to
	 * test that the code in your onSubmit method in AjaxSubmitLink actually works.
	 * <p>
	 * This method is also able to simulate that AJAX (javascript) is disabled on the client. This
	 * is done by setting the isAjax parameter to false. If you have an AjaxFallbackLink you can
	 * then check that it doesn't fail when invoked as a normal link.
	 * 
	 * @param path
	 *            path to <code>Link</code> component
	 * @param isAjax
	 *            Whether to simulate that AJAX (javascript) is enabled or not. If it's false then
	 *            AjaxLink and AjaxSubmitLink will fail, since it wouldn't work in real life.
	 *            AjaxFallbackLink will be invoked with null as the AjaxRequestTarget parameter.
	 */
	public void clickLink(String path, boolean isAjax)
	{
		Component linkComponent = getComponentFromLastRenderedPage(path);

		checkUsability(linkComponent);

		// if the link is an AjaxLink, we process it differently
		// than a normal link
		if (linkComponent instanceof AjaxLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + "is an AjaxLink and will " +
					"not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxLink<?> link = (AjaxLink<?>)linkComponent;

			setupRequestAndResponse(true);
			WebRequestCycle requestCycle = createRequestCycle();
			AjaxRequestTarget target = getApplication().newAjaxRequestTarget(link.getPage());
			requestCycle.setRequestTarget(target);

			link.onClick(target);

			// process the request target
			processRequestCycle(requestCycle);
		}
		// AjaxFallbackLinks is processed like an AjaxLink if isAjax is true
		// If it's not handling of the linkComponent is passed through to the
		// Link.
		else if (linkComponent instanceof AjaxFallbackLink && isAjax)
		{
			AjaxFallbackLink<?> link = (AjaxFallbackLink<?>)linkComponent;

			setupRequestAndResponse(true);
			WebRequestCycle requestCycle = createRequestCycle();
			AjaxRequestTarget target = getApplication().newAjaxRequestTarget(link.getPage());
			requestCycle.setRequestTarget(target);

			link.onClick(target);

			// process the request target
			processRequestCycle(requestCycle);
		}
		// if the link is an AjaxSubmitLink, we need to find the form
		// from it using reflection so we know what to submit.
		else if (linkComponent instanceof AjaxSubmitLink)
		{
			// If it's not ajax we fail
			if (isAjax == false)
			{
				fail("Link " + path + "is an AjaxSubmitLink and " +
					"will not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxSubmitLink link = (AjaxSubmitLink)linkComponent;

			// We cycle through the attached behaviors and select the
			// LAST matching behavior as the one we handle.
			List<IBehavior> behaviors = link.getBehaviors();
			AjaxFormSubmitBehavior ajaxFormSubmitBehavior = null;
			for (IBehavior behavior : behaviors)
			{
				if (behavior instanceof AjaxFormSubmitBehavior)
				{
					AjaxFormSubmitBehavior submitBehavior = (AjaxFormSubmitBehavior)behavior;
					ajaxFormSubmitBehavior = submitBehavior;
				}
			}

			String failMessage = "No form submit behavior found on the submit link. Strange!!";
			notNull(failMessage, ajaxFormSubmitBehavior);

			WebRequestCycle requestCycle = setupRequestAndResponse(true);

			setupAjaxSubmitRequestParameters(linkComponent, ajaxFormSubmitBehavior);

			// Ok, finally we "click" the link
			ajaxFormSubmitBehavior.onRequest();

			// process the request target
			processRequestCycle(requestCycle);
		}
		/*
		 * If the link is a submitlink then we pretend to have clicked it
		 */
		else if (linkComponent instanceof SubmitLink)
		{
			SubmitLink submitLink = (SubmitLink)linkComponent;

			String pageRelativePath = submitLink.getInputName();
			getParametersForNextRequest().put(pageRelativePath, new String[] { "x" });

			Form<?> form = submitLink.getForm();
			form.visitFormComponents(new FormComponent.IVisitor()
			{
				public Object formComponent(IFormVisitorParticipant formComponent)
				{
					FormComponent<?> component = (FormComponent<?>)formComponent;
					if (getParametersForNextRequest().containsKey(component.getInputName()) == false)
					{
						getParametersForNextRequest().put(component.getInputName(),
							new String[] { component.getDefaultModelObjectAsString() });
					}

					return IVisitor.CONTINUE_TRAVERSAL;
				}
			});

			submitForm(submitLink.getForm().getPageRelativePath());
		}
		// if the link is a normal link (or ResourceLink)
		else if (linkComponent instanceof AbstractLink)
		{
			AbstractLink link = (AbstractLink)linkComponent;

			/*
			 * If the link is a bookmarkable link, then we need to transfer the parameters to the
			 * next request.
			 */
			if (link instanceof BookmarkablePageLink)
			{
				BookmarkablePageLink<?> bookmarkablePageLink = (BookmarkablePageLink<?>)link;
				try
				{
					BookmarkablePageLink.class.getDeclaredField("parameters");
					Method getParametersMethod = BookmarkablePageLink.class.getDeclaredMethod(
						"getPageParameters", (Class<?>[])null);
					getParametersMethod.setAccessible(true);

					PageParameters parameters = (PageParameters)getParametersMethod.invoke(
						bookmarkablePageLink, (Object[])null);
					setParametersForNextRequest(parameters.toRequestParameters());
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
			fail("Link " + path + " is not a Link, AjaxLink, AjaxFallbackLink or AjaxSubmitLink");
		}
	}

	/**
	 * Submits the <code>Form</code> in the last rendered <code>Page</code>.
	 * 
	 * @param path
	 *            path to <code>Form</code> component
	 */
	public void submitForm(String path)
	{
		Form<?> form = (Form<?>)getComponentFromLastRenderedPage(path);
		executeListener(form);
	}

	/**
	 * Sets a parameter for the <code>Component</code> with the given path to be used with the next
	 * request.
	 * <p>
	 * NOTE: this method only works when a <code>Page</code> was rendered first.
	 * 
	 * @param componentPath
	 *            path to the <code>Component</code>
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
			getParametersForNextRequest().put(((FormComponent<?>)c).getInputName(),
				new String[] { value.toString() });
		}
		else
		{
			getParametersForNextRequest().put(c.getPath(), new String[] { value.toString() });
		}

	}

	/**
	 * Asserts the last rendered <code>Page</code> class.
	 * 
	 * FIXME explain why the code is so complicated to compare two classes, or simplify
	 * 
	 * @param <C>
	 * 
	 * @param expectedRenderedPageClass
	 *            expected class of last rendered page
	 * @return a <code>Result</code>
	 */
	public <C extends Page> Result isRenderedPage(Class<C> expectedRenderedPageClass)
	{
		Page page = getLastRenderedPage();
		if (page == null)
		{
			return Result.fail("page was null");
		}
		if (!page.getClass().isAssignableFrom(expectedRenderedPageClass))
		{
			return isEqual(Classes.simpleName(expectedRenderedPageClass),
				Classes.simpleName(page.getClass()));
		}
		return Result.pass();
	}

	/**
	 * Asserts last rendered <code>Page</code> against an expected HTML document.
	 * <p>
	 * Use <code>-Dwicket.replace.expected.results=true</code> to automatically replace the expected
	 * output file.
	 * </p>
	 * 
	 * @param pageClass
	 *            used to load the <code>File</code> (relative to <code>clazz</code> package)
	 * @param filename
	 *            expected output <code>File</code> name
	 * @throws Exception
	 */
	public void assertResultPage(final Class<?> pageClass, final String filename) throws Exception
	{
		// Validate the document
		String document = getServletResponse().getDocument();
		DiffUtil.validatePage(document, pageClass, filename, true);
	}

	/**
	 * Asserts last rendered <code>Page</code> against an expected HTML document as a
	 * <code>String</code>.
	 * 
	 * @param expectedDocument
	 *            expected output
	 * @return a <code>Result</code>
	 * @throws Exception
	 */
	public Result isResultPage(final String expectedDocument) throws Exception
	{
		// Validate the document
		String document = getServletResponse().getDocument();
		return isTrue("expected rendered page equals", document.equals(expectedDocument));
	}

	/**
	 * Asserts no error-level feedback messages.
	 * 
	 * @return a <code>Result</code>
	 */
	public Result hasNoErrorMessage()
	{
		List<Serializable> messages = getMessages(FeedbackMessage.ERROR);
		return isTrue(
			"expect no error message, but contains\n" + WicketTesterHelper.asLined(messages),
			messages.isEmpty());
	}

	/**
	 * Asserts no info-level feedback messages.
	 * 
	 * @return a <code>Result</code>
	 */
	public Result hasNoInfoMessage()
	{
		List<Serializable> messages = getMessages(FeedbackMessage.INFO);
		return isTrue(
			"expect no info message, but contains\n" + WicketTesterHelper.asLined(messages),
			messages.isEmpty());
	}

	/**
	 * Retrieves <code>FeedbackMessages</code>.
	 * 
	 * @param level
	 *            level of feedback message, for example:
	 *            <code>FeedbackMessage.DEBUG or FeedbackMessage.INFO.. etc</code>
	 * @return <code>List</code> of messages (as <code>String</code>s)
	 * @see FeedbackMessage
	 */
	public List<Serializable> getMessages(final int level)
	{
		FeedbackMessages feedbackMessages = Session.get().getFeedbackMessages();
		List<FeedbackMessage> allMessages = feedbackMessages.messages(new IFeedbackMessageFilter()
		{
			private static final long serialVersionUID = 1L;

			public boolean accept(FeedbackMessage message)
			{
				return message.getLevel() == level;
			}
		});
		List<Serializable> actualMessages = new ArrayList<Serializable>();
		for (FeedbackMessage message : allMessages)
		{
			actualMessages.add(message.getMessage());
		}
		return actualMessages;
	}

	/**
	 * Dumps the source of last rendered <code>Page</code>.
	 */
	public void dumpPage()
	{
		log.info(getServletResponse().getDocument());
	}

	/**
	 * Dumps the <code>Component</code> trees.
	 */
	public void debugComponentTrees()
	{
		debugComponentTrees("");
	}

	/**
	 * Dumps the <code>Component</code> trees to log. Show only the <code>Component</code>s whose
	 * paths contain the filter <code>String</code>.
	 * 
	 * @param filter
	 *            a filter <code>String</code>
	 */
	public void debugComponentTrees(String filter)
	{
		log.info("debugging ----------------------------------------------");
		for (WicketTesterHelper.ComponentData obj : WicketTesterHelper.getComponentData(getLastRenderedPage()))
		{
			if (obj.path.matches(".*" + filter + ".*"))
			{
				log.info("path\t" + obj.path + " \t" + obj.type + " \t[" + obj.value + "]");
			}
		}
	}

	/**
	 * Tests that a <code>Component</code> has been added to a <code>AjaxRequestTarget</code>, using
	 * {@link AjaxRequestTarget#addComponent(Component)}. This method actually tests that a
	 * <code>Component</code> is on the Ajax response sent back to the client.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using Javascript. But it shouldn't be needed because you have to trust that the Wicket
	 * Ajax Javascript just works.
	 * 
	 * @param component
	 *            the <code>Component</code> to test
	 * @return a <code>Result</code>
	 */
	public Result isComponentOnAjaxResponse(Component component)
	{
		String failMessage = "A component which is null could not have been added to the AJAX response";
		notNull(failMessage, component);

		Result result;

		// test that the component renders the placeholder tag if it's not
		// visible
		if (!component.isVisible())
		{
			failMessage = "A component which is invisible and doesn't render a placeholder tag"
				+ " will not be rendered at all and thus won't be accessible for subsequent AJAX interaction";
			result = isTrue(failMessage, component.getOutputMarkupPlaceholderTag());
			if (result.wasFailed())
			{
				return result;
			}
		}

		// Get the AJAX response
		String ajaxResponse = getServletResponse().getDocument();

		// Test that the previous response was actually a AJAX response
		failMessage = "The Previous response was not an AJAX response. "
			+ "You need to execute an AJAX event, using clickLink, before using this assert";
		boolean isAjaxResponse = Pattern.compile(
			"^<\\?xml version=\"1.0\" encoding=\".*?\"\\?><ajax-response>")
			.matcher(ajaxResponse)
			.find();
		result = isTrue(failMessage, isAjaxResponse);
		if (result.wasFailed())
		{
			return result;
		}

		// See if the component has a markup id
		String markupId = component.getMarkupId();

		failMessage = "The component doesn't have a markup id, "
			+ "which means that it can't have been added to the AJAX response";
		result = isTrue(failMessage, !Strings.isEmpty(markupId));
		if (result.wasFailed())
		{
			return result;
		}

		// Look for that the component is on the response, using the markup id
		boolean isComponentInAjaxResponse = ajaxResponse.matches("(?s).*<component id=\"" +
			markupId + "\"[^>]*?>.*");
		failMessage = "Component wasn't found in the AJAX response";
		return isTrue(failMessage, isComponentInAjaxResponse);
	}

	/**
	 * Simulates the firing of an Ajax event.
	 * 
	 * @see #executeAjaxEvent(Component, String)
	 * 
	 * @since 1.2.3
	 * @param componentPath
	 *            the <code>Component</code> path
	 * @param event
	 *            the event which we simulate being fired. If <code>event</code> is
	 *            <code>null</code>, the test will fail.
	 */
	public void executeAjaxEvent(String componentPath, String event)
	{
		Component component = getComponentFromLastRenderedPage(componentPath);
		executeAjaxEvent(component, event);
	}

	/**
	 * Simulates the firing of all ajax timer behaviors on the page
	 *
	 * @param page
	 *      the page which timers will be executed
	 */
	public void executeAllTimerBehaviors(final MarkupContainer page)
	{
		// execute all timer behaviors for the page itself
		internalExecuteAllTimerBehaviors(page);

		// and for all its children
		page.visitChildren(Component.class, new IVisitor<Component>()
		{
			public Object component(final Component component)
			{
				internalExecuteAllTimerBehaviors(component);
				return CONTINUE_TRAVERSAL;
			}
		});
	}

	private void internalExecuteAllTimerBehaviors(final Component component)
	{
		List<IBehavior> behaviors = BehaviorsUtil.getBehaviors(component, AbstractAjaxTimerBehavior.class);
		for (IBehavior b : behaviors)
		{
			AbstractAjaxTimerBehavior timer = (AbstractAjaxTimerBehavior) b;
			
			if (!timer.isStopped())
			{
				if (log.isDebugEnabled())
				{
					log.debug("Triggering AjaxSelfUpdatingTimerBehavior: {}", component.getClassRelativePath());
				}

				checkUsability(component);
				
				executeBehavior(timer);
			}
		}
	}

	/**
	 * Simulates the firing of an Ajax event. You add an Ajax event to a <code>Component</code> by
	 * using:
	 * 
	 * <pre>
	 *     ...
	 *     component.add(new AjaxEventBehavior(&quot;ondblclick&quot;) {
	 *         public void onEvent(AjaxRequestTarget) {}
	 *     });
	 *     ...
	 * </pre>
	 * 
	 * You can then test that the code inside <code>onEvent</code> actually does what it's supposed
	 * to, using the <code>WicketTester</code>:
	 * 
	 * <pre>
	 *     ...
	 *     tester.executeAjaxEvent(component, &quot;ondblclick&quot;);
	 *     // Test that the code inside onEvent is correct.
	 *     ...
	 * </pre>
	 * 
	 * This also works with <code>AjaxFormSubmitBehavior</code>, where it will "submit" the
	 * <code>Form</code> before executing the command.
	 * <p>
	 * PLEASE NOTE! This method doesn't actually insert the <code>Component</code> in the client DOM
	 * tree, using Javascript.
	 * 
	 * 
	 * @param component
	 *            the <code>Component</code> that has the <code>AjaxEventBehavior</code> we want to
	 *            test. If the <code>Component</code> is <code>null</code>, the test will fail.
	 * @param event
	 *            the event to simulate being fired. If <code>event</code> is <code>null</code>, the
	 *            test will fail.
	 */
	public void executeAjaxEvent(final Component component, final String event)
	{
		setCreateAjaxRequest(true);

		String failMessage = "Can't execute event on a component which is null.";
		notNull(failMessage, component);

		failMessage = "event must not be null";
		notNull(failMessage, event);

		checkUsability(component);

		// Run through all the behavior and select the LAST ADDED behavior which
		// matches the event parameter.
		AjaxEventBehavior ajaxEventBehavior = null;
		List<IBehavior> behaviors = component.getBehaviors();
		for (IBehavior behavior : behaviors)
		{
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
		// which matches the parameters we fail.
		failMessage = "No AjaxEventBehavior found on component: " + component.getId() +
			" which matches the event: " + event;
		notNull(failMessage, ajaxEventBehavior);

		// when the requestcycle is not created via
		// setupRequestAndResponse(true), than create a new
		// one
		WebRequestCycle requestCycle = resolveRequestCycle();
		if (!requestCycle.getWebRequest().isAjax())
		{
			throw new IllegalStateException(
				"The ServletWebRequest was created without wicket-ajax header. Please use tester.setCreateAjaxRequest(true)");
		}

		// If the event is an FormSubmitBehavior then also "submit" the form
		if (ajaxEventBehavior instanceof AjaxFormSubmitBehavior)
		{
			AjaxFormSubmitBehavior ajaxFormSubmitBehavior = (AjaxFormSubmitBehavior)ajaxEventBehavior;
			setupAjaxSubmitRequestParameters(component, ajaxFormSubmitBehavior);
		}

		// process the event
		ajaxEventBehavior.onRequest();

		// process the request target
		processRequestCycle(requestCycle);
	}

	/**
	 * 
	 * @return WebRequestCycle
	 */
	protected WebRequestCycle resolveRequestCycle()
	{
		// initialize the request only if needed to allow the user to pass
		// request parameters, see WICKET-254
		WebRequestCycle requestCycle;
		if (RequestCycle.get() == null)
		{
			requestCycle = setupRequestAndResponse();
		}
		else
		{
			requestCycle = (WebRequestCycle)RequestCycle.get();

			// If a ajax request is requested but the existing is not, than we
			// still need to create
			// a new one
			if ((requestCycle.getWebRequest().isAjax() == false) && (isCreateAjaxRequest() == true))
			{
				setParametersForNextRequest(requestCycle.getWebRequest().getParameterMap());
				requestCycle = setupRequestAndResponse();
			}
		}
		return requestCycle;
	}

	/**
	 * Retrieves a <code>TagTester</code> based on a <code>wicket:id</code>. If more
	 * <code>Component</code>s exist with the same <code>wicket:id</code> in the markup, only the
	 * first one is returned.
	 * 
	 * @param wicketId
	 *            the <code>wicket:id</code> to search for
	 * @return the <code>TagTester</code> for the tag which has the given <code>wicket:id</code>
	 */
	public TagTester getTagByWicketId(String wicketId)
	{
		return TagTester.createTagByAttribute(getServletResponse().getDocument(), "wicket:id",
			wicketId);
	}

	/**
	 * Modified version of BaseWicketTester#getTagByWicketId(String) that returns all matching tags
	 * instead of just the first.
	 * 
	 * @see BaseWicketTester#getTagByWicketId(String)
	 */
	public static List<TagTester> getTagsByWicketId(WicketTester tester, String wicketId)
	{
		return TagTester.createTagsByAttribute(tester.getServletResponse().getDocument(),
			"wicket:id", wicketId, false);
	}

	/**
	 * Retrieves a <code>TagTester</code> based on an DOM id. If more <code>Component</code>s exist
	 * with the same id in the markup, only the first one is returned.
	 * 
	 * @param id
	 *            the DOM id to search for.
	 * @return the <code>TagTester</code> for the tag which has the given DOM id
	 */
	public TagTester getTagById(String id)
	{
		return TagTester.createTagByAttribute(getServletResponse().getDocument(), "id", id);
	}

	/**
	 * Helper method for all the places where an Ajax call should submit an associated
	 * <code>Form</code>.
	 * 
	 * @param component
	 *            The component the behavior is attached to
	 * @param behavior
	 *            The <code>AjaxFormSubmitBehavior</code> with the <code>Form</code> to "submit"
	 */
	private void setupAjaxSubmitRequestParameters(final Component component,
		AjaxFormSubmitBehavior behavior)
	{
		// The form that needs to be "submitted".
		Form<?> form = behavior.getForm();

		String failMessage = "No form attached to the submitlink.";
		notNull(failMessage, form);

		checkUsability(form);

		final Map<String, Object> requestParameters = getServletRequest().getParameterMap();

		/*
		 * Means that an button or an ajax link was clicked and needs to be added to the request
		 * parameters to their form component correctly resolves the submit origin
		 */
		if (component instanceof Button)
		{
			Button clickedButton = (Button)component;
			getServletRequest().setParameter(clickedButton.getInputName(), clickedButton.getValue());
		}
		else if (component instanceof AjaxSubmitLink)
		{
			String inputName = ((IFormSubmittingComponent)component).getInputName();
			requestParameters.put(inputName, new String[] { "x" });
		}

		form.visitFormComponents(new FormComponent.AbstractVisitor()
		{
			@Override
			public void onFormComponent(FormComponent<?> formComponent)
			{
				/*
				 * It is important to don't add every button input name as an request parameter to
				 * respect the submit origin
				 */
				if (!(formComponent instanceof RadioGroup) &&
					!(formComponent instanceof CheckGroup) && !(formComponent instanceof Button) &&
					formComponent.isVisibleInHierarchy() && formComponent.isEnabledInHierarchy())
				{
					if (!((formComponent instanceof IFormSubmittingComponent) && (component instanceof IFormSubmittingComponent)) ||
						(component == formComponent))
					{
						String name = formComponent.getInputName();
						String value = formComponent.getValue();

						// Set request parameter with the field value, but do not modify an existing
						// request parameter explicitly set using FormTester.setValue()
						if (!getServletRequest().getParameterMap().containsKey(name) &&
							!getParametersForNextRequest().containsKey(name))
						{
							getServletRequest().setParameter(name, value);
							getParametersForNextRequest().put(name, new String[] { value });
						}
					}
				}
			}
		});
	}

	/**
	 * Retrieves the content type from the response header.
	 * 
	 * @return the content type from the response header
	 */
	public String getContentTypeFromResponseHeader()
	{
		String contentType = ((MockHttpServletResponse)getWicketResponse().getHttpServletResponse()).getHeader("Content-Type");
		if (contentType == null)
		{
			throw new WicketRuntimeException("No Content-Type header found");
		}
		return contentType;
	}

	/**
	 * Retrieves the content length from the response header.
	 * 
	 * @return the content length from the response header
	 */
	public int getContentLengthFromResponseHeader()
	{
		String contentLength = ((MockHttpServletResponse)getWicketResponse().getHttpServletResponse()).getHeader("Content-Length");
		if (contentLength == null)
		{
			throw new WicketRuntimeException("No Content-Length header found");
		}
		return Integer.parseInt(contentLength);
	}

	/**
	 * Retrieves the last-modified value from the response header.
	 * 
	 * @return the last-modified value from the response header
	 */
	public String getLastModifiedFromResponseHeader()
	{
		return ((MockHttpServletResponse)getWicketResponse().getHttpServletResponse()).getHeader("Last-Modified");
	}

	/**
	 * Retrieves the content disposition from the response header.
	 * 
	 * @return the content disposition from the response header
	 */
	public String getContentDispositionFromResponseHeader()
	{
		return ((MockHttpServletResponse)getWicketResponse().getHttpServletResponse()).getHeader("Content-Disposition");
	}

	private Result isTrue(String message, boolean condition)
	{
		if (condition)
		{
			return Result.pass();
		}
		return Result.fail(message);
	}

	private Result isFalse(String message, boolean condition)
	{
		if (!condition)
		{
			return Result.pass();
		}
		return Result.fail(message);
	}

	protected final Result isEqual(Object expected, Object actual)
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
		if (object == null)
		{
			fail(message);
		}
	}

	private Result isNull(String message, Object object)
	{
		if (object != null)
		{
			return Result.fail(message);
		}
		return Result.pass();
	}

	/**
	 * Checks whether a component is visible and/or enabled before usage
	 * 
	 * @param component
	 */
	protected void checkUsability(final Component component)
	{
		if (component.isVisibleInHierarchy() == false)
		{
			fail("The component is currently not visible in the hierarchy and thus you can not be used." +
				" Component: " + component);
		}

		if (component.isEnabledInHierarchy() == false)
		{
			fail("The component is currently not enabled in the hierarchy and thus you can not be used." +
				" Component: " + component);
		}
	}

	protected final void fail(String message)
	{
		throw new WicketRuntimeException(message);
	}

	/**
	 * @param rc
	 */
	// FIXME 1.5: REMOVE THIS HACK. Currently there is no way to call
	// requestcycle.onbeginrequest() from outside and since tester shortcircuits
	// the normal
	// workflow it is necessary to call onbeginrequest manually
	@Deprecated
	public static void callOnBeginRequest(RequestCycle rc)
	{
		try
		{
			Method method = RequestCycle.class.getDeclaredMethod("onBeginRequest", (Class<?>[])null);
			method.setAccessible(true);
			method.invoke(rc, (Object[])null);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Exception invoking requestcycle.onbeginrequest()", e);
		}
	}

}
