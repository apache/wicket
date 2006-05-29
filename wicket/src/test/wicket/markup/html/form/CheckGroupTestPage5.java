/*
 * $Id: CheckGroupTestPage5.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:53:56 +0000 (Wed, 24
 * May 2006) $
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

package wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.model.Model;

/**
 * tests exception when check is outside any group
 * 
 * @author igor
 * 
 */
public class CheckGroupTestPage5 extends WebPage
{
	/**
	 * Constructor
	 */
	public CheckGroupTestPage5()
	{
		List list = new ArrayList();
		Form form = new Form(this, "form");
		CheckGroup group = new CheckGroup(form, "group", new Model(list));
		WebMarkupContainer container = new WebMarkupContainer(group, "container");
		Check check1 = new Check(group, "check1", new Model("check1"));
		// here we add check2 to the form so it is outside the group - it should
		// throw an exception when rendering
		Check check2 = new Check(form, "check2", new Model("check2"));

	}

	private static final long serialVersionUID = 1L;


}
