/*
 * $Id: RadioGroupTestPage2.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May
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

import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.model.Model;

/**
 * Tests rendering of the RadioGroup and Radio components
 * 
 * @author igor
 */
public class RadioGroupTestPage2 extends WebPage
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor
	 */
	public RadioGroupTestPage2()
	{
		Form form = new Form(this, "form");
		RadioGroup group = new RadioGroup<String>(form, "group", new Model<String>("radio2"));
		new WebMarkupContainer(group, "container");
		new Radio<String>(group, "radio1", new Model<String>("radio1"));
		new Radio<String>(form, "radio2", new Model<String>("radio2"));
	}
}
