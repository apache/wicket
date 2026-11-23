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
package org.apache.wicket.extensions.ajax.markup.html.modal;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Presents a modal dialog to the user. See {@link #open(Component, AjaxRequestTarget)} and
 * {@link #close(AjaxRequestTarget)} methods.
 * <p>
 * The dialog follows the WAI-ARIA pattern of a modal dialog: its markup has {@code role="dialog"}
 * and {@code aria-modal="true"}, the focus moves into it when it opens and back to the element
 * that had it when it closes, Tab and Shift+Tab cycle through the dialog, and Escape closes it.
 * Give the dialog a name with {@link #labelledBy(Component)} or {@link #label(IModel)}; see
 * {@link #trapFocus(boolean)} and {@link #closeOnEscape(boolean)} to change the keyboard handling.
 * <p>
 * Note: This component does not provide any styling by itself, so you have can add a
 * {@link DefaultTheme} to this component if aren't styling these CSS classes by yourself:
 * <dl>
 * <dt>modal-dialog-overlay</dt>
 * <dd>the wrapper around the actual dialog, usually used to overlay the rest of the document</dd>
 * <dt>modal-dialog</dt>
 * <dd>the actual dialog</dd>
 * <dt>modal-dialog-content</dt>
 * <dd>any additional styling for the content of this dialog</dd>
 * </dl>
 *
 * @author Igor Vaynberg (ivaynberg)
 * @author svenmeier
 */
public class ModalDialog extends Panel
{

	private static final long serialVersionUID = 1L;

	private static final String OVERLAY_ID = "overlay";

	private static final String DIALOG_ID = "dialog";

	private static final ResourceReference JS = new JavaScriptResourceReference(
		ModalDialog.class, "modal-dialog.js");

	/**
	 * The id for the content of this dialoh.
	 *
	 * @see #setContent(Component)
	 * @see #open(Component, AjaxRequestTarget)
	 */
	public static final String CONTENT_ID = "content";

	private final WebMarkupContainer overlay;

	private final WebMarkupContainer dialog;

	private boolean removeContentOnClose;

	private boolean closeOnEscape = true;

	private boolean trapFocus = true;

	private Component title;

	private IModel<String> label;

	public ModalDialog(String id)
	{
		super(id);

		setOutputMarkupId(true);

		overlay = newOverlay(OVERLAY_ID);
		overlay.setVisible(false);
		overlay.add(new DialogScript());
		add(overlay);

		dialog = newDialog(DIALOG_ID);
		dialog.setOutputMarkupId(true);
		dialog.add(new DialogAttributes());
		overlay.add(dialog);
	}

	/**
	 * Factory method for the overlay markup around the dialog.
	 *
	 * @param overlayId
	 * 	id
	 * @return overlay
	 */
	protected WebMarkupContainer newOverlay(String overlayId)
	{
		return new WebMarkupContainer(overlayId);
	}

	/**
	 * Factory method for the dialog markup around the content.
	 *
	 * @param dialogId
	 * 	id
	 * @return overlay
	 */
	protected WebMarkupContainer newDialog(String dialogId)
	{
		return new WebMarkupContainer(dialogId);
	}

	/**
	 * Set a content.
	 *
	 * @param content
	 * @see #open(AjaxRequestTarget)
	 */
	public void setContent(Component content)
	{
		if (!CONTENT_ID.equals(content.getId()))
		{
			throw new IllegalArgumentException(
				"Content must have wicket id set to ModalDialog.CONTENT_ID");
		}

		dialog.addOrReplace(content);

		removeContentOnClose = false;
	}

	/**
	 * Open the dialog with a content.
	 * <p>
	 * The content will be removed on close of the dialog.
	 *
	 * @param content
	 * 	the content
	 * @param target
	 * 	an optional Ajax target
	 * @return this
	 * @see #setContent(Component)
	 * @see #close(AjaxRequestTarget)
	 */
	public ModalDialog open(Component content, AjaxRequestTarget target)
	{
		setContent(content);
		removeContentOnClose = true;

		overlay.setVisible(true);

		if (target != null)
		{
			target.add(this);
		}

		return this;
	}

	/**
	 * Open the dialog.
	 *
	 * @param target
	 * 	an optional Ajax target
	 * @return this
	 * @see #setContent(Component)
	 */
	public ModalDialog open(AjaxRequestTarget target)
	{
		if (overlay.size() == 0)
		{
			throw new WicketRuntimeException(
				String.format("ModalDialog with id '%s' has no content set!", getId()));
		}

		overlay.setVisible(true);

		if (target != null)
		{
			target.add(this);
		}

		return this;
	}

	/**
	 * Is this dialog open.
	 *
	 * @return <code>true</code> if open
	 */
	public boolean isOpen()
	{
		return overlay.isVisible();
	}

	/**
	 * Close this dialog.
	 * <p>
	 * If opened via {@link #open(Component, AjaxRequestTarget)}, the content is removed from the
	 * component tree
	 *
	 * @param target
	 * 	an optional Ajax target
	 * @return this
	 * @see #open(Component, AjaxRequestTarget)
	 */
	public ModalDialog close(AjaxRequestTarget target)
	{
		overlay.setVisible(false);
		if (removeContentOnClose)
		{
			dialog.removeAll();

			if (title != null && !dialog.contains(title, true))
			{
				title = null;
			}
		}

		if (target != null)
		{
			target.add(this);
		}

		return this;
	}

	/**
	 * Close this dialog on press of escape key, which it does by default.
	 *
	 * @return this
	 * @see #closeOnEscape(boolean)
	 */
	public ModalDialog closeOnEscape()
	{
		return closeOnEscape(true);
	}

	/**
	 * Sets whether the Escape key closes this dialog, which it does by default.
	 * <p>
	 * Escape closes the innermost open dialog that contains the focused element, or the dialog
	 * opened last when no element has the focus, by calling {@link #close(AjaxRequestTarget)} in
	 * an Ajax request. A widget in the dialog that handles Escape itself, and stops the event or
	 * prevents its default action, keeps the dialog open.
	 *
	 * @param close
	 *            {@code false} to keep the dialog open when Escape is pressed
	 * @return this
	 * @since 12.0.0
	 */
	public ModalDialog closeOnEscape(boolean close)
	{
		closeOnEscape = close;
		return this;
	}

	/**
	 * Close this dialog on click outside.
	 *
	 * @return this
	 */
	public ModalDialog closeOnClick()
	{
		overlay.add(new CloseBehavior("click")
		{
			protected CharSequence getPrecondition()
			{
				return String.format("return attrs.event.target.id === '%s';",
					overlay.getMarkupId());
			}
		});
		return this;
	}

	/**
	 * Convenience method to trap focus inside the overlay with a {@link TrapFocusBehavior}, which
	 * pulls the focus back whenever it moves outside of the dialog.
	 *
	 * @return this
	 * @see #trapFocus(boolean)
	 * @see TrapFocusBehavior
	 */
	public ModalDialog trapFocus()
	{
		return trapFocus(true);
	}

	/**
	 * Sets how the focus is kept inside the dialog.
	 * <p>
	 * By default, Tab and Shift+Tab cycle through the elements of the dialog that can be focused,
	 * and nothing else stops the focus from leaving it, so a widget in the dialog can still focus
	 * a popup of its own that is rendered outside of the dialog. With {@code true}, the overlay
	 * gets a {@link TrapFocusBehavior} as well, which pulls the focus back from wherever it moves
	 * outside of the dialog. With {@code false}, the dialog leaves the focus alone once it has
	 * moved it into the dialog: Tab moves on from its last element to the page behind it, and a
	 * {@link TrapFocusBehavior} added before is removed.
	 *
	 * @param trap
	 *            {@code true} to pull the focus back into the dialog, {@code false} to leave Tab
	 *            to the browser
	 * @return this
	 * @since 12.0.0
	 */
	public ModalDialog trapFocus(boolean trap)
	{
		trapFocus = trap;

		var traps = overlay.getBehaviors(TrapFocusBehavior.class);
		if (!trap)
		{
			traps.forEach(overlay::remove);
		}
		else if (traps.isEmpty())
		{
			overlay.add(new TrapFocusBehavior());
		}

		return this;
	}

	/**
	 * Names the dialog after a component in it, usually the heading of its content: the dialog
	 * renders {@code aria-labelledby} with the markup id of the component, so that screen readers
	 * announce its text when the dialog opens. The component is told to output its markup id.
	 * <p>
	 * The name is rendered while the component is in the dialog. When the dialog closes and
	 * removes the content it was opened with, a title in that content is forgotten, so a dialog
	 * that is opened with a new content each time is named for each opening:
	 *
	 * <pre>
	 * dialog.labelledBy(content.getTitle()).open(content, target);
	 * </pre>
	 *
	 * @param title
	 *            the component that names the dialog, or {@code null} for none
	 * @return this
	 * @since 12.0.0
	 */
	public ModalDialog labelledBy(Component title)
	{
		if (title != null)
		{
			title.setOutputMarkupId(true);
		}
		this.title = title;
		return this;
	}

	/**
	 * Names the dialog with a text, for a dialog that shows no title: the dialog renders
	 * {@code aria-label} with the object of the model, escaped as any attribute value. Prefer
	 * {@link #labelledBy(Component)} for a dialog with a visible title.
	 *
	 * @param label
	 *            the name of the dialog, or {@code null} for none
	 * @return this
	 * @since 12.0.0
	 */
	public ModalDialog label(IModel<String> label)
	{
		this.label = label;
		return this;
	}

	/**
	 * can be overridden to change the {@link AjaxRequestAttributes} of the Ajax requests that
	 * close the dialog on Escape and on a click outside. For example to change the Ajax Channel.
	 *
	 * @param attributes
	 * 	the {@link AjaxRequestAttributes} of the request that closes the dialog
	 */
	protected void postProcessCloseBehaviorAjaxAttributes(AjaxRequestAttributes attributes)
	{
		// no op
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();

		if (label != null)
		{
			label.detach();
		}
	}

	private class DialogAttributes extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			putIfAbsent(tag, "role", "dialog");
			putIfAbsent(tag, "aria-modal", "true");

			if (title != null && dialog.contains(title, true))
			{
				tag.put("aria-labelledby", title.getMarkupId());
			}
			String text = label != null ? label.getObject() : null;
			if (text != null)
			{
				tag.put("aria-label", text);
			}
		}

		private void putIfAbsent(ComponentTag tag, String name, String value)
		{
			if (tag.getAttribute(name) == null)
			{
				tag.put(name, value);
			}
		}
	}

	private class DialogScript extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);

			postProcessCloseBehaviorAjaxAttributes(attributes);
		}

		@Override
		public void renderHead(Component component, IHeaderResponse response)
		{
			super.renderHead(component, response);

			response.render(JavaScriptHeaderItem.forReference(JS));

			CharSequence escape = closeOnEscape ? renderAjaxAttributes(component) : "null";
			response.render(OnDomReadyHeaderItem.forScript(String.format(
				"Wicket.ModalDialog.open({overlay: '%s', dialog: '%s', trapFocus: %b, escape: %s});",
				component.getMarkupId(), dialog.getMarkupId(), trapFocus, escape)));
		}

		@Override
		protected void respond(AjaxRequestTarget target)
		{
			if (closeOnEscape)
			{
				close(target);
			}
		}

		@Override
		public boolean getStatelessHint(Component component)
		{
			return !closeOnEscape;
		}
	}

	private abstract class CloseBehavior extends AjaxEventBehavior
	{
		private CloseBehavior(String event)
		{
			super(event);
		}

		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
		{
			super.updateAjaxAttributes(attributes);

			// has to stop immediately to prevent an enclosing dialog to close too
			attributes.setEventPropagation(EventPropagation.STOP_IMMEDIATE);

			attributes.getAjaxCallListeners().add(new AjaxCallListener()
			{
				@Override
				public CharSequence getPrecondition(Component component)
				{
					return CloseBehavior.this.getPrecondition();
				}
			});

			postProcessCloseBehaviorAjaxAttributes(attributes);
		}

		protected CharSequence getPrecondition()
		{
			return "";
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
			close(target);
		}
	}
}
