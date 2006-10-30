/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 07:46:36 +0200 (vr, 26 mei 2006) $
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

import java.util.Collection;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;

/**
 * Component representing a single checkbox choice in a
 * wicket.markup.html.form.CheckGroup.
 * 
 * Must be attached to an &lt;input type=&quot;checkbox&quot; ... &gt; markup.
 * 
 * @see wicket.markup.html.form.CheckGroup
 * @param <T>
 *            The type
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class Check<T> extends WebMarkupContainer<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String ATTR_DISABLED = "disabled";


	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String)
	 */
	public Check(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * @see WebMarkupContainer#WebMarkupContainer(MarkupContainer,String,
	 *      IModel)
	 */
	public Check(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}


	/**
	 * @see Component#onComponentTag(ComponentTag)
	 * @param tag
	 *            the abstraction representing html tag of this component
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="checkbox" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		CheckGroup<?> group = findParent(CheckGroup.class);
		String path = getPath();
		if (group == null)
		{
			throw new WicketRuntimeException(
					"Check component ["
							+ path
							+ "] cannot find its parent CheckGroup. All Check components must be a child of or below in the hierarchy of a CheckGroup component.");
		}

		String relativePath = path.substring(group.getPath().length() + 1);
		
		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", relativePath);

		// check if the model collection of the group contains the model object.
		// if it does check the check box.
		Collection collection = (Collection)group.getModelObject();

		// check for npe in group's model object
		if (collection == null)
		{
			throw new WicketRuntimeException(
					"CheckGroup ["
							+ group.getPath()
							+ "] contains a null model object, must be an object of type java.util.Collection");
		}

		if (group.hasRawInput())
		{
			String rawInput = group.getRawInput();
			if (rawInput != null && rawInput.indexOf(relativePath) != -1)
			{
				tag.put("checked", "checked");
			}
		}
		else if (collection.contains(getModelObject()))
		{
			tag.put("checked", "checked");
		}

		if (group.wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			final CharSequence url = group.urlFor(IOnChangeListener.INTERFACE);

			Form form = (Form)group.findParent(Form.class);
			if (form != null)
			{
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onclick", "window.location.href='" + url + "&" + group.getInputName()
						+ "=' + this.value;");
			}
		}

		if (!isActionAuthorized(ENABLE) || !isEnabled() || !group.isEnabled())
		{
			tag.put(ATTR_DISABLED, ATTR_DISABLED);
		}
	}
}
