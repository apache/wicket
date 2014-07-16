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
package org.apache.wicket.extensions.markup.html.form.palette;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.palette.component.Choices;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.extensions.markup.html.form.palette.component.Selection;
import org.apache.wicket.extensions.markup.html.form.palette.theme.DefaultTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;


/**
 * Palette is a component that allows the user to easily select and order multiple items by moving
 * them from one select box into another.
 * <p>
 * When creating a Palette object make sure your IChoiceRenderer returns a specific ID, not the
 * index.
 * <p>
 * <strong>Ajaxifying the palette</strong>: If you want to update a Palette with an
 * {@link AjaxFormComponentUpdatingBehavior}, you have to attach it to the contained
 * {@link Recorder} by overriding {@link #newRecorderComponent()} and calling
 * {@link #processInput()}:
 * 
 * <pre>
 *  Palette palette=new Palette(...) {
 *    protected Recorder newRecorderComponent()
 *    {
 *      Recorder recorder=super.newRecorderComponent();     
 *      recorder.add(new AjaxFormComponentUpdatingBehavior(&quot;change&quot;) {
 *        processInput() // let palette process input too
 *        ...
 *      });
 *      return recorder;
 *    }
 *  }
 * </pre>
 * 
 * You can add a {@link DefaultTheme} to style this component in a left to right fashion.
 * 
 * @author Igor Vaynberg ( ivaynberg )
 * @param <T>
 *            Type of model object
 * 
 */
public class Palette<T> extends FormComponentPanel<Collection<T>>
{
	private static final String SELECTED_HEADER_ID = "selectedHeader";

	private static final String AVAILABLE_HEADER_ID = "availableHeader";

	private static final long serialVersionUID = 1L;

	/** collection containing all available choices */
	private final IModel<? extends Collection<? extends T>> choicesModel;

	/**
	 * choice render used to render the choices in both available and selected collections
	 */
	private final IChoiceRenderer<T> choiceRenderer;

	/** number of rows to show in the select boxes */
	private final int rows;

	/** if reordering of selected items is allowed in */
	private final boolean allowOrder;

	/** if add all and remove all are allowed */
	private final boolean allowMoveAll;

	/**
	 * recorder component used to track user's selection. it is updated by javascript on changes.
	 */
	private Recorder<T> recorderComponent;

	/**
	 * component used to represent all available choices. by default this is a select box with
	 * multiple attribute
	 */
	private Component choicesComponent;

	/**
	 * component used to represent selected items. by default this is a select box with multiple
	 * attribute
	 */
	private Component selectionComponent;

	/** reference to the palette's javascript resource */
	private static final ResourceReference JAVASCRIPT = new JQueryPluginResourceReference(
		Palette.class, "palette.js");

	/**
	 * @param id
	 *            Component id
	 * @param choicesModel
	 *            Model representing collection of all available choices
	 * @param choiceRenderer
	 *            Render used to render choices. This must use unique IDs for the objects, not the
	 *            index.
	 * @param rows
	 *            Number of choices to be visible on the screen with out scrolling
	 * @param allowOrder
	 *            Allow user to move selections up and down
	 */
	public Palette(final String id, final IModel<? extends Collection<T>> choicesModel,
		final IChoiceRenderer<T> choiceRenderer, final int rows, final boolean allowOrder)
	{
		this(id, null, choicesModel, choiceRenderer, rows, allowOrder);
	}

	/**
	 * @param id
	 *            Component id
	 * @param model
	 *            Model representing collection of user's selections
	 * @param choicesModel
	 *            Model representing collection of all available choices
	 * @param choiceRenderer
	 *            Render used to render choices. This must use unique IDs for the objects, not the
	 *            index.
	 * @param rows
	 *            Number of choices to be visible on the screen with out scrolling
	 * @param allowOrder
	 *            Allow user to move selections up and down
	 */
	@SuppressWarnings("unchecked")
	public Palette(final String id, final IModel<? extends Collection<T>> model,
		final IModel<? extends Collection<? extends T>> choicesModel,
		final IChoiceRenderer<T> choiceRenderer, final int rows, final boolean allowOrder)
	{
		this(id, model, choicesModel, choiceRenderer, rows, allowOrder, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            Component id
	 * @param choicesModel
	 *            Model representing collection of all available choices
	 * @param choiceRenderer
	 *            Render used to render choices. This must use unique IDs for the objects, not the
	 *            index.
	 * @param rows
	 *            Number of choices to be visible on the screen with out scrolling
	 * @param allowOrder
	 *            Allow user to move selections up and down
	 * @param allowMoveAll
	 *            Allow user to add or remove all items at once
	 */
	public Palette(final String id, final IModel<? extends Collection<T>> model,
		final IModel<? extends Collection<? extends T>> choicesModel,
		final IChoiceRenderer<T> choiceRenderer, final int rows, final boolean allowOrder,
		boolean allowMoveAll)
	{
		super(id, (IModel<Collection<T>>)model);

		this.choicesModel = choicesModel;
		this.choiceRenderer = choiceRenderer;
		this.rows = rows;
		this.allowOrder = allowOrder;
		this.allowMoveAll = allowMoveAll;
	}

	@Override
	protected void onBeforeRender()
	{
		if (get("recorder") == null)
		{
			initFactories();
		}
		super.onBeforeRender();
	}


	/**
	 * One-time init method for components that are created via overridable factories. This method
	 * is here because we do not want to call overridable methods form palette's constructor.
	 */
	private void initFactories()
	{
		recorderComponent = newRecorderComponent();
		add(recorderComponent);

		choicesComponent = newChoicesComponent();
		add(choicesComponent);

		selectionComponent = newSelectionComponent();
		add(selectionComponent);


		add(newAddComponent());
		add(newRemoveComponent());
		add(newUpComponent().setVisible(allowOrder));
		add(newDownComponent().setVisible(allowOrder));
		add(newAddAllComponent().setVisible(allowMoveAll));
		add(newRemoveAllComponent().setVisible(allowMoveAll));

		add(newAvailableHeader(AVAILABLE_HEADER_ID));
		add(newSelectedHeader(SELECTED_HEADER_ID));
	}

	/**
	 * Return true if the palette is enabled, false otherwise
	 * 
	 * @return true if the palette is enabled, false otherwise
	 */
	public final boolean isPaletteEnabled()
	{
		return isEnabledInHierarchy();
	}


	/**
	 * @return iterator over selected choices
	 */
	public Iterator<T> getSelectedChoices()
	{
		return getRecorderComponent().getSelectedList().iterator();
	}

	/**
	 * @return iterator over unselected choices
	 */
	public Iterator<T> getUnselectedChoices()
	{
		return getRecorderComponent().getUnselectedList().iterator();
	}


	/**
	 * factory method to create the tracker component
	 * 
	 * @return tracker component
	 */
	protected Recorder<T> newRecorderComponent()
	{
		// create component that will keep track of selections
		return new Recorder<>("recorder", this);
	}

	/**
	 * factory method for the available items header
	 * 
	 * @param componentId
	 *            component id of the returned header component
	 * 
	 * @return available items component
	 */
	protected Component newAvailableHeader(final String componentId)
	{
		return new Label(componentId, new ResourceModel("palette.available", "Available"));
	}

	/**
	 * factory method for the selected items header
	 * 
	 * @param componentId
	 *            component id of the returned header component
	 * 
	 * @return header component
	 */
	protected Component newSelectedHeader(final String componentId)
	{
		return new Label(componentId, new ResourceModel("palette.selected", "Selected"));
	}


	/**
	 * factory method for the move down component
	 * 
	 * @return move down component
	 */
	protected Component newDownComponent()
	{
		return new PaletteButton("moveDownButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getDownOnClickJS());
			}
		};
	}

	/**
	 * factory method for the move up component
	 * 
	 * @return move up component
	 */
	protected Component newUpComponent()
	{
		return new PaletteButton("moveUpButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getUpOnClickJS());
			}
		};
	}

	/**
	 * factory method for the remove component
	 * 
	 * @return remove component
	 */
	protected Component newRemoveComponent()
	{
		return new PaletteButton("removeButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getRemoveOnClickJS());
			}
		};
	}

	/**
	 * factory method for the addcomponent
	 * 
	 * @return add component
	 */
	protected Component newAddComponent()
	{
		return new PaletteButton("addButton")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onComponentTag(final ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getAddOnClickJS());
			}
		};
	}

	/**
	 * factory method for the selected items component
	 * 
	 * @return selected items component
	 */
	protected Component newSelectionComponent()
	{
		return new Selection<T>("selection", this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Map<String, String> getAdditionalAttributes(final Object choice)
			{
				return Palette.this.getAdditionalAttributesForSelection(choice);
			}

			@Override
			protected boolean localizeDisplayValues()
			{
				return Palette.this.localizeDisplayValues();
			}
		};
	}

	/**
	 * factory method for the addAll component
	 * 
	 * @return addAll component
	 */
	protected Component newAddAllComponent()
	{
		return new PaletteButton("addAllButton")
		{
			private static final long serialVersionUID = 1L;

			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getAddAllOnClickJS());
			}
		};
	}


	/**
	 * factory method for the removeAll component
	 * 
	 * @return removeAll component
	 */
	protected Component newRemoveAllComponent()
	{
		return new PaletteButton("removeAllButton")
		{
			private static final long serialVersionUID = 1L;

			protected void onComponentTag(ComponentTag tag)
			{
				super.onComponentTag(tag);
				tag.getAttributes().put("onclick", Palette.this.getRemoveAllOnClickJS());
			}
		};
	}

	/**
	 * @param choice
	 * @return null
	 * @see org.apache.wicket.extensions.markup.html.form.palette.component.Selection#getAdditionalAttributes(Object)
	 */
	protected Map<String, String> getAdditionalAttributesForSelection(final Object choice)
	{
		return null;
	}

	/**
	 * factory method for the available items component
	 * 
	 * @return available items component
	 */
	protected Component newChoicesComponent()
	{
		return new Choices<T>("choices", this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Map<String, String> getAdditionalAttributes(final Object choice)
			{
				return Palette.this.getAdditionalAttributesForChoices(choice);
			}

			@Override
			protected boolean localizeDisplayValues()
			{
				return Palette.this.localizeDisplayValues();
			}
		};
	}

	/**
	 * Override this method if you do <strong>not</strong> want to localize the display values of
	 * the generated options. By default true is returned.
	 * 
	 * @return true If you want to localize the display values, default == true
	 */
	protected boolean localizeDisplayValues()
	{
		return true;
	}

	/**
	 * @param choice
	 * @return null
	 * @see org.apache.wicket.extensions.markup.html.form.palette.component.Selection#getAdditionalAttributes(Object)
	 */
	protected Map<String, String> getAdditionalAttributesForChoices(final Object choice)
	{
		return null;
	}

	protected Component getChoicesComponent()
	{
		return choicesComponent;
	}

	protected Component getSelectionComponent()
	{
		return selectionComponent;
	}

	/**
	 * Returns recorder component. Recorder component is a form component used to track the
	 * selection of the palette. It receives <code>onchange</code> javascript event whenever a
	 * change in selection occurs.
	 * 
	 * @return recorder component
	 */
	public final Recorder<T> getRecorderComponent()
	{
		return recorderComponent;
	}

	/**
	 * @return collection representing all available items
	 */
	public Collection<? extends T> getChoices()
	{
		return choicesModel.getObject();
	}

	/**
	 * @return collection representing selected items
	 */
	@SuppressWarnings("unchecked")
	public Collection<T> getModelCollection()
	{
		return (Collection<T>)getDefaultModelObject();
	}

	/**
	 * @return choice renderer
	 */
	public IChoiceRenderer<T> getChoiceRenderer()
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

	@Override
	protected void convertInput()
	{
		List<T> selectedList = getRecorderComponent().getSelectedList();
		if (selectedList.isEmpty())
		{
			setConvertedInput(null);
		}
		else
		{
			setConvertedInput(selectedList);
		}
	}

	/**
	 * The model object is assumed to be a Collection, and it is modified in-place. Then
	 * {@link Model#setObject(Object)} is called with the same instance: it allows the Model to be
	 * notified of changes even when {@link Model#getObject()} returns a different
	 * {@link Collection} at every invocation.
	 * 
	 * @see FormComponent#updateModel()
	 */
	@Override
	public final void updateModel()
	{
		FormComponent.updateCollectionModel(this);
	}

	/**
	 * builds javascript handler call
	 * 
	 * @param funcName
	 *            name of javascript function to call
	 * @return string representing the call tho the function with palette params
	 */
	protected String buildJSCall(final String funcName)
	{
		return new StringBuilder(funcName).append("('").append(getChoicesComponent().getMarkupId())
			.append("','").append(getSelectionComponent().getMarkupId()).append("','")
			.append(getRecorderComponent().getMarkupId()).append("');").toString();
	}


	/**
	 * @return choices component on focus javascript handler
	 */
	public String getChoicesOnFocusJS()
	{
		return buildJSCall("Wicket.Palette.choicesOnFocus");
	}

	/**
	 * @return selection component on focus javascript handler
	 */
	public String getSelectionOnFocusJS()
	{
		return buildJSCall("Wicket.Palette.selectionOnFocus");
	}

	/**
	 * @return add action javascript handler
	 */
	public String getAddOnClickJS()
	{
		return buildJSCall("Wicket.Palette.add");
	}

	/**
	 * @return remove action javascript handler
	 */
	public String getRemoveOnClickJS()
	{
		return buildJSCall("Wicket.Palette.remove");
	}

	/**
	 * @return move up action javascript handler
	 */
	public String getUpOnClickJS()
	{
		return buildJSCall("Wicket.Palette.moveUp");
	}

	/**
	 * @return move down action javascript handler
	 */
	public String getDownOnClickJS()
	{
		return buildJSCall("Wicket.Palette.moveDown");
	}

	/**
	 * @return addAll action javascript handler
	 */
	public String getAddAllOnClickJS()
	{
		return buildJSCall("Wicket.Palette.addAll");
	}

	/**
	 * @return removeAll action javascript handler
	 */
	public String getRemoveAllOnClickJS()
	{
		return buildJSCall("Wicket.Palette.removeAll");
	}

	@Override
	protected void onDetach()
	{
		// we need to manually detach the choices model since it is not attached
		// to a component
		// an alternative might be to attach it to one of the subcomponents
		choicesModel.detach();

		super.onDetach();
	}

	private class PaletteButton extends WebMarkupContainer
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param id
		 */
		public PaletteButton(final String id)
		{
			super(id);
		}


		@Override
		protected void onComponentTag(final ComponentTag tag)
		{
			super.onComponentTag(tag);

			if (!isPaletteEnabled())
			{
				tag.getAttributes().put("disabled", "disabled");
			}
		}
	}

	/**
	 * Renders header contributions
	 * 
	 * @param response
	 */
	@Override
	public void renderHead(final IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
	}
}
