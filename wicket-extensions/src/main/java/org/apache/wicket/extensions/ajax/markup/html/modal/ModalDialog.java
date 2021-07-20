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
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.extensions.ajax.markup.html.modal.theme.DefaultTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Presents a modal dialog to the user. See {@link #open(Component, AjaxRequestTarget)} and
 * {@link #close(AjaxRequestTarget)} methods.
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

	public ModalDialog(String id)
	{
		super(id);

		setOutputMarkupId(true);

		overlay = newOverlay(OVERLAY_ID);
		overlay.setVisible(false);
		add(overlay);

		dialog = newDialog(DIALOG_ID);
		overlay.add(dialog);
	}

	/**
	 * Factory method for the overlay markup around the dialog.
	 * 
	 * @param overlayId
	 *            id
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
	 *            id
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
	 * 
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
	 *            the content
	 * @param target
	 *            an optional Ajax target
	 * @return this
	 * 
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
	 *            an optional Ajax target
	 * @return this
	 * 
	 * @see #setContent(Component)
	 */
	public ModalDialog open(AjaxRequestTarget target)
	{
		if (overlay.size() == 0)
		{
			throw new WicketRuntimeException(String.format("ModalDialog with id '%s' has no content set!", getId()));
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
	 *            an optional Ajax target
	 * @return this
	 * 
	 * @see #open(Component, AjaxRequestTarget)
	 */
	public ModalDialog close(AjaxRequestTarget target)
	{
		overlay.setVisible(false);
		if (removeContentOnClose)
		{
			dialog.removeAll();
		}

		if (target != null)
		{
			target.add(this);
		}

		return this;
	}

	/**
	 * Close this dialog on press of escape key.
	 * 
	 * @return this
	 */
	public ModalDialog closeOnEscape()
	{
		overlay.add(new CloseBehavior("keydown")
		{
			protected CharSequence getPrecondition()
			{
				return "return Wicket.Event.keyCode(attrs.event) == 27";
			}
		});
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
				return String.format("return attrs.event.target.id === '%s';", overlay.getMarkupId());
			}
		});
		return this;
	}

	/**
	 * Convenience method to trap focus inside the overlay.
	 * 
	 * @see {@link TrapFocusBehavior}
	 * 
	 * @return this
	 */
	public ModalDialog trapFocus()
	{
		overlay.add(new TrapFocusBehavior());

		return this;
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
