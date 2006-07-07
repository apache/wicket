/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.ajax;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.MockPageWithLinkAndComponent;
import wicket.Page;
import wicket.WicketTestCase;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.WebComponent;
import wicket.util.diff.DiffUtil;
import wicket.util.tester.ITestPageSource;

/**
 * Test the {@link AjaxRequestTarget}.
 * 
 * @author Frank Bille
 */
public class AjaxRequestTargetTest extends WicketTestCase
{
	/**
	 * Construct.
	 */
	public AjaxRequestTargetTest()
	{
		super("Test of AjaxRequestTarget");
	}

	/**
	 * Test that a normal <style> header contribution is added correctly.
	 * 
	 * @throws IOException
	 */
	public void testHeaderContribution1() throws IOException
	{
		executeHeaderTest(MockComponent1.class, "MockComponent1-expected.html");
	}

	/**
	 * Test that if there are no headers contributed in any components added to
	 * the response, we then don't add <header-contribution> at all.
	 * 
	 * @throws IOException
	 */
	public void testHeaderContribution2() throws IOException
	{
		executeHeaderTest(MockComponent2.class);
	}

	/**
	 * Test that a link with a wicket:id is added correctly.
	 * 
	 * @throws IOException
	 */
	public void testHeaderContribution3() throws IOException
	{
		executeHeaderTest(MockComponent3.class, "MockComponent3-expected.html");
	}

	private void executeHeaderTest(final Class<? extends Component> componentClass)
			throws IOException
	{
		executeHeaderTest(componentClass, null);
	}

	private void executeHeaderTest(final Class<? extends Component> componentClass,
			String expectedFile) throws IOException
	{
		final MockPageWithLinkAndComponent page = new MockPageWithLinkAndComponent();

		new WebComponent(page, MockPageWithLinkAndComponent.COMPONENT_ID).setOutputMarkupId(true);


		new AjaxLink(page, MockPageWithLinkAndComponent.LINK_ID)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				// Create an instance of the component
				try
				{
					Constructor<? extends Component> con = componentClass
							.getConstructor(new Class[] { MarkupContainer.class, String.class });

					Component comp = con.newInstance(new Object[] { page,
							MockPageWithLinkAndComponent.COMPONENT_ID });

					comp.setOutputMarkupId(true);

					target.addComponent(comp);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};

		application.startPage(new ITestPageSource()
		{
			private static final long serialVersionUID = 1L;

			public Page getTestPage()
			{
				return page;
			}
		});

		application.clickLink(MockPageWithLinkAndComponent.LINK_ID);

		String document = application.getServletResponse().getDocument();

		Pattern pat = Pattern.compile(".*<header-contribution>(.*?)</header-contribution>.*",
				Pattern.DOTALL);
		Matcher mat = pat.matcher(document);

		String headerContribution = null;

		if (mat.matches())
		{
			headerContribution = mat.group(1);
		}

		// If the filename is empty we use it to say that the headerContribution
		// should be empty.
		// This means that it doesn't exist at all
		if (expectedFile == null)
		{
			assertNull("There was a header contribution on the response: <" + headerContribution
					+ ">", headerContribution);
		}
		else
		{
			assertTrue(DiffUtil.validatePage(headerContribution, this.getClass(), expectedFile));
		}
	}
}
