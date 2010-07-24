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
package org.apache.wicket.examples.library;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.border.Border;


/**
 * Ensures that user is authenticated in session. If no user is signed in, a sign in is forced by
 * redirecting the browser to the SignIn page.
 * <p>
 * This base class also creates a border for each page subclass, automatically adding children of
 * the page to the border. This accomplishes two important things: (1) subclasses do not have to
 * repeat the code to create the border navigation and (2) since subclasses do not repeat this code,
 * they are not hardwired to page navigation structure details
 * 
 * @author Jonathan Locke
 */
public class AuthenticatedWebPage extends WicketExamplePage
{
	private final Border border;

	/**
	 * Contruct
	 */
	public AuthenticatedWebPage()
	{
		border = new LibraryApplicationBorder("border");
	}

	/**
	 * @see org.apache.wicket.examples.WicketExamplePage#onInitialize()
	 */
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		// Create border and add it to the page
		add(border);

		// The WicketExamplePage constructor already created and added it. We need to move it into
		// the border.
		Component mainNavigation = border.getFromBorderBody("mainNavigation");

		border.addToBorder(mainNavigation);
	}


	/**
	 * Get downcast session object
	 * 
	 * @return The session
	 */
	public LibrarySession getLibrarySession()
	{
		return (LibrarySession)getSession();
	}

	/**
	 * For all components which shall not be added to the border
	 * 
	 * @param children
	 * @return this.
	 */
	protected MarkupContainer addToPage(Component... children)
	{
		return super.add(children);
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#add(org.apache.wicket.Component[])
	 */
	@Override
	public MarkupContainer add(final Component... children)
	{
		for (Component child : children)
		{
			if ((border == null) || (child == border))
			{
				super.add(children);
			}
			else
			{
				border.addToBorderBody(child);
			}
		}
		return this;
	}
}
