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
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.feedback.FeedbackMessage;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedbackMessageFilter;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.protocol.http.MockWebApplication;
import wicket.util.lang.Classes;

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
 */
public class WicketTester extends MockWebApplication
{
	/** log. */
	private static Log log = LogFactory.getLog(WicketTester.class);

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
	 * click the <code>Link</code> in the last rendered Page.
	 * 
	 * @param path
	 *            path to <code>Link</code> component
	 */
	public void clickLink(String path)
	{
		Link link = (Link)getComponentFromLastRenderedPage(path);
		newRequestToComponent(link);
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
		log.info("debugging ----------------------------------------------");
		for (Object element : WicketTesterHelper.getComponentData(getLastRenderedPage()))
		{
			WicketTesterHelper.ComponentData obj = (WicketTesterHelper.ComponentData)element;
			log.info("path\t" + obj.path + " \t" + obj.type + " \t[" + obj.value + "]");
		}
	}
}
