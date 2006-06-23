/*
 * $Id: OddEvenItem.java 6043 2006-06-04 18:32:14 +0000 (Sun, 04 Jun 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-05-26 00:46:21 +0200 (vr, 26 mei
 * 2006) $
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
package wicket.extensions.markup.html.repeater.refreshing;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * Item that sets class="even" or class="odd" attributes based on its index
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of model object this item holds
 * 
 */
public class OddEvenItem<T> extends Item<T>
{
	private static final long serialVersionUID = 1L;

	private String CLASS_EVEN = "even";
	private String CLASS_ODD = "odd";

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param index
	 *            item index
	 * @param model
	 *            item model
	 */
	public OddEvenItem(MarkupContainer parent, final String id, int index, IModel<T> model)
	{
		super(parent, id, index, model);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("class", (getIndex() % 2 == 0) ? CLASS_EVEN : CLASS_ODD);
	}

}
