/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.border;

import wicket.markup.html.WebPage;
import wicket.markup.html.form.TextField;
import wicket.model.PropertyModel;
import wicket.util.value.ValueMap;


/**
 * Mock page for testing.
 * 
 * @author Chris Turner
 */
public class BoxBorderTestPage_3 extends WebPage
{
	private static final long serialVersionUID = 1L;

	private final ValueMap properties = new ValueMap();
	
	/**
	 * Construct.
	 * 
	 * 
	 */
	public BoxBorderTestPage_3()
	{
		Border formBorder = new FormBorder("border");
		add(formBorder);
		
		formBorder.add(new TextField("name", new PropertyModel(properties, "name")));
	}
}
