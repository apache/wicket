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
package org.apache.wicket.ng;

import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.response.Response;

public class Page2 extends Component2 implements RequestablePage
{
	private static final long serialVersionUID = 1L;

	int pageId;

	static int pageIdCounter;

	public Page2()
	{
		this(null);
	}

	public Page2(PageParameters parameters)
	{
		super("");
		pageId = pageIdCounter++;
		if (parameters == null)
		{
			pageParameters = new PageParameters();
		}
		else
		{
			pageParameters = parameters;
		}
	}

	public static Page2 get(int id)
	{
		Application app = Application.get();
		return (Page2)app.getPageManager().getPage(id);
	}

	public int getPageId()
	{
		return pageId;
	}

	private final PageParameters pageParameters;

	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	private int renderCount = 0;

	public int getRenderCount()
	{
		return renderCount;
	}

	public boolean isBookmarkable()
	{
		Boolean bookmarkable = null;
		if (bookmarkable == null)
		{
			try
			{

				if (getClass().getConstructor(new Class[] { }) != null)
				{
					bookmarkable = Boolean.TRUE;
				}

			}
			catch (Exception ignore)
			{
				try
				{
					if (getClass().getConstructor(new Class[] { PageParameters.class }) != null)
					{
						bookmarkable = Boolean.TRUE;
					}
				}
				catch (Exception ignore2)
				{
				}
			}
			if (bookmarkable == null)
			{
				bookmarkable = Boolean.FALSE;
			}
		}
		return bookmarkable.booleanValue();

	}

	public boolean isPageStateless()
	{
		return false;
	}

	public void renderPage()
	{
		++renderCount;

		System.out.println("Rendering");

		Response response = RequestCycle.get().getResponse();
		response.write("<html>\n");

		response.write("<body>\n");

		response.write("<p>This is a " + getClass().getName() + "</p>\n");

		for (Component2 c : getChildren())
		{
			c.renderComponent();
		}

		response.write("</body>\n");
		response.write("</html>\n");
	}

	private boolean wasCreatedBookmarkable;

	public void setWasCreatedBookmarkable(boolean wasCreatedBookmarkable)
	{
		this.wasCreatedBookmarkable = wasCreatedBookmarkable;
	}

	public boolean wasCreatedBookmarkable()
	{
		return wasCreatedBookmarkable;
	}

	@Override
	public Page2 getPage()
	{
		return this;
	}

	private int markupIdConter = 0;

	public int getMarkupIdConterNextValue()
	{
		return markupIdConter++;
	}

	@Override
	public boolean canCallListenerInterface()
	{
		return true;
	}
}
