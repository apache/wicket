/*
 * $Id: DropDownChoice.java 5863 2006-05-25 22:04:15 +0000 (Thu, 25 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 22:04:15 +0000 (Thu, 25 May
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
package wicket.markup.html.form;

import java.util.List;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.model.IModel;

/**
 * A choice implemented as a dropdown menu/list.
 * <p>
 * Java:
 * 
 * <pre>
 * List SITES = Arrays.asList(new String[] { &quot;The Server Side&quot;, &quot;Java Lobby&quot;, &quot;Java.Net&quot; });
 * 
 * // Add a dropdown choice component that uses Input's 'site' property to designate the
 * // current selection, and that uses the SITES list for the available options.
 * // Note that when the selection is null, Wicket will lookup a localized string to
 * // represent this null with key: &quot;id + '.null'&quot;. In this case, this is 'site.null'
 * // which can be found in DropDownChoicePage.properties
 * form.add(new DropDownChoice(&quot;site&quot;, SITES));
 * </pre>
 * 
 * HTML:
 * 
 * <pre>
 *     	&lt;select wicket:id=&quot;site&quot;&gt;
 *     		&lt;option&gt;site 1&lt;/option&gt;
 *     		&lt;option&gt;site 2&lt;/option&gt;
 *     	&lt;/select&gt;
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * You can can extend this class and override method
 * wantOnSelectionChangedNotifications() to force server roundtrips on each
 * selection change.
 * </p>
 * 
 * @param <T>
 *            The type
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public class DropDownChoice<T> extends AbstractSingleSelectChoice<T> implements IOnChangeListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String)
	 */
	public DropDownChoice(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      List)
	 */
	public DropDownChoice(MarkupContainer parent, final String id, final List<T> choices)
	{
		super(parent, id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      List,IChoiceRenderer)
	 */
	public DropDownChoice(MarkupContainer parent, final String id, final List<T> data,
			final IChoiceRenderer<T> renderer)
	{
		super(parent, id, data, renderer);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel, List)
	 */
	public DropDownChoice(MarkupContainer parent, final String id, IModel<T> model,
			final List<T> choices)
	{
		super(parent, id, model, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel, List, IChoiceRenderer)
	 */
	public DropDownChoice(MarkupContainer parent, final String id, IModel<T> model,
			final List<T> data, final IChoiceRenderer<T> renderer)
	{
		super(parent, id, model, data, renderer);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel)
	 */
	public DropDownChoice(MarkupContainer parent, String id, IModel<List<T>> choices)
	{
		super(parent, id, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel,IModel)
	 */
	public DropDownChoice(MarkupContainer parent, String id, IModel<T> model,
			IModel<List<T>> choices)
	{
		super(parent, id, model, choices);
	}

	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel,IChoiceRenderer)
	 */
	public DropDownChoice(MarkupContainer parent, String id, IModel<List<T>> choices,
			IChoiceRenderer<T> renderer)
	{
		super(parent, id, choices, renderer);
	}


	/**
	 * @see wicket.markup.html.form.AbstractChoice#AbstractChoice(MarkupContainer,String,
	 *      IModel, IModel,IChoiceRenderer)
	 */
	public DropDownChoice(MarkupContainer parent, String id, IModel<T> model,
			IModel<List<T>> choices, IChoiceRenderer<T> renderer)
	{
		super(parent, id, model, choices, renderer);
	}

	/**
	 * Called when a selection changes.
	 */
	public final void onSelectionChanged()
	{
		convert();
		updateModel();
		onSelectionChanged(getModelObject());
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "select");

		// Should a roundtrip be made (have onSelectionChanged called) when the
		// selection changed?
		if (wantOnSelectionChangedNotifications())
		{
			// url that points to this components IOnChangeListener method
			final CharSequence url = urlFor(IOnChangeListener.INTERFACE);

			Form form = findParent(Form.class);
			if (form != null)
			{
				tag.put("onchange", form.getJsForInterfaceUrl(url));
			}
			else
			{
				// NOTE: do not encode the url as that would give invalid
				// JavaScript
				tag.put("onchange", "window.location.href='" + url + "&" + getInputName()
						+ "=' + this.options[this.selectedIndex].value;");
			}
		}

		super.onComponentTag(tag);
	}

	/**
	 * Template method that can be overriden by clients that implement
	 * IOnChangeListener to be notified by onChange events of a select element.
	 * This method does nothing by default.
	 * <p>
	 * Called when a option is selected of a dropdown list that wants to be
	 * notified of this event. This method is to be implemented by clients that
	 * want to be notified of selection events.
	 * 
	 * @param newSelection
	 *            The newly selected object of the backing model NOTE this is
	 *            the same as you would get by calling getModelObject() if the
	 *            new selection were current
	 */
	protected void onSelectionChanged(final Object newSelection)
	{
	}

	/**
	 * Whether this component's onSelectionChanged event handler should called
	 * using javascript if the selection changes. If true, a roundtrip will be
	 * generated with each selection change, resulting in the model being
	 * updated (of just this component) and onSelectionChanged being called.
	 * This method returns false by default.
	 * 
	 * @return True if this component's onSelectionChanged event handler should
	 *         called using javascript if the selection changes
	 */
	protected boolean wantOnSelectionChangedNotifications()
	{
		return false;
	}

	/**
	 * @see wicket.MarkupContainer#getStatelessHint()
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
}