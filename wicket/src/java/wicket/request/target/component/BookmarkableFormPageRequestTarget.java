/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
package wicket.request.target.component;

import wicket.Page;
import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.form.Form;

/**
 * @author jcompagner
 */
public class BookmarkableFormPageRequestTarget extends BookmarkablePageRequestTarget
{

	private final String formName;

	/**
	 * Construct.
	 * @param pageMapName
	 * @param pageClass
	 * @param pageParameters
	 * @param formName 
	 */
	public BookmarkableFormPageRequestTarget(String pageMapName, Class<? extends Page> pageClass, PageParameters<String, Object> pageParameters, String formName)
	{
		super(pageMapName, pageClass, pageParameters);
		this.formName = formName;
	}

	/**
	 * @see wicket.request.target.component.BookmarkablePageRequestTarget#processEvents(wicket.RequestCycle)
	 */
	@Override
	public void processEvents(RequestCycle requestCycle)
	{
		Page page = getPage(requestCycle);
		Form form = (Form)page.get(formName);
		if(form != null)
		{
			form.onFormSubmitted();
		}
	}
	
	/**
	 * @see wicket.request.target.component.BookmarkablePageRequestTarget#respond(wicket.RequestCycle)
	 */
	@Override
	public void respond(RequestCycle requestCycle)
	{
		// Let the page render itself
		getPage(requestCycle).renderPage();
	}
}
