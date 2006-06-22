/*
 * $Id: AbstractTime.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision: 5874 $ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20 May
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
package wicket.extensions.ajax.markup.html;

import wicket.MarkupContainer;
import wicket.RequestCycle;
import wicket.ajax.AbstractDefaultAjaxBehavior;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.ClientEvent;
import wicket.markup.ComponentTag;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * A simple implementation of ajaxified edit-in-place component. Currently the
 * implementation is pretty inflexible, it is missing validator/error support.
 * It also does not allow the customization of save/cancel triggers. Maybe a
 * textarea instead of an input field would be nicer as well.
 * <p>
 * Current triggers: Save the edit if either enter is pressed or the component
 * loses focus. Cancel if esc is pressed.
 * 
 * @param <T>
 *            The type
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class AjaxEditableLabel<T> extends Panel<T>
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Edit behavior.
	 */
	private class EditorAjaxBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 */
		public EditorAjaxBehavior()
		{
		}

		/**
		 * @see wicket.behavior.AbstractAjaxBehavior#onComponentTag(wicket.markup.ComponentTag)
		 */
		@Override
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

		/**
		 * @see wicket.ajax.AbstractDefaultAjaxBehavior#respond(wicket.ajax.AjaxRequestTarget)
		 */
		@Override
		protected void respond(AjaxRequestTarget target)
		{
			RequestCycle rc = RequestCycle.get();
			boolean save = Boolean.valueOf(rc.getRequest().getParameter("save")).booleanValue();
			if (save)
			{
				editor.processInput();
			}
			label.setVisible(true);
			editor.setVisible(false);
			target.addComponent(AjaxEditableLabel.this);
		}
	}

	/**
	 * The label behavior.
	 */
	private final class LabeAjaxBehavior extends AjaxEventBehavior
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param event
		 */
		private LabeAjaxBehavior(ClientEvent event)
		{
			super(event);
		}

		/**
		 * @see wicket.ajax.AjaxEventBehavior#onEvent(wicket.ajax.AjaxRequestTarget)
		 */
		@Override
		protected void onEvent(AjaxRequestTarget target)
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
	}

	/** editor component. */
	private final TextField editor;

	/** label component. */
	private final Label label;

	/**
	 * @see wicket.Component#Component(MarkupContainer, String)
	 */
	public AjaxEditableLabel(MarkupContainer parent, final String id)
	{
		this(parent, id, null);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer, String, IModel)
	 */
	public AjaxEditableLabel(MarkupContainer parent, final String id, IModel<T> model)
	{
		super(parent, id);
		setOutputMarkupId(true);

		label = new Label(this, "label", model);
		label.setOutputMarkupId(true);
		label.add(new LabeAjaxBehavior(ClientEvent.CLICK));

		editor = new TextField<T>(this, "editor", model);
		editor.setOutputMarkupId(true);
		editor.setVisible(false);
		editor.add(new EditorAjaxBehavior());
	}
}
