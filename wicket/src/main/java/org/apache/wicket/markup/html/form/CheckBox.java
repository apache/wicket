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
package org.apache.wicket.markup.html.form;

import java.util.Locale;

import org.apache.wicket.RequestContext;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.portlet.PortletRequestContext;
import org.apache.wicket.util.convert.IConverter;

/**
 * HTML checkbox input component.
 * <p>
 * Java:
 * 
 * <pre>
 * form.add(new CheckBox(&quot;bool&quot;));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *  &lt;input type=&quot;checkbox&quot; wicket:id=&quot;bool&quot; /&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * You can can extend this class and override method wantOnSelectionChangedNotifications() to force
 * server roundtrips on each selection change.
 * </p>
 * 
 * @author Jonathan Locke
 */
public class CheckBox extends FormComponent<Boolean> implements IOnChangeListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public CheckBox(final String id)
	{
		this(id, null);
	}

	/**
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public CheckBox(final String id, IModel<Boolean> model)
	{
		super(id, model);
		setType(Boolean.class);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.IOnChangeListener#onSelectionChanged()
	 */
	public void onSelectionChanged()
	{
		convertInput();
		updateModel();
		onSelectionChanged(getDefaultModelObject());
	}

	/**
	 * Template method that can be overridden by clients that implement IOnChangeListener to be
	 * notified by onChange events of a select element. This method does nothing by default.
	 * <p>
	 * Called when a option is selected of a dropdown list that wants to be notified of this event.
	 * This method is to be implemented by clients that want to be notified of selection events.
	 * 
	 * @param newSelection
	 *            The newly selected object of the backing model NOTE this is the same as you would
	 *            get by calling getModelObject() if the new selection were current
	 */
	protected void onSelectionChanged(Object newSelection)
	{
	}

	/**
	 * Whether this component's onSelectionChanged event handler should called using javascript if
	 * the selection changes. If true, a roundtrip will be generated with each selection change,
	 * resulting in the model being updated (of just this component) and onSelectionChanged being
	 * called. This method returns false by default.
	 * 
	 * @return True if this component's onSelectionChanged event handler should called using
	 *         javascript if the selection changes
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#getStatelessHint()
	 */
	@Override
	protected boolean getStatelessHint()
	{
		if (wantOnSelectionChangedNotifications())
		{
			return false;
		}
		return super.getStatelessHint();
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "checkbox");

		final String value = getValue();
		final IConverter converter = getConverter(Boolean.class);
		final Boolean checked = (Boolean)converter.convertToObject(value, getLocale());

		if (Boolean.TRUE.equals(checked))
		{
			tag.put("checked", "checked");
		}
		else
		{
			// In case the attribute was added at design time
			tag.remove("checked");
		}

		// remove value attribute, because it overrides the browser's submitted value, eg a [input
		// type="checkbox" value=""] will always submit as false
		tag.remove("value");

		// Should a roundtrip be made (have onSelectionChanged called) when the
		// checkbox is clicked?
		if (wantOnSelectionChangedNotifications())
		{
			CharSequence url = urlFor(IOnChangeListener.INTERFACE);

			Form<?> form = findParent(Form.class);
			if (form != null)
			{
				RequestContext rc = RequestContext.get();
				if (rc.isPortletRequest())
				{
					// restore url back to real wicket path as its going to be interpreted by the
					// form itself
					url = ((PortletRequestContext)rc).getLastEncodedPath();
				}
				tag.put("onclick", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// TODO: following doesn't work with portlets, should be posted to a dynamic hidden
				// form
				// with an ActionURL or something
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onclick", "window.location.href='" + url +
					(url.toString().indexOf('?') > -1 ? "&amp;" : "?") + getInputName() +
					"=' + this.checked;");
			}

		}

		super.onComponentTag(tag);
	}

	/**
	 * @see FormComponent#supportsPersistence()
	 */
	@Override
	protected final boolean supportsPersistence()
	{
		return true;
	}


	/**
	 * Final because we made {@link #convertInput()} final and it no longer delegates to
	 * {@link #getConverter(Class)}
	 * 
	 * @see org.apache.wicket.Component#getConverter(java.lang.Class)
	 */
	@Override
	public final IConverter getConverter(Class<?> type)
	{
		if (Boolean.class.equals(type))
		{
			return CheckBoxConverter.INSTANCE;
		}
		else
		{
			return super.getConverter(type);
		}
	}

	/**
	 * Converter specific to the check box
	 * 
	 * @author igor.vaynberg
	 */
	private static class CheckBoxConverter implements IConverter
	{
		private static final long serialVersionUID = 1L;

		private static final IConverter INSTANCE = new CheckBoxConverter();

		/**
		 * Constructor
		 */
		private CheckBoxConverter()
		{

		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
		 *      java.util.Locale)
		 */
		public Object convertToObject(String value, Locale locale)
		{
			if ("on".equals(value) || "true".equals(value))
			{
				return Boolean.TRUE;
			}
			else
			{
				return Boolean.FALSE;
			}
		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
		 *      java.util.Locale)
		 */
		public String convertToString(Object value, Locale locale)
		{
			return ((Boolean)value).toString();
		}
	}

}
