/*
 * $Id: AbstractCheckGroupTestPage.java 3034 2005-10-21 07:34:47Z ivaynberg $
 * $Revision: 3034 $ $Date: 2005-10-21 09:34:47 +0200 (vr, 21 okt 2005) $
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

import java.io.Serializable;
import java.util.Arrays;

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.model.Model;

/**
 * Tests rendering of the CheckGroup and Check components
 * @author igor
 */
public class CheckGroupDisabledTestPage extends WebPage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Constructor
	 * @param list 
	 */
	@SuppressWarnings("unchecked") // It's not the warnings which are important here.
	public CheckGroupDisabledTestPage() {
		
		Form form=new Form(this, "form");
		CheckGroup group=new CheckGroup(form, "group", new Model((Serializable)Arrays.asList(new String[]{"check1","check2"})));
		group.setRenderBodyOnly(false);
		WebMarkupContainer container=new WebMarkupContainer(group, "container");
		Check check1=new Check(group, "check1", new Model("check1"));
		Check check2=new Check(container, "check2", new Model("check2"));
		
		group.setEnabled(false);
	}
}
