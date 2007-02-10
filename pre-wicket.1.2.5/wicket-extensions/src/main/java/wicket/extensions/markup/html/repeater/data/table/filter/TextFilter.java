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
package wicket.extensions.markup.html.repeater.data.table.filter;

import wicket.markup.html.form.TextField;
import wicket.model.IModel;

/**
 * Filter that can be represented by a text field
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class TextFilter extends AbstractFilter
{
	private static final long serialVersionUID = 1L;

	private final TextField filter;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model for the underlying form component
	 * @param form
	 *            filter form this filter will be added to
	 */
	public TextFilter(String id, IModel model, FilterForm form)
	{
		super(id, form);
		filter = new TextField("filter", model);
		enableFocusTracking(filter);
		add(filter);
	}

	/**
	 * @return underlying {@link TextField} form component that represents this
	 *         filter
	 */
	public final TextField getFilter()
	{
		return filter;
	}


}
