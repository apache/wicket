/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.form;

import java.util.Collection;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * Component representing a single checkbox choice in a
 * wicket.markup.html.form.CheckGroup.
 * 
 * Must be attached to an &lt;input type=&quot;checkbox&quot; ... &gt; markup.
 * 
 * @see wicket.markup.html.form.CheckGroup
 * @param <T>
 *            The type of model object
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * 
 */
public class Check<T> extends AbstractCheck<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param group
	 */
	public Check(MarkupContainer parent, String id, CheckGroup group)
	{
		super(parent, id, group);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param group
	 */
	public Check(MarkupContainer parent, String id, IModel<T> model, CheckGroup group)
	{
		super(parent, id, model, group);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public Check(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}


	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public Check(MarkupContainer parent, String id)
	{
		super(parent, id);
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

		final CheckGroup<?> group = getGroup();
		final String value = getValue();

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", value);

		// check if the model collection of the group contains the model object.
		// if it does check the check box.
		Collection collection = group.getModelObject();

		// check for npe in group's model object
		if (collection == null)
		{
			throw new WicketRuntimeException(
					"CheckGroup ["
							+ group.getPath()
							+ "] contains a null model object, must be an object of type java.util.Collection");
		}

		boolean checked = false;

		if (group.hasRawInput())
		{
			String[] inputs = group.getInputAsArray();
			if (inputs != null)
			{
				for (String input : inputs)
				{
					if (value.equals(input))
					{
						checked = true;
						break;
					}
				}
			}
		}
		else if (collection.contains(getModelObject()))
		{
			checked = true;
		}

		if (checked)
		{
			tag.put("checked", "checked");
		}

		if (group.wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			final CharSequence url = group.urlFor(IOnChangeListener.INTERFACE);

			Form form = group.findParent(Form.class);
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
		
		// disable html component if necessary
		if (!isActionAuthorized(ENABLE) || !isEnabled() || !group.isEnabled())
		{
			tag.put("disabled", "disabled");
		}

	}
}
