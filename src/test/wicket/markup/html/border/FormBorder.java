/*
 * $Id: FormBorder.java 5875 2006-05-25 22:52:19 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 22:52:19 +0000 (Thu, 25 May
 * 2006) $
 * 
 * ========================================================================
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
package wicket.markup.html.border;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.html.form.Form;


/**
 * Test the component: PageView
 * 
 * @author Juergen Donnerstag
 */
public class FormBorder extends Border
{
	private static final long serialVersionUID = 1L;

	private Form form;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public FormBorder(MarkupContainer parent, final String id)
	{
		super(parent, id);

		this.form = new Form(this, "myForm");
	}

	/**
	 * 
	 * @param child
	 * @return MarkupContainer
	 */
	public MarkupContainer addToForm(final Component child)
	{
		return this;
	}
}