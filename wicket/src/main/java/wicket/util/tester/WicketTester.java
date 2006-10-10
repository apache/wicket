/*
 * $Id: WicketTester.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000 (Wed, 24 May
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.tester;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.ajax.form.AjaxFormSubmitBehavior;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.AjaxLink;
import wicket.ajax.markup.html.form.AjaxSubmitLink;
import wicket.behavior.IBehavior;
import wicket.feedback.FeedbackMessage;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedbackMessageFilter;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.protocol.http.MockWebApplication;
import wicket.util.lang.Classes;
import wicket.util.string.Strings;
import wicket.util.tester.WicketTesterHelper.ComponentData;

/**
 * A helper to ease unit testing of Wicket applications without the need for a
 * servlet container. To start a test, we can use either startPage() or
 * startPanel():
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
 * 
 * 	//assert rendered page class
 * 	tester.assertRenderedPage(MyPage.class);
 * 
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
 * 
 * 	//click link and render
 * 	tester.clickLink(&quot;toYourPage&quot;);
 * 
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
 * 
 * 	tester.assertRenderedPage(YourPage.class);
 * 	tester.assertLabel(&quot;yourMessage&quot;, &quot;mock message&quot;);
 * 
 * 	// assert feedback messages in INFO Level 
 * 	tester.assertInfoMessages(new String[] { &quot;Wicket Rocks ;-)&quot; });
 * 
 * }
 * </pre>
 * 
 * Instead of <code>tester.startPage(pageClass)</code>, we define a
 * {@link wicket.util.tester.ITestPageSource} to provide testing page instance
 * for WicketTester. This is necessary because <code>YourPage</code> uses a
 * custom constructor, which is very common for transfering model data, can not
 * be instansiated by reflection. Finally, we use
 * <code>assertInfoMessages</code> to assert there is a feedback message
 * "Wicket Rocks ;-)" in INFO level.
 * 
 * TODO General: Example usage of FormTester
 * 
 * @author Ingram Chen
 * @author Juergen Donnerstag
 * @author Frank Bille
 */
public class WicketTester extends MockWebApplication
{
	/** log. */
	private static final Log log = LogFactory.getLog(WicketTester.class);

	/**
	 * create WicketTester with null path
	 * 
	 * @see #WicketTester(String)
	 */
	public WicketTester()
	{
		this(null);
	}

	/**
	 * create a WicketTester to help unit testing.
	 * 
	 * @param path
	 *            The absolute path on disk to the web application contents
	 *            (e.g. war root) - may be null
	 * 
	 * @see wicket.protocol.http.MockWebApplication#MockWebApplication(String)
	 */
	public WicketTester(final String path)
	{
		super(path);
	}

	/**
	 * Render a page defined in <code>TestPageSource</code>. This usually
	 * used when a page does not have default consturctor. For example, a
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
	public final Page startPage(ITestPageSource testPageSource)
	{
		setHomePage(DummyHomePage.class);
		setupRequestAndResponse();
		processRequestCycle();
		DummyHomePage page = (DummyHomePage)getLastRenderedPage();
		page.setTestPageSource(testPageSource);

		newRequestToComponent(page.getTestPageLink());
		return getLastRenderedPage();
	}

	/**
	 * 
	 * @param component
	 */
	private void newRequestToComponent(Component component)
	{
		setupRequestAndResponse();
		getServletRequest().setRequestToComponent(component);
		// getServletRequest().getSession().getPageMap(null);
		processRequestCycle();
	}

	/**
	 * Render the page
	 * 
	 * @param page
	 * @return The page rendered
	 */
	public final Page startPage(final Page page)
	{
		setHomePage(DummyHomePage.class);
		processRequestCycle(page);

		Page last = getLastRenderedPage();

		createRequestCycle();
		getWicketSession().touch(page);
		if (page != last)
		{
			getWicketSession().touch(last);
		}
		return last;
	}

	/**
	 * Render a page from its default constructor.
	 * 
	 * @param pageClass
	 *            a test page class with default constructor
	 * @return Page Rendered Page
	 */
	public final Page startPage(Class<? extends Page> pageClass)
	{
		setHomePage(pageClass);
		setupRequestAndResponse();
		processRequestCycle();
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
	 * Render a panel from <code>Panel(MarkupContainer parent,String id)</code>
	 * constructor.
	 * 
	 * @param panelClass
	 *            a test panel class with
	 *            <code>Panel(MarkupContainer parent,String id)</code>
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

					public void getTestPanel(MarkupContainer parent, String panelId)
					{
						try
						{
							Constructor c = panelClass.getConstructor(new Class[] {
									MarkupContainer.class, String.class });
							c.newInstance(new Object[] { parent, panelId });
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
			Assert.fail("path: '" + path + "' does no exist for page: "
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
		PageLink pageLink = (PageLink)getComponentFromLastRenderedPage(path);
		try
		{
			Field iPageLinkField = pageLink.getClass().getDeclaredField("pageLink");
			iPageLinkField.setAccessible(true);
			IPageLink iPageLink = (IPageLink)iPageLinkField.get(pageLink);
			Assert.assertEquals(expectedPageClass, iPageLink.getPageIdentity());
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
	 */
	public void assertComponent(String path, Class<?> expectedComponentClass)
	{
		Component component = getComponentFromLastRenderedPage(path);
		Assert.assertTrue("component '" + Classes.simpleName(component.getClass())
				+ "' is not type:" + Classes.simpleName(expectedComponentClass),
				expectedComponentClass.isAssignableFrom(component.getClass()));
	}

	/**
	 * assert component visible.
	 * 
	 * @param path
	 *            path to component
	 */
	public void assertVisible(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			Assert.fail("path: '" + path + "' does no exist for page: "
					+ Classes.simpleName(getLastRenderedPage().getClass()));
		}

		Assert.assertTrue("component '" + path + "' is not visible", component.isVisible());
	}

	/**
	 * assert component invisible.
	 * 
	 * @param path
	 *            path to component
	 */
	public void assertInvisible(String path)
	{
		Assert.assertNull("component '" + path + "' is visible",
				getComponentFromLastRenderedPage(path));
	}

	/**
	 * assert the content of last rendered page contains(matches) regex pattern.
	 * 
	 * @param pattern
	 *            reqex pattern to match
	 */
	public void assertContains(String pattern)
	{
		Assert.assertTrue("pattern '" + pattern + "' not found", getServletResponse().getDocument()
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
	 * create a {@link FormTester} for the form at path, and fill all child
	 * {@link wicket.markup.html.form.FormComponent}s with blank String
	 * initially.
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
	 *            {@link wicket.markup.html.form.FormComponent}s with
	 *            blankString initially.
	 * @return FormTester A FormTester instance for testing form
	 * @see FormTester
	 */
	public FormTester newFormTester(String path, boolean fillBlankString)
	{
		return new FormTester(path, (Form)getComponentFromLastRenderedPage(path), this,
				fillBlankString);
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
				Assert.fail("Link " + path + "is an AjaxLink and will "
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
				Assert.fail("Link " + path + "is an AjaxSubmitLink and "
						+ "will not be invoked when AJAX (javascript) is disabled.");
			}

			AjaxSubmitLink link = (AjaxSubmitLink)linkComponent;

			// We cycle through the attached behaviors and select the
			// LAST matching behavior as the one we handle.
			List behaviors = link.getBehaviors();
			AjaxFormSubmitBehavior ajaxFormSubmitBehavior = null;
			for (Object behavior : behaviors)
			{
				if (behavior instanceof AjaxFormSubmitBehavior)
				{
					AjaxFormSubmitBehavior submitBehavior = (AjaxFormSubmitBehavior)behavior;
					ajaxFormSubmitBehavior = submitBehavior;
				}
			}

			String failMessage = "No form submit behavior found on the submit link. Strange!!";
			Assert.assertNotNull(failMessage, ajaxFormSubmitBehavior);

			// We need to get the form submitted, using reflection.
			// It needs to be "submitted".
			Form form = null;
			try
			{
				Field formField = AjaxFormSubmitBehavior.class.getDeclaredField("form");
				formField.setAccessible(true);
				form = (Form)formField.get(ajaxFormSubmitBehavior);
			}
			catch (Exception e)
			{
				Assert.fail(e.getMessage());
			}

			failMessage = "No form attached to the submitlink.";
			Assert.assertNotNull(failMessage, form);

			setupRequestAndResponse();
			RequestCycle requestCycle = createRequestCycle();

			// "Submit" the form
			form.visitFormComponents(new FormComponent.AbstractVisitor()
			{
				@Override
				public void onFormComponent(FormComponent formComponent)
				{
					if (!(formComponent instanceof Button)
							&& !(formComponent instanceof RadioGroup))
					{
						String name = formComponent.getInputName();
						String value = formComponent.getValue();

						getServletRequest().setParameter(name, value);
					}
				}
			});

			// Ok, finally we "click" the link
			ajaxFormSubmitBehavior.onRequest();

			// process the request target
			requestCycle.getRequestTarget().respond(requestCycle);
		}
		// if the link is a normal link
		else if (linkComponent instanceof Link)
		{
			Link link = (Link)linkComponent;
			newRequestToComponent(link);
		}
		else
		{
			Assert.fail("Link " + path
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
		newRequestToComponent(form);
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
			Assert.fail("before using this method, at least one page has to be rendered");
		}

		Component c = getComponentFromLastRenderedPage(componentPath);
		if (c == null)
		{
			Assert.fail("component " + componentPath + " was not found");
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
	 * @param expectedReneredPageClass
	 *            expected class of last renered page
	 */
	public void assertRenderedPage(Class expectedReneredPageClass)
	{
		if (!getLastRenderedPage().getClass().isAssignableFrom(expectedReneredPageClass))
		{
			Assert.assertEquals(Classes.simpleName(expectedReneredPageClass), Classes
					.simpleName(getLastRenderedPage().getClass()));
		}
	}

	/**
	 * assert no error feedback messages
	 */
	public void assertNoErrorMessage()
	{
		List<String> messages = getMessages(FeedbackMessage.ERROR);
		Assert.assertTrue("expect no error message, but contains\n"
				+ WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * assert no info feedback messages
	 */
	public void assertNoInfoMessage()
	{
		List<String> messages = getMessages(FeedbackMessage.INFO);
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
		List<String> actualMessages = getMessages(FeedbackMessage.ERROR);
		WicketTesterHelper.assertEquals(Arrays.asList(expectedErrorMessages), actualMessages);
	}

	/**
	 * assert info feedback message
	 * 
	 * @param expectedInfoMessages
	 *            expected info messages
	 */
	public void assertInfoMessages(String[] expectedInfoMessages)
	{
		List<String> actualMessages = getMessages(FeedbackMessage.INFO);
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
	public List<String> getMessages(final int level)
	{
		FeedbackMessages feedbackMessages = getLastRenderedPage().getFeedbackMessages();
		List allMessages = feedbackMessages.messages(new IFeedbackMessageFilter()
		{
			private static final long serialVersionUID = 1L;

			public boolean accept(FeedbackMessage message)
			{
				return message.getLevel() == level;
			}
		});
		List<String> actualMessages = new ArrayList<String>();
		for (Iterator iter = allMessages.iterator(); iter.hasNext();)
		{
			actualMessages.add(((FeedbackMessage)iter.next()).getMessage());
		}
		return actualMessages;
	}

	/**
	 * assert previous rendered page expired
	 * 
	 * TODO Post 1.2: General: This test is no longer valid because it depends
	 * on an implementation detail that just changed!
	 * 
	 * public void assertExpirePreviousPage() { PageMap pageMap =
	 * getWicketSession().getPageMap(null); Field internalMapCacheField; try {
	 * internalMapCacheField = pageMap.getClass().getDeclaredField("pages");
	 * internalMapCacheField.setAccessible(true); MostRecentlyUsedMap mru =
	 * (MostRecentlyUsedMap)internalMapCacheField.get(pageMap);
	 * Assert.assertFalse("Previous Page '" +
	 * Classes.name(getPreviousRenderedPage().getClass()) + "' not expire", mru
	 * .containsValue(getPreviousRenderedPage())); } catch (SecurityException e) {
	 * throw convertoUnexpect(e); } catch (NoSuchFieldException e) { throw
	 * convertoUnexpect(e); } catch (IllegalAccessException e) { throw
	 * convertoUnexpect(e); } }
	 */

	/**
	 * dump the source of last rendered page
	 */
	public void dumpPage()
	{
		log.info(getServletResponse().getDocument());
	}

	/**
	 * dump component tree
	 */
	public void debugComponentTrees()
	{
		debugComponentTrees("");
	}


	/**
	 * Dump the component tree to log.
	 * 
	 * @param filter
	 *            Show only the components, which path contains the
	 *            filterstring.
	 */
	public void debugComponentTrees(String filter)
	{
		log.info("debugging ----------------------------------------------");
		for (ComponentData element : WicketTesterHelper.getComponentData(getLastRenderedPage()))
		{
			if (element.path.matches(".*" + filter + ".*"))
			{
				log.info("path\t" + element.path + " \t" + element.type + " \t[" + element.value
						+ "]");
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
		String failMessage = "A component which is null could not have been added to the AJAX response";
		Assert.assertNotNull(failMessage, component);

		// Get the AJAX response
		String ajaxResponse = getServletResponse().getDocument();

		// Test that the previous response was actually a AJAX response
		failMessage = "The Previous response was not an AJAX response. "
				+ "You need to execute an AJAX event, using clickLink, before using this assert";
		boolean isAjaxResponse = ajaxResponse
				.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ajax-response>");
		Assert.assertTrue(failMessage, isAjaxResponse);

		// See if the component has a markup id
		String markupId = component.getMarkupId();

		failMessage = "The component doesn't have a markup id, "
				+ "which means that it can't have been added to the AJAX response";
		Assert.assertFalse(failMessage, Strings.isEmpty(markupId));

		// Look for that the component is on the response, using the markup id
		boolean isComponentInAjaxResponse = ajaxResponse.matches(".*<component id=\"" + markupId
				+ "\" ?>.*");
		failMessage = "Component wasn't found in the AJAX response";
		Assert.assertTrue(failMessage, isComponentInAjaxResponse);
	}

	/**
	 * Simulate that an AJAX event has been fired. You add an AJAX event to a
	 * component by using:
	 * 
	 * <pre>
	 *      ...
	 *      component.add(new AjaxEventBehavior(ClientEvent.DBLCLICK) {
	 *          public void onEvent(AjaxRequestTarget) {
	 *              // Do something.
	 *          }
	 *      });
	 *      ...
	 * </pre>
	 * 
	 * You can then test that the code inside onEvent actually does what it's
	 * supposed to, using the WicketTester:
	 * 
	 * <pre>
	 *      ...
	 *      tester.executeAjaxEvent(component, ClientEvent.DBLCLICK);
	 *                
	 *      // Test that the code inside onEvent is correct.
	 *      ...
	 * </pre>
	 * 
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
	@SuppressWarnings("unchecked")
	public void executeAjaxEvent(Component component, ClientEvent event)
	{
		String failMessage = "Can't execute event on a component which is null.";
		Assert.assertNotNull(failMessage, component);

		failMessage = "event must not be null";
		Assert.assertNotNull(failMessage, event);

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

				if (tmp.getEvent() == event)
				{
					ajaxEventBehavior = tmp;
				}
			}
		}

		// If there haven't been found any event behaviors on the component
		// which maches the parameters we fail.
		failMessage = "No AjaxEventBehavior found on component: " + component.getId()
				+ " which matches the event: " + event.toString();
		Assert.assertNotNull(failMessage, ajaxEventBehavior);

		setupRequestAndResponse();
		RequestCycle requestCycle = createRequestCycle();

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
}
