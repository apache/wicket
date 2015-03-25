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
package org.apache.wicket.extensions.ajax.markup.html;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.IGenericComponent;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IObjectClassAwareModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidator;

/**
 * An implementation of ajaxified edit-in-place component using a {@link TextField} as it's editor.
 * <p>
 * There are several methods that can be overridden for customization.
 * <ul>
 * <li>{@link #onEdit(org.apache.wicket.ajax.AjaxRequestTarget)} is called when the label is clicked
 * and the editor is to be displayed. The default implementation switches the label for the editor
 * and places the caret at the end of the text.</li>
 * <li>{@link #onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)} is called when in edit mode, the
 * user submitted new content, that content validated well, and the model value successfully
 * updated. This implementation also clears any <code>window.status</code> set.</li>
 * <li>{@link #onError(org.apache.wicket.ajax.AjaxRequestTarget)} is called when in edit mode, the
 * user submitted new content, but that content did not validate. Get the current input by calling
 * {@link FormComponent#getInput()} on {@link #getEditor()}, and the error message by calling:
 * 
 * <pre>
 * String errorMessage = editor.getFeedbackMessage().getMessage();
 * </pre>
 * 
 * The default implementation of this method displays the error message in
 * <code>window.status</code>, redisplays the editor, selects the editor's content and sets the
 * focus on it.
 * <li>{@link #onCancel(org.apache.wicket.ajax.AjaxRequestTarget)} is called when in edit mode, the
 * user choose not to submit the contents (he/she pressed escape). The default implementation
 * displays the label again without any further action.</li>
 * </ul>
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 * @param <T>
 */
// TODO wonder if it makes sense to refactor this into a formcomponentpanel
public class AjaxEditableLabel<T> extends Panel implements IGenericComponent<T>
{
	private static final long serialVersionUID = 1L;

	/** editor component. */
	private FormComponent<T> editor;

	/** label component. */
	private Component label;

	protected class EditorAjaxBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);

			AjaxEditableLabel.this.updateEditorAjaxAttributes(attributes);
		}

		@Override
		public void renderHead(final Component component, final IHeaderResponse response)
		{
			super.renderHead(component, response);

			AjaxRequestTarget target = getRequestCycle().find(AjaxRequestTarget.class);
			if (target != null)
			{
				CharSequence callbackScript = getCallbackScript(component);
				target.appendJavaScript(callbackScript);
			}
		}

		@Override
		protected void respond(final AjaxRequestTarget target)
		{
			RequestCycle requestCycle = RequestCycle.get();
			boolean save = requestCycle.getRequest()
				.getRequestParameters()
				.getParameterValue("save")
				.toBoolean(false);

			if (save)
			{
				editor.processInput();

				if (editor.isValid())
				{
					onSubmit(target);
				}
				else
				{
					onError(target);
				}
			}
			else
			{
				onCancel(target);
			}
		}
	}

	protected class LabelAjaxBehavior extends AjaxEventBehavior
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param event
		 */
		public LabelAjaxBehavior(final String event)
		{
			super(event);
		}

		@Override
		protected void onEvent(final AjaxRequestTarget target)
		{
			onEdit(target);
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);

			AjaxEditableLabel.this.updateLabelAjaxAttributes(attributes);
		}
	}

	/**
	 * Gives a chance to the specializations to modify the Ajax attributes for the request when this
	 * component switches from an editor to a label.
	 * 
	 * @param attributes
	 *            The Ajax attributes to modify
	 */
	protected void updateLabelAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Gives a chance to the specializations to modify the Ajax attributes for the request when this
	 * component switches from a label to an editor.
	 * 
	 * @param attributes
	 *            The Ajax attributes to modify
	 */
	protected void updateEditorAjaxAttributes(AjaxRequestAttributes attributes)
	{
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public AjaxEditableLabel(final String id)
	{
		super(id);
		setOutputMarkupId(true);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxEditableLabel(final String id, final IModel<T> model)
	{
		super(id, model);
		setOutputMarkupId(true);
	}

	/**
	 * Adds a validator to this form component. A model must be available for this component before
	 * Validators can be added. Either add this Component to its parent (already having a Model), or
	 * provide one before this call via constructor {@link #AjaxEditableLabel(String,IModel)} or
	 * {@link #setDefaultModel(IModel)}.
	 * 
	 * @param validator
	 *            The validator
	 * @return This
	 */
	public final AjaxEditableLabel<T> add(final IValidator<T> validator)
	{
		getEditor().add(validator);
		return this;
	}

	/**
	 * Implementation that returns null by default (panels don't typically need converters anyway).
	 * This is used by the embedded default instances of label and form field to determine whether
	 * they should use a converter like they normally would (when this method returns null), or
	 * whether they should use a custom converter (when this method is overridden and returns not
	 * null).
	 */
	@Override
	public <C> IConverter<C> getConverter(final Class<C> type)
	{
		return null;
	}

	/**
	 * The value will be made available to the validator property by means of ${label}. It does not
	 * have any specific meaning to FormComponent itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	public final AjaxEditableLabel<T> setLabel(final IModel<String> labelModel)
	{
		getEditor().setLabel(labelModel);
		return this;
	}

	@Override
	public final AjaxEditableLabel<T> setDefaultModel(final IModel<?> model)
	{
		super.setDefaultModel(model);
		getLabel().setDefaultModel(model);
		getEditor().setDefaultModel(model);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IModel<T> getModel()
	{
		return (IModel<T>)getDefaultModel();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getModelObject()
	{
		return (T)getDefaultModelObject();
	}

	@Override
	public void setModel(IModel<T> model)
	{
		setDefaultModel(model);
	}

	@Override
	public void setModelObject(T object)
	{
		setDefaultModelObject(object);
	}

	/**
	 * Sets the required flag
	 * 
	 * @param required
	 * @return this for chaining
	 */
	public final AjaxEditableLabel<T> setRequired(final boolean required)
	{
		getEditor().setRequired(required);
		return this;
	}

	/**
	 * Sets the type that will be used when updating the model for this component. If no type is
	 * specified String type is assumed.
	 * 
	 * @param type
	 * @return this for chaining
	 */
	public final AjaxEditableLabel<T> setType(final Class<?> type)
	{
		getEditor().setType(type);
		return this;
	}

	/**
	 * Create a new form component instance to serve as editor.
	 * 
	 * @param parent
	 *            The parent component
	 * @param componentId
	 *            Id that should be used by the component
	 * @param model
	 *            The model
	 * @return The editor
	 */
	protected FormComponent<T> newEditor(final MarkupContainer parent, final String componentId,
		final IModel<T> model)
	{
		TextField<T> editor = new TextField<T>(componentId, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public <C> IConverter<C> getConverter(final Class<C> type)
			{
				IConverter<C> c = AjaxEditableLabel.this.getConverter(type);
				return c != null ? c : super.getConverter(type);
			}

			@Override
			protected void onModelChanged()
			{
				super.onModelChanged();
				AjaxEditableLabel.this.onModelChanged();
			}

			@Override
			protected void onModelChanging()
			{
				super.onModelChanging();
				AjaxEditableLabel.this.onModelChanging();
			}
		};
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior()
		{
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.setEventNames("blur", "keyup", "keydown");

				// Note: preventDefault is handled selectively below
				attributes.setPreventDefault(false);

				// Note: escape can be detected on keyup, enter can be detected on keyup
				CharSequence precondition = "var kc=Wicket.Event.keyCode(attrs.event),"
					+ "evtType=attrs.event.type,"
					+ "ret=false;"
					+ "if (evtType==='blur' || (evtType==='keyup' && kc===27) || (evtType==='keydown' && kc===13)) {attrs.event.preventDefault(); ret = true;}"
					+ "return ret;";
				AjaxCallListener ajaxCallListener = new AjaxCallListener();
				ajaxCallListener.onPrecondition(precondition);

				CharSequence dynamicExtraParameters = "var result,"
					+ "evtType=attrs.event.type;"
					+ "if (evtType === 'keyup') { result = { 'save': false }; }"
					+ "else { result = { 'save': true }; }"
					+ "return result;";
				attributes.getDynamicExtraParameters().add(dynamicExtraParameters);

				attributes.getAjaxCallListeners().add(ajaxCallListener);

			}
		});
		return editor;
	}

	/**
	 * Create a new form component instance to serve as label.
	 * 
	 * @param parent
	 *            The parent component
	 * @param componentId
	 *            Id that should be used by the component
	 * @param model
	 *            The model
	 * @return The editor
	 */
	protected Component newLabel(final MarkupContainer parent, final String componentId,
		final IModel<T> model)
	{
		Label label = new Label(componentId, model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public <C> IConverter<C> getConverter(final Class<C> type)
			{
				IConverter<C> c = AjaxEditableLabel.this.getConverter(type);
				return c != null ? c : super.getConverter(type);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
			{
				Object modelObject = getDefaultModelObject();
				if ((modelObject == null) || "".equals(modelObject))
				{
					replaceComponentTagBody(markupStream, openTag, defaultNullLabel());
				}
				else
				{
					super.onComponentTagBody(markupStream, openTag);
				}
			}
		};
		label.setOutputMarkupId(true);
		label.add(new LabelAjaxBehavior(getLabelAjaxEvent()));
		return label;
	}

	/**
	 * By default this returns "click", users can overwrite this on which event the label behavior
	 * should be triggered
	 * 
	 * @return The event name
	 */
	protected String getLabelAjaxEvent()
	{
		return "click";
	}


	/**
	 * Gets the editor component.
	 * 
	 * @return The editor component
	 */
	protected final FormComponent<T> getEditor()
	{
		if (editor == null)
		{
			initLabelAndEditor(new WrapperModel());
		}
		return editor;
	}

	/**
	 * Gets the label component.
	 * 
	 * @return The label component
	 */
	protected final Component getLabel()
	{
		if (label == null)
		{
			initLabelAndEditor(new WrapperModel());
		}
		return label;
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		// lazily add label and editor
		if (editor == null)
		{
			initLabelAndEditor(new WrapperModel());
		}
		// obsolete with WICKET-1919
		// label.setEnabled(isEnabledInHierarchy());
	}

	/**
	 * Invoked when the label is in edit mode, and received a cancel event. Typically, nothing
	 * should be done here.
	 * 
	 * @param target
	 *            the ajax request target
	 */
	protected void onCancel(final AjaxRequestTarget target)
	{
		label.setVisible(true);
		editor.setVisible(false);
		editor.clearInput();
		target.add(AjaxEditableLabel.this);
	}

	/**
	 * Called when the label is clicked and the component is put in edit mode.
	 * 
	 * @param target
	 *            Ajax target
	 */
	public void onEdit(final AjaxRequestTarget target)
	{
		label.setVisible(false);
		editor.setVisible(true);
		target.add(AjaxEditableLabel.this);
		String selectScript = String.format(
			"(function(){var el = Wicket.$('%s'); if (el.select) el.select();})()",
			editor.getMarkupId());
		target.appendJavaScript(selectScript);
		target.focusComponent(editor);
	}

	/**
	 * Invoked when the label is in edit mode, received a new input, but that input didn't validate
	 * 
	 * @param target
	 *            the ajax request target
	 */
	protected void onError(final AjaxRequestTarget target)
	{
		if (editor.hasErrorMessage())
		{
			Serializable errorMessage = editor.getFeedbackMessages().first(FeedbackMessage.ERROR);
			target.appendJavaScript("window.status='" +
				JavaScriptUtils.escapeQuotes(errorMessage.toString()) + "';");
		}
		String selectAndFocusScript = String.format(
			"(function(){var el=Wicket.$('%s'); if (el.select) el.select(); el.focus();})()",
			editor.getMarkupId());
		target.appendJavaScript(selectAndFocusScript);
	}

	/**
	 * Invoked when the editor was successfully updated. Use this method e.g. to persist the changed
	 * value. This implementation displays the label and clears any window status that might have
	 * been set in onError.
	 * 
	 * @param target
	 *            The ajax request target
	 */
	protected void onSubmit(final AjaxRequestTarget target)
	{
		label.setVisible(true);
		editor.setVisible(false);
		target.add(AjaxEditableLabel.this);

		target.appendJavaScript("window.status='';");
	}

	/**
	 * Lazy initialization of the label and editor components and set tempModel to null.
	 * 
	 * @param model
	 *            The model for the label and editor
	 */
	private void initLabelAndEditor(final IModel<T> model)
	{
		editor = newEditor(this, "editor", model);
		label = newLabel(this, "label", model);
		add(label);
		add(editor);
	}

	/**
	 * Model that accesses the parent model lazily. this is required since we eventually request the
	 * parents model before the component is added to the parent.
	 */
	private class WrapperModel implements IModel<T>, IObjectClassAwareModel<T>
	{
		@Override
		public T getObject()
		{
			return getParentModel().getObject();
		}

		@Override
		public void setObject(final T object)
		{
			getParentModel().setObject(object);
		}

		@Override
		public void detach()
		{
			getParentModel().detach();

		}

		@Override
		public Class<T> getObjectClass()
		{
			if (getParentModel() instanceof IObjectClassAwareModel)
			{
				return ((IObjectClassAwareModel)getParentModel()).getObjectClass();
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * @return Gets the parent model in case no explicit model was specified.
	 */
	private IModel<T> getParentModel()
	{
		// the #getModel() call below will resolve and assign any inheritable
		// model this component can use. Set that directly to the label and
		// editor so that those components work like this enclosing panel
		// does not exist (must have that e.g. with CompoundPropertyModels)
		IModel<T> m = getModel();

		// check that a model was found
		if (m == null)
		{
			Component parent = getParent();
			String msg = "No model found for this component, either pass one explicitly or "
				+ "make sure an inheritable model is available.";
			if (parent == null)
			{
				msg += " This component is not added to a parent yet, so if this component "
					+ "is supposed to use the model of the parent (e.g. when it uses a "
					+ "compound property model), add it first before further configuring "
					+ "the component calling methods like e.g. setType and addValidator.";
			}
			throw new IllegalStateException(msg);
		}
		return m;
	}

	/**
	 * Override this to display a different value when the model object is null. Default is
	 * <code>...</code>
	 * 
	 * @return The string which should be displayed when the model object is null.
	 */
	protected String defaultNullLabel()
	{
		return "...";
	}

	/**
	 * Dummy override to fix WICKET-1239
	 */
	@Override
	protected void onModelChanged()
	{
		super.onModelChanged();
	}

	/**
	 * Dummy override to fix WICKET-1239
	 */
	@Override
	protected void onModelChanging()
	{
		super.onModelChanging();
	}
}
