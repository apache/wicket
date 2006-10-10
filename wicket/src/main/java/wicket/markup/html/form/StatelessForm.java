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
package wicket.markup.html.form;

import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * This StatelessForm is the same as a normal form but with the statelesshint default to true.
 * The form can be newly constructed when the onSubmit of its form or its buttons is called.
 * So you can't depend on state within the page. The only state you can depend on is what 
 * was submitted from the browser.  So the model of the form or the formcomponents are updated
 * with the submit values.
 * 
 * @param <T>
 *            Type of model object this component holds
 *            
 * @author jcompagner
 */
public class StatelessForm<T> extends Form<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * @param parent
	 * @param id
	 */
	public StatelessForm(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Construct.
	 * @param parent
	 * @param id
	 * @param model
	 */
	public StatelessForm(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	
	@Override
	protected boolean getStatelessHint()
	{
		return true;
	}
}
