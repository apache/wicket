/*
 * $Id$
 * $Revision$ $Date$
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
import wicket.PageMap;
import wicket.WicketRuntimeException;
import wicket.feedback.FeedbackMessage;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedbackMessageFilter;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.link.IPageLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.PageLink;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.protocol.http.MockWebApplication;
import wicket.util.collections.MostRecentlyUsedMap;
import wicket.util.lang.Classes;

/**
 * 
 * @author ingram
 * @author Juergen Donnerstag
 */
public class WicketTester extends MockWebApplication
{
	/** log. */
	private static Log log = LogFactory.getLog(WicketTester.class);

	/**
	 * 
	 */
	public WicketTester()
	{
		this(null);
	}

	/**
	 * 
	 * @param path
	 */
	public WicketTester(final String path)
	{
		super(path);
	}

	/**
	 * Render a wicket WebPage that is defined in TestPageSource.
	 * 
	 * @param testPageSource
	 * @return Page
	 */
	public final Page startPage(TestPageSource testPageSource)
	{
		getPages().setHomePage(DummyHomePage.class);
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
		processRequestCycle();
	}

	/**
	 * render a wicket WebPage for pageClass
	 * 
	 * @param pageClass
	 * @return WebPage
	 */
	public final WebPage startPage(Class pageClass)
	{
		getPages().setHomePage(pageClass);
		setupRequestAndResponse();
		processRequestCycle();
		return (WebPage)getLastRenderedPage();
	}

	/**
	 * render a Panel that defined in TestPanelSource.
	 * 
	 * @param testPanelSource
	 * @return Panel
	 */
	public final Panel startPanel(final TestPanelSource testPanelSource)
	{
		return (Panel)startPage(new TestPageSource()
		{
			public Page getTestPage()
			{
				return new DummyPanelPage(testPanelSource);
			}
		}).get(DummyPanelPage.TEST_PANEL_ID);
	}

	/**
	 * render a Panel for panelClass
	 * 
	 * @param panelClass
	 * @return Panel
	 */
	public final Panel startPanel(final Class panelClass)
	{
		return (Panel)startPage(new TestPageSource()
		{
			public Page getTestPage()
			{
				return new DummyPanelPage(new TestPanelSource()
				{
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
	 * 
	 * @param e
	 * @return RuntimeException
	 */
	private RuntimeException convertoUnexpect(Exception e)
	{
		return new WicketRuntimeException("tester: unexpected", e);
	}

	/**
	 * get component From Last Rendered Page
	 * 
	 * @param path
	 * @return component at specified path
	 */
	public Component getComponentFromLastRenderedPage(String path)
	{
		Component component = getLastRenderedPage().get(path);
		if (component == null)
		{
			Assert.fail("path: '" + path + "' does no exist for page: "
					+ Classes.name(getLastRenderedPage().getClass()));
		}
		return component;
	}

	/**
	 * assert the model of Label component in String.
	 * 
	 * @param path
	 * @param expect
	 */
	public final void assertLabel(String path, String expect)
	{
		Label label = (Label)getComponentFromLastRenderedPage(path);
		Assert.assertEquals(expect, label.getModelObjectAsString());
	}

	/**
	 * assert PageLink link to pageClass.
	 * 
	 * @param path
	 * @param pageClass
	 */
	public final void assertPageLink(String path, Class pageClass)
	{
		PageLink pageLink = (PageLink)getComponentFromLastRenderedPage(path);
		try
		{
			Field iPageLinkField = pageLink.getClass().getDeclaredField("pageLink");
			iPageLinkField.setAccessible(true);
			IPageLink iPageLink = (IPageLink)iPageLinkField.get(pageLink);
			Assert.assertEquals(pageClass, iPageLink.getPageIdentity());
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
	 * assert componentClass defined at path ;
	 * 
	 * @param path
	 * @param componentClass
	 */
	public void assertComponent(String path, Class componentClass)
	{
		Component component = getComponentFromLastRenderedPage(path);
		Assert.assertTrue("componet '" + Classes.name(component.getClass()) + "' is not type:"
				+ Classes.name(componentClass), componentClass.isAssignableFrom(component
				.getClass()));
	}

	/**
	 * assert component at path visible.
	 * 
	 * @param path
	 */
	public void assertVisible(String path)
	{
		Assert.assertTrue("component '" + path + "' is not visible",
				getComponentFromLastRenderedPage(path).isVisible());
	}

	/**
	 * assert component at path invisible.
	 * 
	 * @param path
	 */
	public void assertInvisible(String path)
	{
		Assert.assertFalse("component '" + path + "' is visible", getComponentFromLastRenderedPage(
				path).isVisible());
	}

	/**
	 * assert last rendered page contain String pattern.
	 * 
	 * @param pattern
	 *            reqex pattern of containing string
	 */
	public void assertContains(String pattern)
	{
		Assert.assertTrue("pattern '" + pattern + "' not found", getServletResponse().getDocument()
				.matches("(?s).*" + pattern + ".*"));
	}

	/**
	 * assert ListView at path use expectList ;
	 * 
	 * @param path
	 * @param expectList
	 */
	public void assertListView(String path, List expectList)
	{
		ListView listView = (ListView)getComponentFromLastRenderedPage(path);
		WicketTesterHelper.assertEquals(expectList, listView.getList());
	}

	/**
	 * create a TestWorkingForm for the form of latest rendered WebPage
	 * 
	 * @param path
	 * @return FormTester
	 */
	public final FormTester newFormTester(String path)
	{
		return new FormTester(path, (Form)getComponentFromLastRenderedPage(path), this);
	}

	/**
	 * click the Link of the last rendered WebPage
	 * 
	 * @param path
	 */
	public final void clickLink(String path)
	{
		Link link = (Link)getComponentFromLastRenderedPage(path);
		newRequestToComponent(link);
	}

	/**
	 * assert last rendered WebPage
	 * 
	 * @param pageClass
	 */
	public final void assertRenderedPage(Class pageClass)
	{
		if (!getLastRenderedPage().getClass().isAssignableFrom(pageClass))
		{
			Assert.assertEquals(Classes.name(pageClass), Classes.name(getLastRenderedPage().getClass()));
		}
	}

	/**
	 * assert no error feedback message
	 */
	public final void assertNoErrorMessage()
	{
		List messages = getMessages(FeedbackMessage.ERROR);
		Assert.assertTrue("expect no error message, but contains\n"
				+ WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * assert no info feedback message
	 */
	public final void assertNoInfoMessage()
	{
		List messages = getMessages(FeedbackMessage.INFO);
		Assert.assertTrue("expect no info message, but contains\n"
				+ WicketTesterHelper.asLined(messages), messages.isEmpty());
	}

	/**
	 * assert error feedback message
	 * 
	 * @param expectMessages
	 */
	public final void assertErrorMessages(String[] expectMessages)
	{
		List actualMessages = getMessages(FeedbackMessage.ERROR);
		WicketTesterHelper.assertEquals(Arrays.asList(expectMessages), actualMessages);
	}

	/**
	 * assert info feedback message
	 * 
	 * @param expectMessages
	 */
	public final void assertInfoMessages(String[] expectMessages)
	{
		List actualMessages = getMessages(FeedbackMessage.INFO);
		WicketTesterHelper.assertEquals(Arrays.asList(expectMessages), actualMessages);
	}

	/**
	 * 
	 * @param level
	 * @return List
	 */
	private List getMessages(final int level)
	{
		FeedbackMessages feedbackMessages = getLastRenderedPage().getFeedbackMessages();
		List allMessages = feedbackMessages.messages(new IFeedbackMessageFilter()
		{
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
	 * assert previous page expired.
	 */
	public final void assertExpirePreviousPage()
	{
		PageMap pageMap = getWicketSession().getPageMap(null);
		Field internalMapCacheField;
		try
		{
			internalMapCacheField = pageMap.getClass().getDeclaredField("pages");
			internalMapCacheField.setAccessible(true);
			MostRecentlyUsedMap mru = (MostRecentlyUsedMap)internalMapCacheField.get(pageMap);
			Assert.assertFalse("Previous Page '" + Classes.name(getPreviousRenderedPage().getClass())
					+ "' not expire", mru.containsValue(getPreviousRenderedPage()));
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
	 * dump the source of lastet rendered WebPage to System.out
	 */
	public final void dumpPage()
	{
		log.info(getServletResponse().getDocument());
	}

	/**
	 * dump componet tree
	 */
	public final void debugComponentTrees()
	{
		log.info("debugging ----------------------------------------------");
		for (Iterator iter = WicketTesterHelper.getComponentData(getLastRenderedPage()).iterator(); iter
				.hasNext();)
		{
			WicketTesterHelper.ComponentData obj = (WicketTesterHelper.ComponentData)iter.next();
			log.info("path\t" + obj.path + " \t" + obj.type + " \t[" + obj.value + "]");
		}
	}
}
