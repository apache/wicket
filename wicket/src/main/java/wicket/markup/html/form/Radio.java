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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.model.IModel;
import wicket.util.lang.Objects;

/**
 * Component representing a single radio choice in a
 * wicket.markup.html.form.RadioGroup.
 * 
 * Must be attached to an &lt;input type=&quot;radio&quot; ... &gt; markup.
 * 
 * @param <T>
 *            The type
 * 
 * @see wicket.markup.html.form.RadioGroup
 * 
 * @author Igor Vaynberg (ivaynberg@users.sf.net)
 * @author Sven Meier (svenmeier)
 * 
 */
public class Radio<T> extends AbstractRadio<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 */
	public Radio(MarkupContainer parent, String id)
	{
		super(parent, id);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 */
	public Radio(MarkupContainer parent, String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param model
	 * @param group
	 */
	public Radio(MarkupContainer parent, String id, IModel<T> model, RadioGroup group)
	{
		super(parent, id, model, group);
	}

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 * @param group
	 */
	public Radio(MarkupContainer parent, String id, RadioGroup group)
	{
		super(parent, id, group);
	}

	/**
	 * @see Component#onComponentTag(ComponentTag)
	 * @param tag
	 *            the abstraction representing html tag of this component
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		// Default handling for component tag
		super.onComponentTag(tag);

		// must be attached to <input type="radio" .../> tag
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "radio");

		final RadioGroup group = findParent(RadioGroup.class);

		final String value = getValue();

		if (group == null)
		{
			throw new WicketRuntimeException(
					"Radio component ["
							+ getPath()
							+ "] cannot find its parent RadioGroup. All Radio components must be a child of or below in the hierarchy of a RadioGroup component.");
		}

		// assign name and value
		tag.put("name", group.getInputName());
		tag.put("value", value);

		// compare the model objects of the group and self, if the same add the
		// checked attribute, first check if there was a raw input on the group.
		boolean checked = false;

		if (group.hasRawInput())
		{
			String rawInput = group.getRawInput();
			if (rawInput != null && rawInput.equals(value))
			{
				checked = true;
			}
		}
		else if (Objects.equal(group.getModelObject(), getModelObject()))
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
			tag.put("disabled", "disabled");
		}
	}
	
}
