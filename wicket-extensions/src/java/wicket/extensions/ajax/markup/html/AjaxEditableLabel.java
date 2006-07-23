package wicket.extensions.ajax.markup.html;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.ajax.AbstractDefaultAjaxBehavior;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.ComponentTag;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.IValidator;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractModel;
import wicket.model.IModel;
import wicket.util.string.JavascriptUtils;

/**
 * An implementation of ajaxified edit-in-place component using a
 * {@link TextField} as it's editor.
 * <p>
 * There are several methods that can be overriden for customization.
 * <ul>
 * <li>{@link #onEdit(AjaxRequestTarget)} is called when the label is clicked
 * and the editor is to be displayed. The default implementation switches the
 * label for the editor and places the curret at the end of the text. </li>
 * <li>{@link #onSubmit(AjaxRequestTarget)} is called when in edit mode, the
 * user submitted new content, that content validated well, and the model value
 * succesfully updated. This implementation also clears any
 * <code>window.status</code> set. </li>
 * <li>{@link #onError(AjaxRequestTarget)} is called when in edit mode, the
 * user submitted new content, but that content did not validate. Get the
 * current input by calling {@link FormComponent#getInput()} on
 * {@link #getEditor()}, and the error message by calling:
 * 
 * <pre>
 * String errorMessage = editor.getFeedbackMessage().getMessage();
 * </pre>
 * 
 * The default implementation of this method displays the error message in
 * <code>window.status</code>, redisplays the editor, selects the editor's
 * content and sets the focus on it.
 * <li>{@link #onCancel(AjaxRequestTarget)} is called when in edit mode, the
 * user choose not to submit the contents (he/she pressed espace). The default
 * implementation displays the label again without any further action.</li>
 * </ul>
 * </p>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Eelco Hillenius
 */
public class AjaxEditableLabel extends Panel
{
	private static final long serialVersionUID = 1L;

	/** editor component. */
	private FormComponent editor;

	/** label component. */
	private Component label;

	protected class EditorAjaxBehavior extends AbstractDefaultAjaxBehavior
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 */
		public EditorAjaxBehavior()
		{
		}

		protected void onComponentTag(ComponentTag tag)
		{
			super.onComponentTag(tag);
			final String saveCall = "{wicketAjaxGet('" + getCallbackUrl()
					+ "&save=true&'+this.name+'='+wicketEncode(this.value)); return true;}";

			final String cancelCall = "{wicketAjaxGet('" + getCallbackUrl()
					+ "&save=false'); return false;}";


			final String keypress = "var kc=wicketKeyCode(event); if (kc==27) " + cancelCall
					+ " else if (kc!=13) { return true; } else " + saveCall;

			tag.put("onblur", saveCall);
			tag.put("onkeypress", keypress);

		}

		protected void respond(AjaxRequestTarget target)
		{
			RequestCycle requestCycle = RequestCycle.get();
			boolean save = Boolean.valueOf(requestCycle.getRequest().getParameter("save"))
					.booleanValue();

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
		public LabelAjaxBehavior(String event)
		{
			super(event);
		}

		protected void onEvent(AjaxRequestTarget target)
		{
			onEdit(target);
		}
	}

	/**
	 * Model that allows other components to benefit of the compound model that
	 * AjaxEditableLabel inherits.
	 */
	private final class PassThroughModel extends AbstractModel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.model.IModel#getObject(wicket.Component)
		 */
		public Object getObject(Component component)
		{
			return getModel().getObject(AjaxEditableLabel.this);
		}

		/**
		 * @see wicket.model.IModel#setObject(wicket.Component,
		 *      java.lang.Object)
		 */
		public void setObject(Component component, Object object)
		{
			getModel().setObject(AjaxEditableLabel.this, object);
		}
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 */
	public AjaxEditableLabel(String id)
	{
		super(id);
		init(new PassThroughModel());
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 */
	public AjaxEditableLabel(String id, IModel model)
	{
		super(id, model);
		init((model != null) ? model : new PassThroughModel());
	}

	/**
	 * Adds a validator to this form component.
	 * 
	 * @param validator
	 *            The validator
	 * @return This
	 */
	public AjaxEditableLabel add(IValidator validator)
	{
		editor.add(validator);
		return this;
	}

	/**
	 * The value will be made available to the validator property by means of
	 * ${label}. It does not have any specific meaning to FormComponent itself.
	 * 
	 * @param labelModel
	 * @return this for chaining
	 */
	public AjaxEditableLabel setLabel(final IModel labelModel)
	{
		editor.setLabel(labelModel);
		return this;
	}

	/**
	 * @see wicket.MarkupContainer#setModel(wicket.model.IModel)
	 */
	public Component setModel(IModel model)
	{
		super.setModel(model);
		editor.setModel(model);
		return this;
	}

	/**
	 * Sets the required flag
	 * 
	 * @param required
	 * @return this for chaining
	 */
	public AjaxEditableLabel setRequired(final boolean required)
	{
		editor.setRequired(required);
		return this;
	}

	/**
	 * Sets the type that will be used when updating the model for this
	 * component. If no type is specified String type is assumed.
	 * 
	 * @param type
	 * @return this for chaining
	 */
	public AjaxEditableLabel setType(Class type)
	{
		editor.setType(type);
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
	protected FormComponent newEditor(MarkupContainer parent, String componentId, IModel model)
	{
		TextField editor = new TextField(componentId, model);
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior());
		return editor;
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
	protected Component newLabel(MarkupContainer parent, String componentId, IModel model)
	{
		Label label = new Label("label", model);
		label.setOutputMarkupId(true);
		label.add(new LabelAjaxBehavior("onclick"));
		return label;
	}

	/**
	 * Gets the editor component.
	 * 
	 * @return The editor component
	 */
	protected final FormComponent getEditor()
	{
		return editor;
	}

	/**
	 * Gets the label component.
	 * 
	 * @return The label component
	 */
	protected final Component getLabel()
	{
		return label;
	}

	/**
	 * Invoked when the label is in edit mode, and received a cancel event.
	 * Typically, nothing should be done here.
	 * 
	 * @param target
	 *            the ajax request target
	 */
	protected void onCancel(AjaxRequestTarget target)
	{
		label.setVisible(true);
		editor.setVisible(false);
		target.addComponent(AjaxEditableLabel.this);
	}

	/**
	 * Called when the label is clicked and the component is put in edit mode.
	 * 
	 * @param target
	 *            Ajax target
	 */
	protected void onEdit(AjaxRequestTarget target)
	{
		label.setVisible(false);
		editor.setVisible(true);
		target.addComponent(AjaxEditableLabel.this);
		// put focus on the textfield and stupid explorer hack to move the
		// caret to the end
		target.addJavascript("{ var el=wicketGet('" + editor.getMarkupId() + "');"
				+ "  el.focus(); " + "  if (el.createTextRange) { "
				+ "     var v = el.value; var r = el.createTextRange(); "
				+ "     r.moveStart('character', v.length); r.select(); } }");
	}

	/**
	 * Invoked when the label is in edit mode, received a new input, but that
	 * input didn't validate
	 * 
	 * @param target
	 *            the ajax request target
	 */
	protected void onError(AjaxRequestTarget target)
	{
		String errorMessage = editor.getFeedbackMessage().getMessage();
		if (errorMessage != null)
		{
			target.addJavascript("window.status='" + JavascriptUtils.escapeQuotes(errorMessage)
					+ "';");
		}
		String editorMarkupId = editor.getMarkupId();
		target.addJavascript(editorMarkupId + ".select();");
		target.addJavascript(editorMarkupId + ".focus();");
		target.addComponent(editor);
	}

	/**
	 * Invoked when the editor was succesfully updated. Use this method e.g. to
	 * persist the changed value. This implemention displays the label and
	 * clears any window status that might have been set in onError.
	 * 
	 * @param target
	 *            The ajax request target
	 */
	protected void onSubmit(AjaxRequestTarget target)
	{
		label.setVisible(true);
		editor.setVisible(false);
		target.addComponent(AjaxEditableLabel.this);

		target.addJavascript("window.status='';");
	}


	/**
	 * Internal init method.
	 * 
	 * @param model
	 */
	private void init(IModel model)
	{
		setOutputMarkupId(true);

		label = newLabel(this, "label", model);

		editor = newEditor(this, "editor", model);

		add(label);
		add(editor);
	}
}
