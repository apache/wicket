/*
 * $Id: AbstractFilter.java 5840 2006-05-24 20:49:09 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:49:09 +0000 (Wed, 24 May
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.MarkupContainer;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Base class for filters that provides some useful functionality
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AbstractFilter extends Panel
{
	private static final long serialVersionUID = 1L;

	private FilterForm form;

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param form
	 *            filter form of the filter toolbar
	 */
	public AbstractFilter(MarkupContainer parent, final String id, FilterForm form)
	{
		super(parent, id);
		this.form = form;
	}

	/**
	 * Enables the tracking of focus for the specified form component. This
	 * allows the filter form to restore focus to the component which caused the
	 * form submission. Great for when you are inside a filter textbox and use
	 * the enter key to submit the filter.
	 * 
	 * @param fc
	 *            form component for which focus tracking will be enabled
	 */
	protected void enableFocusTracking(FormComponent fc)
	{
		form.enableFocusTracking(fc);
	}

	protected IFilterStateLocator getStateLocator()
	{
		return form.getStateLocator();
	}

	protected IModel getStateModel()
	{
		return form.getModel();
	}

	protected Object getState()
	{
		return form.getModelObject();
	}

}
