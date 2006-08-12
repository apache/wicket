/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.extensions.markup.html.form.palette;

import java.util.Collection;
import java.util.Iterator;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.extensions.markup.html.form.palette.component.Choices;
import wicket.extensions.markup.html.form.palette.component.Recorder;
import wicket.extensions.markup.html.form.palette.component.Selection;
import wicket.markup.ComponentTag;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.image.Image;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.string.AppendingStringBuffer;

/**
 * Palette is a component that allows the user to easily select and order
 * multiple items by moving them from one select box into another.
 * 
 * <strong>Ajaxifying the palette</strong>: The palette itself cannot be
 * ajaxified because it is a panel and therefore does not receive any javascript
 * events. Instead ajax behaviors can be attached to the recorder component
 * which supports the javascript <code>onchange</code> event. The recorder
 * component can be retrieved via a call to {@link #getRecorderComponent()}.
 * 
 * Example:
 * 
 * <pre>
 *      Form form=new Form(...);
 *      Palette palette=new Palette(...);
 *      palette.getRecorderComponent().add(new AjaxFormComponentUpdatingBehavior(&quot;onchange&quot;) {...});
 * </pre>
 * 
 * @param <T>
 *            Type of model object this component holds
 * @param <E>
 *            Type of choices model object
 * 
 * @author Igor Vaynberg ( ivaynberg )
 */
public class Palette<T, E> extends Panel<Collection<T>>
{
	private static final long serialVersionUID = 1L;

	private static final String SELECTED_HEADER_ID = "selectedHeader";

	private static final String AVAILABLE_HEADER_ID = "availableHeader";

	/** collection containing all available choices */
	private IModel<E> choicesModel;

	/**
	 * choice render used to render the choices in both available and selected
	 * collections
	 */
	private IChoiceRenderer choiceRenderer;

	/** number of rows to show in the select boxes */
	private int rows;

	/**
	 * recorder component used to track user's selection. it is updated by
	 * javascript on changes.
	 */
	private Recorder recorderComponent;

	/**
	 * component used to represent all available choices. by default this is a
	 * select box with multiple attribute
	 */
	private Component choicesComponent;

	/**
	 * component used to represent selected items. by default this is a select
	 * box with multiple attribute
	 */
	private Component selectionComponent;

	/** reference to the palette's javascript resource */
	private static final PackageResourceReference javascript = new PackageResourceReference(
			Palette.class, "palette.js");

	/** reference to default up buttom image */
	private static final PackageResourceReference upImage = new PackageResourceReference(
			Palette.class, "up.gif");

	/** reference to default down button image */
	private static final PackageResourceReference downImage = new PackageResourceReference(
			Palette.class, "down.gif");

	/** reference to default remove button image */
	private static final PackageResourceReference removeImage = new PackageResourceReference(
			Palette.class, "remove.gif");

	/** reference to default add buttom image */
	private static final PackageResourceReference addImage = new PackageResourceReference(
			Palette.class, "add.gif");

	/**
	 * @param parent
	 *            The parent of this component The parent of this component.
	 * @param id
	 *            component id
	 * @param model
	 *            model representing collection of user's selections
	 * @param choicesModel
	 *            model representing collection of all available choices
	 * @param choiceRenderer
	 *            render used to render choices
	 * @param rows
	 *            number of choices to be visible on the screen with out
	 *            scrolling
	 * @param allowOrder
	 *            allow user to move selections up and down
	 */
	public Palette(MarkupContainer parent, final String id, IModel<Collection<T>> model,
			IModel<E> choicesModel, IChoiceRenderer choiceRenderer, int rows, boolean allowOrder)
	{
		super(parent, id, model);

		this.choicesModel = choicesModel;
		this.choiceRenderer = choiceRenderer;
		this.rows = rows;

		recorderComponent = newRecorderComponent();
		choicesComponent = newChoicesComponent();
		selectionComponent = newSelectionComponent();

		newAddComponent();
		newRemoveComponent();
		newUpComponent().setVisible(allowOrder);
		newDownComponent().setVisible(allowOrder);

		newAvailableHeader(AVAILABLE_HEADER_ID);
		newSelectedHeader(SELECTED_HEADER_ID);

		addJavascript();
	}

	/**
	 * adds the component used to represent the link the the javascript file to
	 * the header
	 */
	private void addJavascript()
	{
		IModel<CharSequence> srcReplacement = new Model<CharSequence>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public CharSequence getObject()
			{
				return urlFor(javascript);
			};
		};
		WebMarkupContainer javascript = new WebMarkupContainer(this, "javascript");
		javascript.add(new AttributeModifier("src", true, srcReplacement));
	}

	/**
	 * @return iterator over selected choices
	 */
	public Iterator getSelectedChoices()
	{
		return getRecorderComponent().getSelectedChoices();
	}

	/**
	 * @return iterator over unselected choices
	 */
	public Iterator getUnselectedChoices()
	{
		return getRecorderComponent().getUnselectedChoices();
	}

	/**
	 * factory method to create the tracker component
	 * 
	 * @return tracker component
	 */
	private Recorder newRecorderComponent()
	{
		// create component that will keep track of selections
		return new Recorder(this, "recorder", this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void updateModel()
			{
				super.updateModel();
				Palette.this.updateModel();
			}
		};
	}

	/**
	 * factory method for the available items header
	 * 
	 * @param componentId
	 *            component id of the returned header component
	 * 
	 * @return available items component
	 */
	protected Component newAvailableHeader(String componentId)
	{
		return new Label(this, componentId, "Available");
	}

	/**
	 * factory method for the selected items header
	 * 
	 * @param componentId
	 *            component id of the returned header component
	 * 
	 * @return header component
	 */
	protected Component newSelectedHeader(String componentId)
	{
		return new Label(this, componentId, "Selected");
	}

	/**
	 * factory method for the move down component
	 * 
	 * @return move down component
	 */
	protected Component newDownComponent()
	{
		WebMarkupContainer webMarkupContainer = new PaletteButton(this, "moveDownButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getDownOnClickJS());
			}
		};
		return new Image(webMarkupContainer, "image", downImage);
	}

	/**
	 * factory method for the move up component
	 * 
	 * @return move up component
	 */
	protected Component newUpComponent()
	{
		WebMarkupContainer webMarkupContainer = new PaletteButton(this, "moveUpButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getUpOnClickJS());
			}
		};
		return new Image(webMarkupContainer, "image", upImage);
	}

	/**
	 * factory method for the remove component
	 * 
	 * @return remove component
	 */
	protected Component newRemoveComponent()
	{
		WebMarkupContainer webMarkupContainer = new PaletteButton(this, "removeButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getRemoveOnClickJS());
			}
		};
		return new Image(webMarkupContainer, "image", removeImage);
	}

	/**
	 * factory method for the addcomponent
	 * 
	 * @return add component
	 */
	protected Component newAddComponent()
	{
		WebMarkupContainer webMarkupContainer = new PaletteButton(this, "addButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getAddOnClickJS());
			}
		};
		return new Image(webMarkupContainer, "image", addImage);
	}

	/**
	 * factory method for the selected items component
	 * 
	 * @return selected items component
	 */
	protected Component newSelectionComponent()
	{
		return new Selection(this, "selection", this);
	}

	/**
	 * factory method for the available items component
	 * 
	 * @return available items component
	 */
	protected Component newChoicesComponent()
	{
		return new Choices(this, "choices", this);
	}

	private Component getChoicesComponent()
	{
		return choicesComponent;
	}

	private Component getSelectionComponent()
	{
		return selectionComponent;
	}

	/**
	 * Returns recorder component. Recorder component is a form component used
	 * to track the selection of the palette. It receives <code>onchange</code>
	 * javascript event whenever a change in selection occurs.
	 * 
	 * @return recorder component
	 */
	public final Recorder getRecorderComponent()
	{
		return recorderComponent;
	}

	/**
	 * @return collection representing all available items
	 */
	public Collection getChoices()
	{
		return (Collection)choicesModel.getObject();
	}

	/**
	 * @return collection representing selected items
	 */
	public Collection getModelCollection()
	{
		return (Collection)getModelObject();
	}

	/**
	 * @return choice renderer
	 */
	public IChoiceRenderer getChoiceRenderer()
	{
		return choiceRenderer;
	}

	/**
	 * @return items visible without scrolling
	 */
	public int getRows()
	{
		return rows;
	}

	/**
	 * update the model upon form processing
	 */
	protected final void updateModel()
	{
		// prepare model
		Collection<T> model = getModelObject();
		model.clear();

		// update model
		Iterator<T> it = getRecorderComponent().getSelectedChoices();
		while (it.hasNext())
		{
			final T selectedChoice = it.next();
			model.add(selectedChoice);
		}
	}

	/**
	 * builds javascript handler call
	 * 
	 * @param funcName
	 *            name of javascript function to call
	 * @return string representing the call tho the function with palette params
	 */
	protected String buildJSCall(String funcName)
	{
		return new AppendingStringBuffer(funcName).append("('").append(
				getChoicesComponent().getMarkupId()).append("','").append(
				getSelectionComponent().getMarkupId()).append("','").append(
				getRecorderComponent().getMarkupId()).append("');").toString();
	}

	/**
	 * @return choices component on focus javascript handler
	 */
	public String getChoicesOnFocusJS()
	{
		return buildJSCall("paletteChoicesOnFocus");
	}

	/**
	 * @return selection component on focus javascript handler
	 */
	public String getSelectionOnFocusJS()
	{
		return buildJSCall("paletteSelectionOnFocus");
	}

	/**
	 * @return add action javascript handler
	 */
	public String getAddOnClickJS()
	{
		return buildJSCall("paletteAdd");
	}

	/**
	 * @return remove action javascript handler
	 */
	public String getRemoveOnClickJS()
	{
		return buildJSCall("paletteRemove");
	}

	/**
	 * @return move up action javascript handler
	 */
	public String getUpOnClickJS()
	{
		return buildJSCall("paletteMoveUp");
	}

	/**
	 * @return move down action javascript handler
	 */
	public String getDownOnClickJS()
	{
		return buildJSCall("paletteMoveDown");
	}

	@Override
	protected void internalOnDetach()
	{
		super.internalOnDetach();
		// we need to manually detach the choices model since it is not attached
		// to a component
		// an alternative might be to attach it to one of the subcomponents
		choicesModel.detach();
	}

	/**
	 * Return true if the palette is enabled, false otherwise
	 * 
	 * @return true if the palette is enabled, false otherwise
	 */
	public final boolean isPaletteEnabled()
	{
		return isEnabled() && isEnableAllowed();
	}

	private class PaletteButton extends WebMarkupContainer
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param id
		 */
		public PaletteButton(MarkupContainer parent, String id)
		{
			super(parent, id);
		}


		protected void onComponentTag(ComponentTag tag)
		{
			if (!isPaletteEnabled())
			{
				tag.getAttributes().put("disabled", "disabled");
			}
		}
	}
}
